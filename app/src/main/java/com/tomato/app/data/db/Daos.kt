package com.tomato.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    /** 未完成任务：先按待办集、再按自定义排序 */
    @Query("SELECT * FROM tasks WHERE done = 0 ORDER BY listId, sortOrder, id")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun byId(id: Long): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Insert
    suspend fun insert(task: TaskEntity): Long
}

@Dao
interface TaskListDao {

    @Query("SELECT * FROM task_lists WHERE archived = 0 ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<TaskListEntity>>

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

    /** 计时过程中反复落库：更新已专注秒数 / 结束时间 / 是否完成 */
    @Query(
        "UPDATE pomodoro_sessions SET endedAt = :endedAt, focusSec = :focusSec," +
            " completed = :completed WHERE id = :id"
    )
    suspend fun finish(id: Long, endedAt: Long, focusSec: Int, completed: Boolean)

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

    /** 按任务聚合（用于统计页饼图） */
    @Query(
        "SELECT t.title AS title, t.styleKey AS styleKey, COALESCE(SUM(s.focusSec), 0) AS sec" +
            " FROM pomodoro_sessions s JOIN tasks t ON t.id = s.taskId" +
            " WHERE s.completed = 1 AND s.startedAt >= :from" +
            " GROUP BY s.taskId ORDER BY sec DESC"
    )
    fun groupByTaskSince(from: Long): Flow<List<TaskFocusRow>>
}

/** 按任务聚合的行（Room 直接映射查询结果列） */
data class TaskFocusRow(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "styleKey") val styleKey: String,
    @ColumnInfo(name = "sec") val sec: Long
)
