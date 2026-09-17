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
    val minutes: Int,                     // 倒计时分钟数；正向/不计时也存预期值用于展示
    val styleKey: String = "leaf",        // 卡片渐变/插画样式，见 ui/data/DemoData.kt#TaskStyles
    val note: String = "",                // 任务备注（高级设置）
    val done: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),

    // ===== 类型与计时方式（存 ordinal，见 data/model/Enums.kt）=====
    val kind: Int = 0,                    // TaskKind
    val timerMode: Int = 0,               // TimerMode
    val durationPreset: Int = 0,          // DurationPreset

    // ===== 高级设置 =====
    val cycleTarget: Int = 1,             // 单次预期循环次数 K（>=1）：专注 N + 休息 M，共 K 轮
    val breakMinutes: Int = 5,            // 自定义休息分钟 M（仅倒计时 + K>1 时生效）
    val hideNextDay: Boolean = false,     // 配置：完成后第二天不再显示

    /** 运行期状态：隐藏到期时刻（次日 0 点），0 = 未隐藏。查询时按它过滤，无需回写 hideNextDay */
    val hiddenUntil: Long = 0L
)

/** 一次番茄（完成或中途放弃都落库，统计页从这里聚合） */
@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val taskTitle: String,
    val startedAt: Long,    // epoch millis
    val endedAt: Long,      // epoch millis
    val focusSec: Int,      // 实际专注秒数
    val completed: Boolean, // true = 有效专注（>=60s）
    val breakSec: Int = 0,  // 本轮搭配的休息秒数（仅统计用）
    val mode: Int = 0       // TimerMode.ordinal
)
