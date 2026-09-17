package com.tomato.app.focus

import android.os.SystemClock
import com.tomato.app.data.Graph
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.model.TimerMode
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

/** 专注阶段：一个番茄由「专注 → 休息 → 专注 …」组成 */
enum class FocusPhase(val label: String) {
    FOCUS("专注中"),
    BREAK("休息中"),
    DONE("完成")
}

/** 一次专注的实时状态 */
data class FocusState(
    val taskId: Long = -1L,
    val title: String = "",
    val totalMillis: Long = 0L,
    val remainMillis: Long = 0L,
    val running: Boolean = false,
    val finished: Boolean = false,
    val sessionId: Long = -1L,   // Room 行 id，-1 表示当前阶段还没落库
    val startedAt: Long = 0L,

    // ===== 模式与循环 =====
    val mode: TimerMode = TimerMode.COUNTDOWN,
    val phase: FocusPhase = FocusPhase.FOCUS,
    val elapsedMillis: Long = 0L,      // 正向计时 / 不计时：本阶段已过时间
    val breakMillis: Long = 0L,        // 单次休息时长（0 = 不休息）
    val cycleIndex: Int = 1,           // 当前第几轮（1-based）
    val cycleTotal: Int = 1,
    val hideNextDay: Boolean = false   // 完成后是否隐藏到明天
) {
    /** 是否有正在进行的番茄（用于旋屏 / 返回后自动回到专注页） */
    val active: Boolean get() = taskId >= 0L

    /** 本阶段已专注秒数（注意：正向计时下 totalMillis = 0，不能再用差值算） */
    val elapsedSec: Int
        get() = when (mode) {
            TimerMode.COUNTDOWN -> ((totalMillis - remainMillis) / 1000L).toInt()
            else -> (elapsedMillis / 1000L).toInt()
        }

    /** 剩余比例 0f~1f；只有倒计时才有意义（其余返回 0，不画进度弧） */
    val progress: Float
        get() = when {
            mode != TimerMode.COUNTDOWN -> 0f
            totalMillis <= 0L -> 0f
            else -> (remainMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
        }

    /** 主时间显示用的毫秒数 */
    val displayMillis: Long
        get() = when (mode) {
            TimerMode.COUNTDOWN -> remainMillis
            TimerMode.STOPWATCH, TimerMode.NONE -> elapsedMillis
        }

    /** 圆环是否画进度弧 */
    val hasRingProgress: Boolean get() = mode == TimerMode.COUNTDOWN

    /** 是否需要用户手动结束（正向计时 / 不计时） */
    val manualStopOnly: Boolean get() = mode.manualStopOnly

    /** 是否达到「有效专注」门槛（低于它视为放弃，不计入统计） */
    val countedAsDone: Boolean get() = elapsedSec >= MIN_VALID_FOCUS_SEC

    companion object {
        const val MIN_VALID_FOCUS_SEC = 60
    }
}

/**
 * 番茄计时后端（进程内单例）。
 *
 * 设计要点：
 * 1. 用 [SystemClock.elapsedRealtime] 的**绝对截止时刻**计时，不累加 delta，
 *    因此息屏、丢帧、切后台都不会走偏。
 * 2. 100ms 一跳刷新 UI，圆环是连续动画而不是每秒跳一格。
 * 3. 三种模式：倒计时（有终点，自动完成）/ 正向计时（无终点，手动结束）/ 不计时（无时间，手动结束）。
 * 4. 循环：专注 N 分钟 → 自动切休息 M 分钟 → 再回专注，共 K 轮；最后一轮结束不再休息。
 *    每轮专注落一条会话（所以「完成 K 个番茄」），休息时长累计进该轮的 breakSec。
 */
object FocusTimer {

    private const val TICK_MS = 100L
    private const val PERSIST_INTERVAL_MS = 15_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    /** 本阶段开始的 elapsedRealtime（正向计时据此算已过时间） */
    @Volatile
    private var phaseStartMs = 0L

    /** 倒计时阶段的截止 elapsedRealtime；正向/不计时为 0 */
    @Volatile
    private var deadlineMs = 0L

    /** 暂停前已累积的毫秒（正向计时 / 不计时用） */
    @Volatile
    private var pausedAccumMs = 0L

    /** 本阶段已累积的休息秒数，收尾时写进当轮 session */
    @Volatile
    private var breakAccumSec = 0

    @Volatile
    private var lastPersistAt = 0L

    private var ticker: Job? = null

    // ===== 生命周期 =====

    /** 开始一个番茄（点击任务卡「开始」） */
    fun start(task: TaskEntity) {
        stopTicker()
        val mode = TimerMode.from(task.timerMode)
        val total = if (mode == TimerMode.COUNTDOWN) {
            task.minutes.coerceAtLeast(1) * 60_000L
        } else 0L

        val now = SystemClock.elapsedRealtime()
        phaseStartMs = now
        deadlineMs = if (total > 0L) now + total else 0L
        pausedAccumMs = 0L
        breakAccumSec = 0
        lastPersistAt = System.currentTimeMillis()

        _state.value = FocusState(
            taskId = task.id,
            title = task.title,
            totalMillis = total,
            remainMillis = total,
            running = true,
            startedAt = System.currentTimeMillis(),
            mode = mode,
            phase = FocusPhase.FOCUS,
            breakMillis = task.breakMinutes.coerceAtLeast(0) * 60_000L,
            cycleIndex = 1,
            cycleTotal = task.cycleTarget.coerceAtLeast(1),
            hideNextDay = task.hideNextDay
        )
        scope.launch {
            val id = Graph.repository.openSession(task.id, task.title, mode)
            _state.value = _state.value.copy(sessionId = id)
        }
        startTicker()
    }

    fun pause() {
        val s = _state.value
        if (!s.running || s.finished) return
        stopTicker()
        val now = SystemClock.elapsedRealtime()
        _state.value = if (s.mode == TimerMode.COUNTDOWN) {
            s.copy(running = false, remainMillis = (deadlineMs - now).coerceAtLeast(0L))
        } else {
            pausedAccumMs = s.elapsedMillis + (now - phaseStartMs)
            s.copy(running = false, elapsedMillis = pausedAccumMs)
        }
        persistCurrentPhase()
    }

    fun resume() {
        val s = _state.value
        if (s.running || s.finished || !s.active) return
        val now = SystemClock.elapsedRealtime()
        phaseStartMs = now - if (s.mode == TimerMode.COUNTDOWN) {
            (s.totalMillis - s.remainMillis)
        } else {
            pausedAccumMs
        }
        if (s.mode == TimerMode.COUNTDOWN) deadlineMs = now + s.remainMillis
        _state.value = s.copy(running = true)
        startTicker()
    }

    /**
     * 重置当前阶段：
     * - 倒计时：回到满时长并重新开始
     * - 正向 / 不计时：已过时间清零并继续
     */
    fun reset() {
        val s = _state.value
        if (!s.active) return
        val now = SystemClock.elapsedRealtime()
        phaseStartMs = now
        pausedAccumMs = 0L
        _state.value = if (s.mode == TimerMode.COUNTDOWN) {
            deadlineMs = now + s.totalMillis
            s.copy(remainMillis = s.totalMillis, elapsedMillis = 0L, running = true, finished = false)
        } else {
            deadlineMs = 0L
            s.copy(elapsedMillis = 0L, running = true, finished = false)
        }
        persistCurrentPhase()
        startTicker()
    }

    /**
     * 用户主动结束当前阶段。
     * 正向计时 / 不计时这是唯一结束方式；倒计时也可提前结束；休息中调用 = 跳过休息。
     */
    fun finishManually() {
        val s = _state.value
        if (!s.active || s.finished) return
        if (s.phase == FocusPhase.BREAK) {
            advanceToNextCycle()
        } else {
            completeFocusPhase()
        }
    }

    /** 放弃 / 退出：按当前已专注时长落库（completed = false） */
    fun stop() {
        persistCurrentPhase()
        stopTicker()
        _state.value = FocusState()
    }

    // ===== 内部状态机 =====

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val s = _state.value
                if (s.mode == TimerMode.COUNTDOWN) {
                    val remain = (deadlineMs - now).coerceAtLeast(0L)
                    _state.value = s.copy(
                        remainMillis = remain,
                        elapsedMillis = s.totalMillis - remain
                    )
                    if (remain <= 0L) {
                        // 倒计时归零：自动进入下一阶段
                        if (s.phase == FocusPhase.BREAK) {
                            breakAccumSec += (s.totalMillis / 1000L).toInt()
                            advanceToNextCycle()
                        } else {
                            completeFocusPhase()
                        }
                        break
                    }
                } else {
                    _state.value = s.copy(elapsedMillis = pausedAccumMs + (now - phaseStartMs))
                }
                val wall = System.currentTimeMillis()
                if (wall - lastPersistAt >= PERSIST_INTERVAL_MS) {
                    lastPersistAt = wall
                    persistCurrentPhase()
                }
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    /** 一轮「专注」结束：落库 → 决定进休息、进下一轮、还是全部完成 */
    private fun completeFocusPhase() {
        stopTicker()
        val s = _state.value
        persistFocus(completed = s.countedAsDone)

        val next = s.cycleIndex + 1
        val hasMore = next <= s.cycleTotal
        when {
            s.breakMillis > 0L && hasMore -> beginPhase(FocusPhase.BREAK, s.cycleIndex, s.breakMillis)
            hasMore -> beginPhase(FocusPhase.FOCUS, next, s.totalMillis)
            else -> finishAll()
        }
    }

    /** 休息结束 / 手动跳过休息：进入下一轮专注 */
    private fun advanceToNextCycle() {
        stopTicker()
        val s = _state.value
        if (s.phase == FocusPhase.BREAK) {
            breakAccumSec += (s.elapsedMillis / 1000L).toInt()
        }
        val next = s.cycleIndex + 1
        if (next <= s.cycleTotal) {
            beginPhase(FocusPhase.FOCUS, next, s.totalMillis)
        } else {
            finishAll()
        }
    }

    /** 开启一个新阶段（专注 / 休息） */
    private fun beginPhase(phase: FocusPhase, cycleIndex: Int, durationMillis: Long) {
        stopTicker()
        val now = SystemClock.elapsedRealtime()
        val s = _state.value
        phaseStartMs = now
        pausedAccumMs = 0L
        val dur = when {
            phase == FocusPhase.BREAK -> durationMillis
            s.mode == TimerMode.COUNTDOWN -> durationMillis
            else -> 0L
        }
        deadlineMs = if (dur > 0L) now + dur else 0L
        val isFocus = phase == FocusPhase.FOCUS

        _state.value = s.copy(
            phase = phase,
            running = true,
            finished = false,
            sessionId = if (isFocus) -1L else s.sessionId,
            remainMillis = dur,
            elapsedMillis = 0L,
            cycleIndex = cycleIndex
        )

        if (isFocus) {
            scope.launch {
                val id = Graph.repository.openSession(s.taskId, s.title, s.mode)
                _state.value = _state.value.copy(sessionId = id)
            }
        }
        startTicker()
    }

    /** 全部轮次完成 */
    private fun finishAll() {
        stopTicker()
        val s = _state.value
        _state.value = s.copy(
            running = false,
            finished = true,
            remainMillis = 0L,
            elapsedMillis = if (s.mode == TimerMode.COUNTDOWN) s.totalMillis else s.elapsedMillis,
            phase = FocusPhase.DONE
        )
        if (s.hideNextDay) {
            scope.launch { Graph.repository.hideTaskUntilTomorrow(s.taskId) }
        }
    }

    // ===== 持久化 =====

    /** 计时过程中的周期回写（不在这里判定 completed，收尾时才定） */
    private fun persistCurrentPhase() {
        val s = _state.value
        if (s.sessionId < 0L || s.phase != FocusPhase.FOCUS) return
        val id = s.sessionId
        val sec = s.elapsedSec
        val brk = breakAccumSec
        scope.launch { Graph.repository.closeSession(id, sec, completed = false, breakSec = brk) }
    }

    /** 一轮专注结束时的落库（此时才决定 completed） */
    private fun persistFocus(completed: Boolean) {
        val s = _state.value
        if (s.sessionId < 0L) return
        val id = s.sessionId
        val sec = s.elapsedSec
        val brk = breakAccumSec
        breakAccumSec = 0
        scope.launch { Graph.repository.closeSession(id, sec, completed, brk) }
    }
}
