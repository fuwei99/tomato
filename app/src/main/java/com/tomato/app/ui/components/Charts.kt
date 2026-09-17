package com.tomato.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.theme.InkMuted
import com.tomato.app.ui.theme.InkSub
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.White

/**
 * 统计页的图表组件，全部 Canvas 手绘，不引入任何图表库。
 * 数据源统一为「每日专注秒数」列表，index 与调用方约定（今天在末位 / 周一在首位 / 1 号在首位）。
 */

// ===== 柱状图 =====

/**
 * 圆角柱状图。
 * [values] 每根柱子的值（秒），[labels] 与之等长（可为空则不画标签）。
 * 柱子高度按最大值归一，全 0 时画出「基线」空状态。
 */
@Composable
fun BarChart(
    values: List<Long>,
    modifier: Modifier = Modifier,
    labels: List<String> = emptyList(),
    highlightLast: Boolean = true,
    barColor: Color = TealPrimary,
    emptyColor: Color = Color(0xFFD6E4E2),
    height: Dp = 108.dp
) {
    val measurer = rememberTextMeasurer()
    val max = values.maxOrNull() ?: 0L

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (values.isEmpty()) return@Canvas

        val n = values.size
        val labelH = if (labels.isEmpty()) 0f else 14.sp.toPx()
        val chartH = size.height - labelH
        val slot = size.width / n
        val barW = (slot * 0.56f).coerceAtMost(14.dp.toPx())
        val radius = CornerRadius(barW / 2f, barW / 2f)

        values.forEachIndexed { i, v ->
            val ratio = if (max <= 0L) 0f else (v.toFloat() / max.toFloat()).coerceIn(0f, 1f)
            // 有意义的最小高度，避免 0 值看起来像「没渲染」
            val h = if (v <= 0L) 2.dp.toPx() else (ratio * (chartH - 4.dp.toPx())).coerceAtLeast(3.dp.toPx())
            val x = i * slot + (slot - barW) / 2f
            val isLast = highlightLast && i == n - 1
            drawRoundRect(
                color = if (v <= 0L) emptyColor else if (isLast) barColor else barColor.copy(alpha = 0.55f),
                topLeft = Offset(x, chartH - h),
                size = Size(barW, h),
                cornerRadius = radius
            )

            labels.getOrNull(i)?.let { text ->
                val layout = measurer.measure(text, TextStyle(fontSize = 8.5.sp, color = InkMuted))
                drawText(
                    textMeasurer = measurer,
                    text = text,
                    topLeft = Offset(
                        i * slot + (slot - layout.size.width) / 2f,
                        chartH + 3.dp.toPx()
                    ),
                    style = TextStyle(fontSize = 8.5.sp, color = InkMuted)
                )
            }
        }
    }
}

// ===== 周热力图 =====

/** 7 个小圆点：有记录填充 teal，无记录浅灰 */
@Composable
fun WeekDots(
    dots: List<Boolean>,
    modifier: Modifier = Modifier,
    dotSize: Dp = 9.dp,
    spacing: Dp = 7.dp,
    activeColor: Color = TealPrimary,
    inactiveColor: Color = Color(0xFFE0E4E6)
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { on ->
            Box(
                Modifier
                    .size(dotSize)
                    .background(if (on) activeColor else inactiveColor, RoundedCornerShape(50))
            )
        }
    }
}

/** 详情卡里「周热力图」整行：文字 + 7 圆点 */
@Composable
fun WeekHeatRow(
    dots: List<Boolean>,
    modifier: Modifier = Modifier,
    label: String = "周热力图",
    onClick: () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F8F8), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF37474F))
        Spacer(Modifier.weight(1f))
        WeekDots(dots = dots)
    }
}

// ===== 月历热力图 =====

/**
 * 本月日历热力图：7 列 × N 行，每天一个色块。
 * [daySec] 以「当月第几天（1-based）」为 key，缺失视为 0。
 */
@Composable
fun MonthCalendarHeat(
    daySec: Map<Int, Long>,
    year: Int,
    month: Int,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = 1,   // 1 = 周一（与 java.time DayOfWeek.MONDAY 一致）
    teal: Color = TealPrimary
) {
    val monthDays = java.time.YearMonth.of(year, month).lengthOfMonth()
    // 当月 1 号是周几（1=周一 … 7=周日）
    val offset = java.time.LocalDate.of(year, month, 1).dayOfWeek.value - firstDayOfWeek
    val lead = ((offset % 7) + 7) % 7
    val max = daySec.values.maxOrNull() ?: 0L

    val rows = mutableListOf<List<Int?>>()
    var cursor = 0
    val totalCells = lead + monthDays
    val rowCount = (totalCells + 6) / 7
    for (r in 0 until rowCount) {
        val row = mutableListOf<Int?>()
        for (c in 0 until 7) {
            val idx = r * 7 + c
            val day = idx - lead + 1
            row += if (day in 1..monthDays) day else null
            cursor++
        }
        rows += row
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        // 表头：一 二 三 四 五 六 日
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { w ->
                Text(
                    text = w,
                    fontSize = 9.sp,
                    color = InkMuted,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                row.forEach { day ->
                    Box(Modifier.weight(1f)) {
                        if (day == null) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.35f)
                            )
                        } else {
                            val sec = daySec[day] ?: 0L
                            val alpha = if (max <= 0L || sec <= 0L) 0f
                            else (0.28f + 0.72f * (sec.toFloat() / max.toFloat())).coerceIn(0.28f, 1f)
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.35f)
                                    .background(
                                        if (alpha <= 0f) Color(0xFFEEF2F2) else teal.copy(alpha = alpha),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 9.sp,
                                    color = if (alpha > 0.62f) White else InkSub
                                )
                            }
                        }
                    }
                }
            }
        }

        if (max <= 0L) {
            Text(
                text = "本月还没有专注记录",
                fontSize = 11.sp,
                color = InkMuted,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
        }
    }
}

// ===== 24 小时时段分布 =====

/**
 * 24 小时时段分布柱状图。
 * [hourSec] 以小时（0~23）为 key。
 */
@Composable
fun HourBars(
    hourSec: Map<Int, Long>,
    modifier: Modifier = Modifier,
    barColor: Color = TealPrimary,
    height: Dp = 92.dp
) {
    val measurer = rememberTextMeasurer()
    val max = (0..23).map { hourSec[it] ?: 0L }.maxOrNull() ?: 0L

    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val labelH = 13.sp.toPx()
            val chartH = size.height - labelH
            val slot = size.width / 24f
            val barW = (slot * 0.62f).coerceAtLeast(2.dp.toPx())

            for (h in 0..23) {
                val v = hourSec[h] ?: 0L
                val ratio = if (max <= 0L) 0f else (v.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                val bh = if (v <= 0L) 2.dp.toPx()
                else (ratio * (chartH - 4.dp.toPx())).coerceAtLeast(3.dp.toPx())
                drawRoundRect(
                    color = if (v <= 0L) Color(0xFFD6E4E2) else barColor,
                    topLeft = Offset(h * slot + (slot - barW) / 2f, chartH - bh),
                    size = Size(barW, bh),
                    cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                )
            }

            // 只标 0 / 6 / 12 / 18 点，避免拥挤
            listOf(0, 6, 12, 18).forEach { h ->
                val text = "${h}点"
                val layout = measurer.measure(text, TextStyle(fontSize = 8.5.sp, color = InkMuted))
                val cx = h * slot + slot / 2f
                drawText(
                    textMeasurer = measurer,
                    text = text,
                    topLeft = Offset((cx - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width), chartH + 2.dp.toPx()),
                    style = TextStyle(fontSize = 8.5.sp, color = InkMuted)
                )
            }
        }

        if (max <= 0L) {
            Text(
                text = "本月还没有专注记录",
                fontSize = 11.sp,
                color = InkMuted,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
        }
    }
}
