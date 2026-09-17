package com.tomato.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.data.Graph
import com.tomato.app.data.StatsSnapshot
import com.tomato.app.data.db.DayFocusRow
import com.tomato.app.ui.components.AppHeader
import com.tomato.app.ui.components.ArrowPair
import com.tomato.app.ui.components.BarChart
import com.tomato.app.ui.components.FocusPieChart
import com.tomato.app.ui.components.HourBars
import com.tomato.app.ui.components.IconBarChart
import com.tomato.app.ui.components.IconCalendarArrow
import com.tomato.app.ui.components.IconMedal
import com.tomato.app.ui.components.IconMoreVert
import com.tomato.app.ui.components.IconShare
import com.tomato.app.ui.components.IconTimer
import com.tomato.app.ui.components.MonthCalendarHeat
import com.tomato.app.ui.components.PieEmpty
import com.tomato.app.ui.components.PieLegend
import com.tomato.app.ui.components.RecordButton
import com.tomato.app.ui.components.SegmentedControl
import com.tomato.app.ui.components.StatCard
import com.tomato.app.ui.components.StatCardHead
import com.tomato.app.ui.components.StatColumnsThree
import com.tomato.app.ui.components.StatColumnsTwo
import com.tomato.app.ui.components.WeekDots
import com.tomato.app.ui.data.PieSlice
import com.tomato.app.ui.data.StatColumn
import com.tomato.app.ui.data.durationValues
import com.tomato.app.ui.data.formatHm
import com.tomato.app.ui.data.pieColor
import com.tomato.app.ui.data.startOfTodayMillis
import com.tomato.app.ui.data.todayDateLabel
import com.tomato.app.ui.data.todayMonthLabel
import com.tomato.app.ui.theme.InkMuted
import com.tomato.app.ui.theme.MiniIcon
import com.tomato.app.ui.theme.StatsBgStops
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.White
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

/**
 * 统计数据页：背景青→白竖向渐变；卡片宽 342dp、间距 14dp。
 * 所有图表数据都来自 Room 的 pomodoro_sessions 实时聚合，无任何假数据。
 *
 * 分段器「日 / 周 / 月 / 自定义」真实切换查询区间 —— 切换后以下三块同步刷新：
 *  - 专注时长分布（饼图）
 *  - 每日专注（柱状图）
 *  - 本周打卡（热力行）
 */

private val zone: ZoneId get() = ZoneId.systemDefault()

// ===== 区间解析 =====

/**
 * 把分段器下标解析成 [from, to) 毫秒区间。
 * 0 日 = 今天；1 周 = 本周一 0 点起 7 天；2 月 = 本月 1 号起至下月 1 号；3 自定义 = 近 30 天（含今天）。
 */
internal fun resolveRange(segment: Int, todayStart: Long): Pair<Long, Long> {
    val today = Instant.ofEpochMilli(todayStart).atZone(zone).toLocalDate()
    return when (segment) {
        0 -> todayStart to (todayStart + 86_400_000L)
        1 -> {
            val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val from = monday.atStartOfDay(zone).toInstant().toEpochMilli()
            from to (from + 7 * 86_400_000L)
        }
        2 -> {
            val first = today.withDayOfMonth(1)
            val from = first.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = first.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
            from to to
        }
        else -> todayStart - 29 * 86_400_000L to (todayStart + 86_400_000L)
    }
}

/** 卡片右上角的区间文案 */
internal fun rangeLabel(segment: Int): String {
    val today = LocalDate.now()
    return when (segment) {
        0 -> todayDateLabel()
        1 -> {
            val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val sunday = monday.plusDays(6)
            "${monday.monthValue}.${monday.dayOfMonth}-${sunday.monthValue}.${sunday.dayOfMonth}"
        }
        2 -> todayMonthLabel()
        else -> {
            val from = today.minusDays(29)
            "${from.monthValue}.${from.dayOfMonth}-${today.monthValue}.${today.dayOfMonth}"
        }
    }
}

/**
 * 把稀疏的日聚合结果补齐成「对齐到区间」的定长序列。
 * 返回 每日秒数（缺失补 0）与对应的横轴标签（按跨度抽稀，避免挤成一团）。
 */
internal fun buildDailySeries(
    rows: List<DayFocusRow>,
    range: Pair<Long, Long>
): Pair<List<Long>, List<String>> {
    val dayMs = 86_400_000L
    val span = (((range.second - range.first) + dayMs - 1) / dayMs).toInt().coerceIn(1, 400)
    val map = rows.associate { it.dayIndex to it.sec }
    val values = List(span) { map[it] ?: 0L }
    val start = Instant.ofEpochMilli(range.first).atZone(zone).toLocalDate()
    // 标签抽稀：柱子 ≤ 10 根全标，否则每 N 根标一个（保证首尾可见）
    val step = if (span <= 10) 1 else ((span + 6) / 7)
    val labels = List(span) { i ->
        if (i % step == 0 || i == span - 1) {
            val d = start.plusDays(i.toLong())
            if (span > 31) "${d.monthValue}/${d.dayOfMonth}" else d.dayOfMonth.toString()
        } else ""
    }
    return values to labels
}

/** 区间内有记录的天数（用于日均） */
internal fun activeDaysIn(rows: List<DayFocusRow>, fallback: Int): Int =
    rows.count { it.sec > 0 }.takeIf { it > 0 } ?: max(1, fallback)

/**
 * 统计数据页。
 */
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    var segment by remember { mutableIntStateOf(1) }   // 默认「周」

    val todayStart = remember { startOfTodayMillis() }
    val range = remember(segment, todayStart) { resolveRange(segment, todayStart) }

    val stats by remember { Graph.repository.stats(todayStart) }
        .collectAsState(initial = StatsSnapshot.Empty)

    // 区间内的按任务聚合（饼图）
    val rows by remember(range) { Graph.repository.taskBreakdown(range.first) }
        .collectAsState(initial = emptyList())

    // 区间内的按天聚合（柱状图 / 日均）
    val dayRows by remember(range) {
        Graph.repository.dailyFocusBetween(range.first, range.second)
    }.collectAsState(initial = emptyList())

    // 本周打卡（固定本周一~周日，与分段器无关）
    val weekRows by remember { Graph.repository.weekFocus() }
        .collectAsState(initial = emptyList())

    // 本月时段分布 + 本月日历
    val hourRows by remember { Graph.repository.hourlyFocusThisMonth() }
        .collectAsState(initial = emptyList())

    val monthRows by remember { Graph.repository.monthFocus() }
        .collectAsState(initial = emptyList())

    val slices = remember(rows) {
        val total = rows.sumOf { it.sec }.coerceAtLeast(1L)
        rows.take(8).mapIndexed { i, r ->
            PieSlice(
                name = r.title,
                time = formatHm(r.sec),
                percent = r.sec.toFloat() / total.toFloat(),
                color = pieColor(i)
            )
        }
    }

    val rangeSec = remember(rows) { rows.sumOf { it.sec } }
    val rangeActiveDays = remember(dayRows, stats) {
        activeDaysIn(dayRows, stats.activeDays)
    }

    val note = remember(rangeSec, rangeActiveDays) {
        if (rangeSec <= 0L) "" else "总 计 ${formatHm(rangeSec)}　日 均 ${rangeSec / 60L / rangeActiveDays} 分 钟"
    }

    val (barValues, barLabels) = remember(dayRows, range) {
        buildDailySeries(dayRows, range)
    }

    val weekDots = remember(weekRows) {
        val map = weekRows.associate { it.dayIndex to it.sec }
        List(7) { (map[it] ?: 0L) > 0L }
    }

    val hourMap = remember(hourRows) { hourRows.associate { it.hourIndex to it.sec } }
    val monthMap = remember(monthRows) { monthRows.associate { it.dayIndex + 1 to it.sec } }

    Column(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(StatsBgStops))
    ) {
        AppHeader(
            title = "统计数据",
            showStudyModePill = false,
            showPermissionText = false
        ) {
            IconBarChart(tint = White, iconSize = 20.dp)
            IconTimer(tint = White, iconSize = 20.dp)
            IconMedal(tint = White, iconSize = 20.dp)
            IconMoreVert(tint = White, iconSize = 20.dp)
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 9.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ① 累计专注（总账，不随分段器变化）
            StatCard {
                StatCardHead(
                    title = "累计专注",
                    afterTitle = {
                        IconCalendarArrow(
                            tint = MiniIcon,
                            iconSize = 16.dp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                ) {
                    IconShare(tint = MiniIcon, iconSize = 16.dp)
                }
                StatColumnsThree(
                    listOf(
                        StatColumn("专注时长", durationValues(stats.totalSec)),
                        StatColumn("完成番茄", listOf(stats.totalCount.toString() to "个")),
                        StatColumn(
                            "日均",
                            listOf((stats.totalSec / 60L / max(1, stats.activeDays)).toString() to "分")
                        )
                    )
                )
            }

            // ② 当日专注（今天，与分段器无关）
            StatCard {
                StatCardHead("当日专注", date = todayDateLabel()) {
                    ArrowPair()
                }
                StatColumnsTwo(
                    listOf(
                        StatColumn("专注时长", durationValues(stats.todaySec)),
                        StatColumn("完成番茄", listOf(stats.todayCount.toString() to "个"))
                    )
                )
                Box(Modifier.fillMaxWidth().padding(top = 8.dp), Alignment.Center) {
                    WeekDots(dots = weekDots)
                }
            }

            // ③ 专注时长分布（饼图，跟随分段器）
            StatCard {
                StatCardHead(
                    title = "专注时长分布",
                    date = rangeLabel(segment),
                    teal = true
                ) {
                    Text(
                        text = "分享",
                        fontSize = 12.sp,
                        color = TealPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    ArrowPair()
                }
                Box(Modifier.fillMaxWidth().padding(top = 10.dp), Alignment.Center) {
                    SegmentedControl(
                        options = listOf("日", "周", "月", "自定义"),
                        selectedIndex = segment,
                        onSelect = { segment = it }
                    )
                }
                if (slices.isEmpty()) {
                    PieEmpty(
                        modifier = Modifier.padding(top = 6.dp),
                        text = "该区间还没有专注记录"
                    )
                } else {
                    FocusPieChart(
                        slices = slices,
                        note = note,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    RecordButton()
                    PieLegend(slices, Modifier.padding(top = 10.dp))
                }
            }

            // ④ 每日专注（柱状图，跟随分段器）
            StatCard {
                StatCardHead(
                    title = "每日专注",
                    date = rangeLabel(segment),
                    teal = true
                ) {
                    Text(
                        text = "合计 ${formatHm(rangeSec)}",
                        fontSize = 11.sp,
                        color = TealPrimary
                    )
                }
                BarChart(
                    values = barValues,
                    labels = barLabels,
                    modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
                )
            }

            // ⑤ 本周打卡（固定本周，不随分段器变化）
            StatCard {
                StatCardHead("本周打卡", date = rangeLabel(1), teal = true) {
                    Text(
                        text = "${weekDots.count { it }} / 7 天",
                        fontSize = 11.sp,
                        color = TealPrimary
                    )
                }
                Box(Modifier.fillMaxWidth().padding(top = 10.dp), Alignment.Center) {
                    WeekDots(dots = weekDots, dotSize = 18.dp, spacing = 14.dp)
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("一", "二", "三", "四", "五", "六", "日").forEach { w ->
                        Text(
                            text = w,
                            fontSize = 9.sp,
                            color = InkMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ⑥ 本月专注时段分布（24 小时柱状图）
            StatCard {
                StatCardHead("本月专注时段分布", date = todayMonthLabel(), teal = true) {
                    ArrowPair()
                }
                HourBars(
                    hourSec = hourMap,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // ⑦ 本月专注日历（日历热力图）
            StatCard {
                StatCardHead(
                    title = "本月专注日历",
                    date = todayMonthLabel(),
                    teal = true
                ) {
                    ArrowPair()
                }
                MonthCalendarHeat(
                    daySec = monthMap,
                    year = LocalDate.now().year,
                    month = LocalDate.now().monthValue,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
