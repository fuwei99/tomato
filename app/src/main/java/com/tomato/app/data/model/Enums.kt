package com.tomato.app.data.model

/**
 * 任务类型 / 计时方式 / 时长预设。
 *
 * 持久化策略：存 `ordinal`（Int），Entity 字段类型就是 Int。
 * 这样不需要写 @TypeConverter，Room + KSP 零风险；UI 侧统一走 `from(v)` 转换，
 * 并且 `from` 对越界值做了兜底，读到脏数据不会崩。
 */

/** 任务类型：普通番茄钟 / 定目标 / 养习惯 */
enum class TaskKind(val label: String) {
    POMODORO("普通番茄钟"),
    GOAL("定目标"),
    HABIT("养习惯");

    companion object {
        fun from(v: Int): TaskKind = entries.getOrElse(v) { POMODORO }
    }
}

/** 计时方式：倒计时 / 正向计时 / 不计时 */
enum class TimerMode(val label: String) {
    COUNTDOWN("倒计时"),
    STOPWATCH("正向计时"),
    NONE("不计时");

    /** 需要用户手动结束的模式（正向计时、不计时没有终点） */
    val manualStopOnly: Boolean get() = this != COUNTDOWN

    companion object {
        fun from(v: Int): TimerMode = entries.getOrElse(v) { COUNTDOWN }
    }
}

/** 时长预设：25 / 35 / 自定义 */
enum class DurationPreset(val label: String, val minutes: Int) {
    M25("25分钟", 25),
    M35("35分钟", 35),
    CUSTOM("自定义", 0);

    companion object {
        fun from(v: Int): DurationPreset = entries.getOrElse(v) { M25 }

        /** 已有 minutes 反推预设（编辑任务时预填选项） */
        fun of(minutes: Int): DurationPreset = when (minutes) {
            25 -> M25
            35 -> M35
            else -> CUSTOM
        }
    }
}
