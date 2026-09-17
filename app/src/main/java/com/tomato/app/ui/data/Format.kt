package com.tomato.app.ui.data

import androidx.compose.ui.graphics.Color
import com.tomato.app.ui.theme.PieEnglish
import com.tomato.app.ui.theme.PieLock
import com.tomato.app.ui.theme.PieMath1000
import com.tomato.app.ui.theme.PieMathStudy
import com.tomato.app.ui.theme.PieMechanics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ===== 时间 =====

private val zone: ZoneId get() = ZoneId.systemDefault()

/** 当日 0 点（本地时区）*/
fun startOfTodayMillis(): Long =
    LocalDate.now().millisAtStartOfDay()

fun LocalDate.millisAtStartOfDay(): Long =
    atStartOfDay(zone).toInstant().toEpochMilli()

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val monthFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")

fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(dateFmt)

fun formatMonth(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(monthFmt)

fun todayDateLabel(): String = LocalDate.now().format(dateFmt)

fun todayMonthLabel(): String = LocalDate.now().format(monthFmt)

// ===== 时长 =====

/** 拆成 时 / 分 */
fun splitHoursMinutes(sec: Long): Pair<Long, Long> {
    val totalMin = sec / 60L
    return (totalMin / 60L) to (totalMin % 60L)
}

/** 统计页大数字列的值：[(时,"时"), (分,"分")] */
fun durationValues(sec: Long): List<Pair<String, String?>> {
    val (h, m) = splitHoursMinutes(sec)
    return listOf(h.toString() to "时", m.toString() to "分")
}

/** 图例 / 提示文案：36小时51分 */
fun formatHm(sec: Long): String {
    val (h, m) = splitHoursMinutes(sec)
    return if (h > 0) "${h}小时${m}分" else "${m}分钟"
}

// ===== 饼图配色（按任务顺序循环取用）=====

private val PiePalette = listOf(
    PieMath1000, PieMathStudy, PieEnglish, PieMechanics, PieLock
)

fun pieColor(index: Int): Color = PiePalette[index % PiePalette.size]
