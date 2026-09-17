package com.tomato.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.data.PieSlice
import com.tomato.app.ui.data.StatColumn
import com.tomato.app.ui.theme.InkLabel
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.InkMuted
import com.tomato.app.ui.theme.InkNumber
import com.tomato.app.ui.theme.InkSub
import com.tomato.app.ui.theme.PieLabelLine
import com.tomato.app.ui.theme.PieLabelText
import com.tomato.app.ui.theme.PieNote
import com.tomato.app.ui.theme.TealButtonBg
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.TealPrimaryDark
import com.tomato.app.ui.theme.TealSegmentLine
import com.tomato.app.ui.theme.TealSegmentOff
import com.tomato.app.ui.theme.TealSegmentOn
import com.tomato.app.ui.theme.White

/**
 * 统计页卡片：宽 342dp（左右边距 9dp）、圆角 12dp、padding(8,9,9,9)、1dp 阴影。
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(1.dp, shape)
            .clip(shape)
            .background(White)
            .padding(start = 9.dp, end = 9.dp, top = 8.dp, bottom = 9.dp),
        content = content
    )
}

/** 卡片标题行：14sp bold + 可选日期 + 右侧操作区 */
@Composable
fun StatCardHead(
    title: String,
    date: String? = null,
    teal: Boolean = false,
    afterTitle: @Composable RowScope.() -> Unit = {},
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (teal) TealPrimaryDark else InkMain
        )
        afterTitle()
        date?.let {
            Text(
                text = it,
                fontSize = 11.sp,
                color = if (teal) TealPrimary else InkSub,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

/** 左右箭头（统计卡右上角切换日期） */
@Composable
fun ArrowPair(color: Color = TealPrimary) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconChevronLeft(tint = color, iconSize = 13.dp)
        IconChevronRight(tint = color, iconSize = 13.dp)
    }
}

// ===== 大数字 =====

@Composable
private fun StatColumnItem(column: StatColumn, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = column.label,
            fontSize = 9.5.sp,
            color = InkLabel,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            column.values.forEach { (value, unit) ->
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    color = InkNumber,
                    letterSpacing = (-0.6).sp
                )
                unit?.let {
                    Text(
                        text = it,
                        fontSize = 9.sp,
                        color = InkNumber,
                        modifier = Modifier.padding(start = 1.dp, bottom = 1.dp)
                    )
                }
            }
        }
    }
}

/** 累计专注：三等分（中间列 flex 1.22） */
@Composable
fun StatColumnsThree(columns: List<StatColumn>) {
    Row(Modifier.fillMaxWidth().padding(top = 5.dp)) {
        columns.forEachIndexed { i, c ->
            StatColumnItem(c, Modifier.weight(if (i == 1) 1.22f else 1f))
        }
    }
}

/** 当日专注：两端对齐 + 左右 20dp 内边距 */
@Composable
fun StatColumnsTwo(columns: List<StatColumn>) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        columns.forEach { StatColumnItem(it) }
    }
}

// ===== 分段器（日/周/月/自定义）=====

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {}
) {
    val shape = RoundedCornerShape(13.dp)
    Row(
        modifier
            .height(IntrinsicSize.Min)
            .border(1.dp, TealSegmentLine, shape)
            .clip(shape)
    ) {
        options.forEachIndexed { i, text ->
            if (i > 0) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(TealSegmentLine)
                )
            }
            Box(
                Modifier
                    .background(if (i == selectedIndex) TealSegmentOn else Color.Transparent)
                    .padding(horizontal = 13.dp, vertical = 3.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 11.sp,
                    color = if (i == selectedIndex) White else TealSegmentOff
                )
            }
        }
    }
}

// ===== 饼图 =====

/** 饼图设计稿：viewBox 322×196，圆心 (161,88)，半径 71。起点 −90°，顺时针。 */
@Composable
fun FocusPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    note: String = "总 计 53 小时 54 分 钟　日 均 21 分 钟"
) {
    val measurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(322f / 196f)
    ) {
        val k = size.width / 322f
        val cx = 161f * k
        val cy = 88f * k
        val r = 71f * k

        var angle = -90f
        slices.forEach { s ->
            val sweep = s.percent * 360f
            drawArc(
                color = s.color,
                startAngle = angle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2)
            )
            angle += sweep
        }

        // 引线（坐标来自设计稿）
        listOf(
            Offset(213f, 132f) to Offset(231f, 146f),
            Offset(89f, 54f) to Offset(101f, 70f),
            Offset(119f, 24f) to Offset(128f, 36f)
        ).forEach { (a, b) ->
            drawLine(
                color = PieLabelLine,
                start = Offset(a.x * k, a.y * k),
                end = Offset(b.x * k, b.y * k),
                strokeWidth = 1f * k
            )
        }

        // 标签
        fun label(
            text: String,
            x: Float,
            y: Float,
            fontSize: Float = 11f,
            color: Color = PieLabelText,
            centered: Boolean = false
        ) {
            val style = TextStyle(fontSize = fontSize.sp, color = color)
            val layout = measurer.measure(text, style)
            val left = if (centered) x * k - layout.size.width / 2f else x * k
            drawText(
                textMeasurer = measurer,
                text = text,
                topLeft = Offset(left, y * k - fontSize * 0.80f * k),
                style = style
            )
        }

        label("数学1000题", 186f, 122f)
        label("36小时51分", 235f, 150f)
        label("13小时8分", 26f, 46f)
        label("数学学习", 103f, 76f)
        label("2小时0分", 48f, 18f)
        label("英语阅读练习-翻译-作文", 122f, 44f, fontSize = 10f)
        label("机械学习", 140f, 60f, fontSize = 10f)
        label(note, 161f, 184f, color = PieNote, centered = true)
    }
}

// ===== 图例 =====

@Composable
fun PieLegend(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slices.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { LegendItem(it, Modifier.weight(1f)) }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RowScope.LegendItem(slice: PieSlice, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .background(slice.color, RoundedCornerShape(50))
        )
        Column(Modifier.weight(1f).padding(start = 6.dp)) {
            Text(
                text = slice.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = slice.time,
                fontSize = 9.5.sp,
                color = InkMuted,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
        Text(
            text = "${(slice.percent * 1000).toInt() / 10f}%",
            fontSize = 11.sp,
            color = InkMain,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/** 「查看专注记录」按钮 */
@Composable
fun RecordButton(text: String = "查看专注记录", onClick: () -> Unit = {}) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .background(TealButtonBg, RoundedCornerShape(50))
                .padding(horizontal = 22.dp, vertical = 5.dp)
        ) {
            Text(text = text, fontSize = 11.sp, color = TealPrimaryDark)
        }
    }
}

/** 月度卡底部的空白区（30dp） */
@Composable
fun MonthBlank() {
    Box(Modifier.fillMaxWidth().height(30.dp))
}
