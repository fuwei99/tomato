package com.tomato.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    /**
     * 未完成任务。
     * `hiddenUntil` 是「完成后第二天不再显示」的运行期状态：到点自动重新出现，无需回写配置。
     */
    @Query(
        "SELECT * FROM tasks WHERE done = 0 AND (hiddenUntil = 0 OR hiddenUntil <= :now)" +
            " ORDER BY listId, sortOrder, id"
    )
    fun observeAll(now: Long): Flow<List<TaskEntity>>

    /** 不过滤隐藏（内部用：排序、计数） */
    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY sortOrder, id")
    suspend fun byList(listId: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun byId(id: Long): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM tasks WHERE listId = :listId")
    suspend fun nextSortOrder(listId: Long): Int

    @Insert
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Insert
    suspend fun insert(task: TaskEntity): Long

    /** 编辑：整行覆盖（不动 id / listId / sortOrder / hiddenUntil） */
    @Query(
        "UPDATE tasks SET title = :title, minutes = :minutes, note = :note, styleKey = :styleKey," +
            " kind = :kind, timerMode = :timerMode, durationPreset = :durationPreset," +
            " cycleTarget = :cycleTarget, breakMinutes = :breakMinutes, hideNextDay = :hideNextDay" +
            " WHERE id = :id"
    )
    suspend fun updateFull(
        id: Long,
        title: String,
        minutes: Int,
        note: String,
        styleKey: String,
        kind: Int,
        timerMode: Int,
        durationPreset: Int,
        cycleTarget: Int,
        breakMinutes: Int,
        hideNextDay: Boolean
    )

    /** 隐藏到指定时刻（「完成后第二天不再显示」勾选时，完成一轮后调用） */
    @Query("UPDATE tasks SET hiddenUntil = :until WHERE id = :id")
    suspend fun hideUntil(id: Long, until: Long)

    /** 重排 / 移动：改排序位 + 归属待办集 */
    @Query("UPDATE tasks SET sortOrder = :order, listId = :listId WHERE id = :id")
    suspend fun reorder(id: Long, order: Int, listId: Long)

    /** 勾选 / 取消勾选完成 */
    @Query("UPDATE tasks SET done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface TaskListDao {

    @Query("SELECT * FROM task_lists WHERE archived = 0 ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE archived = 0 ORDER BY sortOrder, id")
    suspend fun all(): List<TaskListEntity>

    @Query("SELECT COUNT(*) FROM task_lists")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(lists: List<TaskListEntity>)
}

@Dao
interface SessionDao {

    @Query("SELECT * FROM pomodoro_sessions ORDER BY endedAt DESC")
    fun observeAll(): Flow<List<PomodoroSessionEntity>>

    @Insert
    suspend fun insert(session: PomodoroSessionEntity): Long

    @Query(
        "UPDATE pomodoro_sessions SET endedAt = :endedAt, focusSec = :focusSec," +
            " breakSec = :breakSec, completed = :completed WHERE id = :id"
    )
    suspend fun finish(id: Long, endedAt: Long, focusSec: Int, breakSec: Int, completed: Boolean)

    // ===== 统计聚合 =====

    @Query("SELECT COALESCE(SUM(focusSec), 0) FROM pomodoro_sessions WHERE completed = 1")
    fun totalFocusSec(): Flow<Long>

    @Query(
        "SELECT COUNT(*) FROM pomodoro_sessions WHERE completed = 1" +
            " AND (:from <= 0 OR startedAt >= :from)"
    )
    fun completedCount(from: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT startedAt / 86400000) FROM pomodoro_sessions WHERE completed = 1")
    fun activeDays(): Flow<Int>

    @Query(
        "SELECT COALESCE(SUM(focusSec), 0) FROM pomodoro_sessions" +
            " WHERE completed = 1 AND startedAt >= :from"
    )
    fun focusSecSince(from: Long): Flow<Long>

    /** 按任务聚合（统计页饼图） */
    @Query(
        "SELECT t.title AS title, t.styleKey AS styleKey, COALESCE(SUM(s.focusSec), 0) AS sec" +
            " FROM pomodoro_sessions s JOIN tasks t ON t.id = s.taskId" +
            " WHERE s.completed = 1 AND s.startedAt >= :from" +
            " GROUP BY s.taskId ORDER BY sec DESC"
    )
    fun groupByTaskSince(from: Long): Flow<List<TaskFocusRow>>

    /**
     * 按天聚合（周热力图 / 日历热力图 / 柱状图共用）。
     * dayIndex = 距 :from 的天数；:from 必须取本地 0 点，且 :from <= startedAt。
     */
    @Query(
        "SELECT CAST((startedAt - :from) / 86400000 AS INTEGER) AS dayIndex," +
            " COALESCE(SUM(focusSec), 0) AS sec" +
            " FROM pomodoro_sessions WHERE completed = 1 AND startedAt >= :from AND startedAt < :to" +
            " GROUP BY dayIndex ORDER BY dayIndex"
    )
    fun dailyFocus(from: Long, to: Long): Flow<List<DayFocusRow>>

    /** 按小时聚合（本月专注时段分布，0~23） */
    @Query(
        "SELECT CAST(((startedAt - :from) / 3600000) % 24 AS INTEGER) AS hourIndex," +
            " COALESCE(SUM(focusSec), 0) AS sec" +
            " FROM pomodoro_sessions WHERE completed = 1 AND startedAt >= :from" +
            " GROUP BY hourIndex ORDER BY hourIndex"
    )
    fun hourlyFocus(from: Long): Flow<List<HourFocusRow>>

    /** 单任务累计（详情卡「累计专注」大数字） */
    @Query(
        "SELECT COUNT(*) AS count, COALESCE(SUM(focusSec), 0) AS sec" +
            " FROM pomodoro_sessions WHERE completed = 1 AND taskId = :taskId"
    )
    fun taskTotals(taskId: Long): Flow<TaskTotalsRow>
}

// ===== 查询结果 DTO（@ColumnInfo 必须与 SQL 别名一致）=====

/** 按任务聚合的行 */
data class TaskFocusRow(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "styleKey") val styleKey: String,
    @ColumnInfo(name = "sec") val sec: Long
)

/** 按天聚合的行 */
data class DayFocusRow(
    @ColumnInfo(name = "dayIndex") val dayIndex: Int,
    @ColumnInfo(name = "sec") val sec: Long
)

/** 按小时聚合的行 */
data class HourFocusRow(
    @ColumnInfo(name = "hourIndex") val hourIndex: Int,
    @ColumnInfo(name = "sec") val sec: Long
)

/** 单任务累计 */
data class TaskTotalsRow(
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "sec") val sec: Long
) {
    companion object {
        val Empty = TaskTotalsRow(0, 0L)
    }
}
