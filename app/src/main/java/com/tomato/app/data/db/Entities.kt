package com.tomato.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 待办集（番茄ToDo 的「待办集」页签） */
@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Long = 0xFF009186,
    val sortOrder: Int = 0,
    val archived: Boolean = false
)

/** 任务（属于某个待办集） */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val title: String,
    val minutes: Int,
    val styleKey: String = "leaf",   // 卡片渐变/插画样式，见 ui/data/DemoData.kt#TaskStyles
    val note: String = "",
    val done: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/** 一次番茄（完成或中途放弃都落库，统计页从这里聚合） */
@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val taskTitle: String,
    val startedAt: Long,   // epoch millis
    val endedAt: Long,     // epoch millis
    val focusSec: Int,     // 实际专注秒数
    val completed: Boolean // true=自然完成 false=中途放弃
)
