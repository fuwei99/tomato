package com.tomato.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.tomato.app.ui.components.PieLegend
import com.tomato.app.ui.components.RecordButton
import com.tomato.app.ui.components.SegmentedControl
import com.tomato.app.ui.components.StatCard
import com.tomato.app.ui.components.StatCardHead
import com.tomato.app.ui.components.StatColumnsThree
import com.tomato.app.ui.components.StatColumnsTwo
import com.tomato.app.ui.data.CumulativeStats
import com.tomato.app.ui.data.DemoPieSlices
import com.tomato.app.ui.data.TodayStats
import com.tomato.app.ui.theme.MiniIcon
import com.tomato.app.ui.theme.StatsBgStops
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.TomatoTheme
import com.tomato.app.ui.theme.White

/**
 * 统计数据页：页面背景为青→白竖向渐变；卡片宽 342dp、间距 14dp、顶部 9dp、底部 14dp。
 */
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    var segment by remember { mutableIntStateOf(3) }   // 默认选中「自定义」

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
                StatColumnsThree(CumulativeStats)
            }

            // 当日专注
            StatCard {
                StatCardHead("当日专注", date = "2026-09-16") {
                    ArrowPair()
                }
                StatColumnsTwo(TodayStats)
            }

            // 专注时长分布
            StatCard {
                StatCardHead(
                    title = "专注时长分布",
                    date = "2026-04-17 ~ 2026-09-18",
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
                FocusPieChart(
                    slices = DemoPieSlices,
                    modifier = Modifier.padding(top = 4.dp)
                )
                RecordButton()
                PieLegend(DemoPieSlices, Modifier.padding(top = 10.dp))
            }

            // 本月专注时段分布
            StatCard {
                StatCardHead("本月专注时段分布", date = "2026年09月", teal = true) {
                    ArrowPair()
                }
                MonthBlank()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun StatsScreenPreview() {
    TomatoTheme { StatsScreen() }
}
