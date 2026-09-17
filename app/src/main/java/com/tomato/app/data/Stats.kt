package com.tomato.app.data

import com.tomato.app.data.db.DayFocusRow
import com.tomato.app.data.db.HourFocusRow
import com.tomato.app.data.db.PomodoroSessionEntity
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.db.TaskFocusRow
import com.tomato.app.data.db.TaskListEntity
import com.tomato.app.data.db.TaskTotalsRow
import com.tomato.app.data.db.TomatoDatabase
import com.tomato.app.data.model.DurationPreset
import com.tomato.app.data.model.TaskKind
import com.tomato.app.data.model.TimerMode
import com.tomato.app.ui.data.DemoSeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/** 统计页一屏所需数据（全部由 PomodoroSession 聚合而来） */
data class StatsSnapshot(
    val totalSec: Long = 0L,        // 累计专注秒数（仅有效专注）
    val totalCount: Int = 0,        // 累计完成番茄数
    val activeDays: Int = 0,        // 有专注记录的天数
    val todaySec: Long = 0L,        // 当日专注秒数
    val todayCount: Int = 0         // 当日完成番茄数
) {
    companion object {
        val Empty = StatsSnapshot()
    }
}

/** 待办集 + 其下的任务（待办集页按组渲染） */
data class TaskGroup(
    val list: TaskListEntity,
    val tasks: List<TaskEntity>
)

/**
 * 数据层门面：UI 只跟它打交道，不直接碰 DAO。
 */
class TomatoRepository internal constructor(private val db: TomatoDatabase) {

    private val taskDao = db.taskDao()
    private val listDao = db.taskListDao()
    private val sessionDao = db.sessionDao()

    // ===== 读取 =====

    /**
     * 未完成任务。
     * `now` 取一次即可 —— 「完成后第二天不再显示」的任务在次日重启/刷新列表后自然回归。
     */
    val tasks: Flow<List<TaskEntity>> =
        flow { emit(System.currentTimeMillis()) }
            .flatMapLatest { now -> taskDao.observeAll(now) }

    /** 待办集分组：待办集列表 × 任务，按 listId 归组 */
    val groups: Flow<List<TaskGroup>> =
        combine(tasks, listDao.observeAll()) { taskList, lists ->
            lists.map { list -> TaskGroup(list, taskList.filter { it.listId == list.id }) }
        }

    /** 待办集列表（新建任务选归属、移动任务选目标用） */
    val taskLists: Flow<List<TaskListEntity>> = listDao.observeAll()

    /** 统计聚合（from = 当日 0 点 epoch millis） */
    fun stats(from: Long): Flow<StatsSnapshot> =
        combine(
            sessionDao.totalFocusSec(),
            sessionDao.completedCount(0L),
            sessionDao.activeDays(),
            sessionDao.focusSecSince(from),
            sessionDao.completedCount(from)
        ) { totalSec: Long, totalCount: Int, days: Int, todaySec: Long, todayCount: Int ->
            StatsSnapshot(totalSec, totalCount, days, todaySec, todayCount)
        }

    /** 饼图数据：按任务聚合（from = 区间起点，0 = 全部） */
    fun taskBreakdown(from: Long): Flow<List<TaskFocusRow>> =
        sessionDao.groupByTaskSince(from)

    /** 单任务累计（详情卡大数字） */
    fun taskTotals(taskId: Long): Flow<TaskTotalsRow> = sessionDao.taskTotals(taskId)

    /** 近 N 天（含今天）每天的专注秒数：index 0 = N-1 天前，index N-1 = 今天 */
    fun dailyFocus(days: Int): Flow<List<DayFocusRow>> {
        val today = startOfToday()
        val from = today - (days - 1).coerceAtLeast(0) * DAY_MS
        return sessionDao.dailyFocus(from, today + DAY_MS)
    }

    /** 指定区间的每日专注（统计页 日/周/月/自定义 切换用） */
    fun dailyFocusBetween(from: Long, to: Long): Flow<List<DayFocusRow>> =
        sessionDao.dailyFocus(from, to)

    /** 本周（周一 0 点起）每天的专注秒数：index 0 = 周一 */
    fun weekFocus(): Flow<List<DayFocusRow>> {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant().toEpochMilli()
        return sessionDao.dailyFocus(monday, monday + 7 * DAY_MS)
    }

    /** 本月每天的专注秒数：index 0 = 1 号 */
    fun monthFocus(): Flow<List<DayFocusRow>> {
        val now = LocalDate.now()
        val first = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val next = now.withDayOfMonth(1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return sessionDao.dailyFocus(first, next)
    }

    /** 本月按小时聚合（0~23 时） */
    fun hourlyFocusThisMonth(): Flow<List<HourFocusRow>> {
        val first = LocalDate.now().withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return sessionDao.hourlyFocus(first)
        }

    // ===== 种子数据 =====

    suspend fun seedIfEmpty() {
        if (taskDao.count() == 0 && listDao.count() == 0) {
            listDao.insertAll(DemoSeed.lists)
            taskDao.insertAll(DemoSeed.tasks())
        }
    }

    // ===== 任务增删改 =====

    /** 新建任务：sortOrder 自动排到所属待办集末尾 */
    suspend fun insertTask(
        listId: Long,
        title: String,
        minutes: Int,
        kind: TaskKind,
        timerMode: TimerMode,
        preset: DurationPreset,
        cycleTarget: Int,
        breakMinutes: Int,
        hideNextDay: Boolean,
        note: String = "",
        styleKey: String = DemoSeed.nextStyleKey()
    ): Long {
        val order = taskDao.nextSortOrder(listId)
        return taskDao.insert(
            TaskEntity(
                listId = listId,
                title = title,
                minutes = minutes.coerceAtLeast(1),
                styleKey = styleKey,
                note = note,
                sortOrder = order,
                kind = kind.ordinal,
                timerMode = timerMode.ordinal,
                durationPreset = preset.ordinal,
                cycleTarget = cycleTarget.coerceAtLeast(1),
                breakMinutes = breakMinutes.coerceAtLeast(0),
                hideNextDay = hideNextDay
            )
        )
    }

    /** 编辑任务（保留 id / listId / sortOrder / hiddenUntil） */
    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateFull(
            id = task.id,
            title = task.title,
            minutes = task.minutes.coerceAtLeast(1),
            note = task.note,
            styleKey = task.styleKey,
            kind = task.kind,
            timerMode = task.timerMode,
            durationPreset = task.durationPreset,
            cycleTarget = task.cycleTarget.coerceAtLeast(1),
            breakMinutes = task.breakMinutes.coerceAtLeast(0),
            hideNextDay = task.hideNextDay
        )
    }

    /** 勾选 / 取消勾选完成 */
    suspend fun setDone(taskId: Long, done: Boolean) {
        taskDao.setDone(taskId, done)
        if (done) hideIfNeeded(taskId)
    }

    suspend fun deleteTask(taskId: Long) = taskDao.deleteById(taskId)

    /** 隐藏到明天 0 点 */
    suspend fun hideTaskUntilTomorrow(taskId: Long) {
        taskDao.hideUntil(taskId, startOfToday() + DAY_MS)
    }

    /** 若任务勾了「完成后第二天不再显示」，则隐藏 */
    suspend fun hideIfNeeded(taskId: Long) {
        val task = taskDao.byId(taskId) ?: return
        if (task.hideNextDay) hideTaskUntilTomorrow(taskId)
    }

    // ===== 排序 / 移动 =====

    /** 与同待办集内相邻任务交换位置（delta = -1 上移 / +1 下移） */
    private suspend fun swap(taskId: Long, delta: Int) {
        db.withTransaction {
            val task = taskDao.byId(taskId) ?: return@withTransaction
            val siblings = taskDao.byList(task.listId)
            val index = siblings.indexOfFirst { it.id == taskId }
            if (index < 0) return@withTransaction
            val target = index + delta
            if (target !in siblings.indices) return@withTransaction
            val other = siblings[target]
            // 按「索引」重写而不是交换原 sortOrder，避免历史脏数据导致换不动
            taskDao.reorder(task.id, target, task.listId)
            taskDao.reorder(other.id, index, other.listId)
        }
    }

    suspend fun moveTaskUp(taskId: Long) = swap(taskId, -1)

    suspend fun moveTaskDown(taskId: Long) = swap(taskId, +1)

    /** 置顶 / 置底 */
    suspend fun moveTaskToEdge(taskId: Long, toTop: Boolean) {
        db.withTransaction {
            val task = taskDao.byId(taskId) ?: return@withTransaction
            val siblings = taskDao.byList(task.listId)
            val index = siblings.indexOfFirst { it.id == taskId }
            if (index < 0) return@withTransaction
            val target = if (toTop) 0 else siblings.size - 1
            if (index == target) return@withTransaction
            if (toTop) {
                siblings.take(index).forEachIndexed { i, t -> taskDao.reorder(t.id, i + 1, t.listId) }
            } else {
                siblings.drop(index + 1).forEachIndexed { i, t ->
                    taskDao.reorder(t.id, index + i, t.listId)
                }
            }
            taskDao.reorder(task.id, target, task.listId)
        }
    }

    /** 移动到另一个待办集（追加到目标集末尾） */
    suspend fun moveTaskToList(taskId: Long, toListId: Long) {
        db.withTransaction {
            val task = taskDao.byId(taskId) ?: return@withTransaction
            if (task.listId == toListId) return@withTransaction
            val siblings = taskDao.byList(task.listId)
            val index = siblings.indexOfFirst { it.id == taskId }
            if (index >= 0) {
                // 原集合中排在它后面的任务各前移一位
                siblings.drop(index + 1).forEachIndexed { i, t ->
                    taskDao.reorder(t.id, index + i, t.listId)
                }
            }
            taskDao.reorder(taskId, taskDao.nextSortOrder(toListId), toListId)
        }
    }

    /** 同待办集内的任务 id 顺序（判断能否上移 / 下移） */
    suspend fun siblingIds(taskId: Long): List<Long> {
        val task = taskDao.byId(taskId) ?: return emptyList()
        return taskDao.byList(task.listId).map { it.id }
    }

    // ===== 番茄会话 =====

    /** 开始专注：先插一条「未完成」记录，崩溃/被杀也留痕 */
    suspend fun openSession(
        taskId: Long,
        taskTitle: String,
        mode: TimerMode = TimerMode.COUNTDOWN
    ): Long {
        val now = System.currentTimeMillis()
        return sessionDao.insert(
            PomodoroSessionEntity(
                taskId = taskId,
                taskTitle = taskTitle,
                startedAt = now,
                endedAt = now,
                focusSec = 0,
                completed = false,
                breakSec = 0,
                mode = mode.ordinal
            )
        )
    }

    /** 计时中 / 暂停 / 放弃 / 完成时回写 */
    suspend fun closeSession(
        sessionId: Long,
        focusSec: Int,
        completed: Boolean,
        breakSec: Int = 0
    ) {
        sessionDao.finish(
            id = sessionId,
            endedAt = System.currentTimeMillis(),
            focusSec = focusSec,
            breakSec = breakSec,
            completed = completed
        )
    }

    // ===== 时间工具 =====

    /** 当日 0 点 epoch millis */
    fun startOfToday(): Long =
        LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()

    companion object {
        const val DAY_MS = 86_400_000L

        private val zone: ZoneId get() = ZoneId.systemDefault()
    }
}
