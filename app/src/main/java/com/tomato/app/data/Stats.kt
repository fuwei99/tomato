package com.tomato.app.data

import com.tomato.app.data.db.PomodoroSessionEntity
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.db.TaskFocusRow
import com.tomato.app.data.db.TaskListEntity
import com.tomato.app.data.db.TomatoDatabase
import com.tomato.app.ui.data.DemoSeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 统计页一屏所需数据（全部由 PomodoroSession 聚合而来） */
data class StatsSnapshot(
    val totalSec: Long = 0L,        // 累计专注秒数（仅自然完成）
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

    val tasks: Flow<List<TaskEntity>> = taskDao.observeAll()

    /** 待办集分组：待办集列表 × 任务，按 listId 归组 */
    val groups: Flow<List<TaskGroup>> =
        combine(taskDao.observeAll(), listDao.observeAll()) { tasks, lists ->
            lists.map { list -> TaskGroup(list, tasks.filter { it.listId == list.id }) }
        }

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

    /** 饼图数据：按任务聚合 */
    fun taskBreakdown(from: Long): Flow<List<TaskFocusRow>> =
        sessionDao.groupByTaskSince(from)

    // ===== 种子数据 =====

    suspend fun seedIfEmpty() {
        if (taskDao.count() == 0 && listDao.count() == 0) {
            listDao.insertAll(DemoSeed.lists)
            taskDao.insertAll(DemoSeed.tasks())
        }
    }

    // ===== 番茄会话 =====

    /** 开始专注：先插一条「未完成」记录，崩溃/被杀也留痕 */
    suspend fun openSession(taskId: Long, taskTitle: String): Long {
        val now = System.currentTimeMillis()
        return sessionDao.insert(
            PomodoroSessionEntity(
                taskId = taskId,
                taskTitle = taskTitle,
                startedAt = now,
                endedAt = now,
                focusSec = 0,
                completed = false
            )
        )
    }

    /** 计时中 / 暂停 / 放弃 / 完成时回写 */
    suspend fun closeSession(sessionId: Long, focusSec: Int, completed: Boolean) {
        sessionDao.finish(
            id = sessionId,
            endedAt = System.currentTimeMillis(),
            focusSec = focusSec,
            completed = completed
        )
    }
}
