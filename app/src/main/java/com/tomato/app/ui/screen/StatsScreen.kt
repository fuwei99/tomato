package com.tomato.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.data.Graph
import com.tomato.app.data.StatsSnapshot
import com.tomato.app.ui.components.AppHeader
import com.tomato.app.ui.components.ArrowPair
import com.tomato.app.ui.components.FocusPieChart
import com.tomato.app.ui.components.IconBarChart
import com.tomato.app.ui.components.IconCalendarArrow
import com.tomato.app.ui.components.IconMedal
import com.tomato.app.ui.components.IconMoreVert
import com.tomato.app.ui.components.IconShare
import com.tomato.app.ui.components.IconTimer
import com.tomato.app.ui.components.MonthBlank
import com.tomato.app.ui.components.PieEmpty
import com.tomato.app.ui.components.PieLegend
import com.tomato.app.ui.components.RecordButton
import com.tomato.app.ui.components.SegmentedControl
import com.tomato.app.ui.components.StatCard
import com.tomato.app.ui.components.StatCardHead
import com.tomato.app.ui.components.StatColumnsThree
import com.tomato.app.ui.components.StatColumnsTwo
import com.tomato.app.ui.data.PieSlice
import com.tomato.app.ui.data.StatColumn
import com.tomato.app.ui.data.durationValues
import com.tomato.app.ui.data.formatHm
import com.tomato.app.ui.data.pieColor
import com.tomato.app.ui.data.startOfTodayMillis
import com.tomato.app.ui.data.todayDateLabel
import com.tomato.app.ui.data.todayMonthLabel
import com.tomato.app.ui.theme.MiniIcon
import com.tomato.app.ui.theme.StatsBgStops
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.White
import kotlin.math.max

/**
 * 统计数据页：页面背景为青→白竖向渐变；卡片宽 342dp、间距 14dp、顶部 9dp、底部 14dp。
 * 全部数字由 Room 的 pomodoro_sessions 实时聚合而来。
 */
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    var segment by remember { mutableIntStateOf(3) }   // 默认选中「自定义」

    val stats by remember { Graph.repository.stats(startOfTodayMillis()) }
        .collectAsState(initial = StatsSnapshot.Empty)

    // 分布按全部记录聚合
    val rows by remember { Graph.repository.taskBreakdown(0L) }
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

    val note = remember(rows, stats) {
        if (rows.isEmpty()) "" else {
            val total = rows.sumOf { it.sec }
            val days = max(1, stats.activeDays)
            "总 计 ${formatHm(total)}　日 均 ${total / 60L / days} 分 钟"
        }
    }

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
            // 累计专注
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

            // 当日专注
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
            }

            // 专注时长分布
            StatCard {
                StatCardHead(
                    title = "专注时长分布",
                    date = "全部记录",
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
                    PieEmpty(Modifier.padding(top = 6.dp))
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

            // 本月专注时段分布
            StatCard {
                StatCardHead("本月专注时段分布", date = todayMonthLabel(), teal = true) {
                    ArrowPair()
                }
                MonthBlank()
            }
        }
    }
}
