package com.tomato.app.focus

import android.os.SystemClock
import com.tomato.app.data.Graph
import com.tomato.app.data.db.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** 一次专注的实时状态 */
data class FocusState(
    val taskId: Long = -1L,
    val title: String = "",
    val totalMillis: Long = 0L,
    val remainMillis: Long = 0L,
    val running: Boolean = false,
    val finished: Boolean = false,
    val sessionId: Long = -1L,   // Room 行 id，-1 表示还没落库
    val startedAt: Long = 0L
) {
    /** 是否有正在进行的番茄（用于旋转/进程重建后自动回到专注页） */
    val active: Boolean get() = taskId >= 0L

    /** 已专注秒数 */
    val elapsedSec: Int get() = ((totalMillis - remainMillis) / 1000L).toInt()

    /** 进度 0f~1f（剩余比例） */
    val progress: Float
        get() = if (totalMillis <= 0L) 0f else remainMillis.toFloat() / totalMillis.toFloat()
}

/**
 * 番茄计时后端（进程内单例）。
 *
 * 设计要点：
 * 1. 用 [SystemClock.elapsedRealtime] 的**绝对截止时刻**计时，不累加 delta，
 *    因此息屏、丢帧、切后台都不会走偏。
 * 2. 100ms 一跳刷新 UI，圆环是连续动画而不是每秒跳一格。
 * 3. 每次状态变化 + 运行中每 15s 回写 Room，异常退出也留下记录。
 */
object FocusTimer {

    private const val TICK_MS = 100L
    private const val PERSIST_INTERVAL_MS = 15_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    @Volatile
    private var deadlineMs = 0L

    @Volatile
    private var lastPersistAt = 0L

    private var ticker: Job? = null

    /** 开始一个番茄（点击任务卡「开始」） */
    fun start(task: TaskEntity) {
        stopTicker()
        val total = task.minutes.coerceAtLeast(1) * 60_000L
        deadlineMs = SystemClock.elapsedRealtime() + total
        lastPersistAt = System.currentTimeMillis()
        _state.value = FocusState(
            taskId = task.id,
            title = task.title,
            totalMillis = total,
            remainMillis = total,
            running = true,
            startedAt = System.currentTimeMillis()
        )
        scope.launch {
            val id = Graph.repository.openSession(task.id, task.title)
            _state.value = _state.value.copy(sessionId = id)
        }
        startTicker()
    }

    fun pause() {
        val s = _state.value
        if (!s.running || s.finished) return
        stopTicker()
        val remain = (deadlineMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        _state.value = s.copy(running = false, remainMillis = remain)
        persist(completed = false)
    }

    fun resume() {
        val s = _state.value
        if (s.running || s.finished || !s.active) return
        deadlineMs = SystemClock.elapsedRealtime() + s.remainMillis
        _state.value = s.copy(running = true)
        startTicker()
    }

    /** 重置：回到满时长并立即开始 */
    fun reset() {
        val s = _state.value
        if (!s.active) return
        deadlineMs = SystemClock.elapsedRealtime() + s.totalMillis
        _state.value = s.copy(remainMillis = s.totalMillis, running = true, finished = false)
        persist(completed = false)
        startTicker()
    }

    /** 放弃 / 退出：按当前已专注时长落库（completed = false） */
    fun stop() {
        persist(completed = false)
        stopTicker()
        _state.value = FocusState()
    }

    /** 自然结束：标记完成 */
    private fun finish() {
        stopTicker()
        _state.value = _state.value.copy(remainMillis = 0L, running = false, finished = true)
        persist(completed = true)
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val remain = (deadlineMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                _state.value = _state.value.copy(remainMillis = remain)
                if (remain <= 0L) {
                    finish()
                    break
                }
                val now = System.currentTimeMillis()
                if (now - lastPersistAt >= PERSIST_INTERVAL_MS) {
                    lastPersistAt = now
                    persist(completed = false)
                }
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun persist(completed: Boolean) {
        val s = _state.value
        if (s.sessionId < 0L) return
        val sec = s.elapsedSec
        scope.launch { Graph.repository.closeSession(s.sessionId, sec, completed) }
    }
}
