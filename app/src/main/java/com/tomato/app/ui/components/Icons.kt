package com.tomato.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 全部图标用 Canvas 手绘（24×24 坐标系统），不依赖 material-icons-extended，
 * 好处是形状与番茄TODO 截图一致（例如"待办"是两条横线、"待办集"是三条横线）。
 */
@Composable
fun CanvasIcon(
    modifier: Modifier = Modifier,
    tint: Color,
    iconSize: Dp = 24.dp,
    content: DrawScope.(s: Float) -> Unit
) {
    Canvas(modifier = modifier.size(iconSize)) {
        content(size.width / 24f)
    }
}

// ===== 底部导航 =====

/** 待办：上短下长两条横线 */
@Composable
fun IconTodo(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val w = 3.2f * s
        drawLine(tint, Offset(4f * s, 8.8f * s), Offset(13.5f * s, 8.8f * s), w, StrokeCap.Round)
        drawLine(tint, Offset(4f * s, 15.6f * s), Offset(20f * s, 15.6f * s), w, StrokeCap.Round)
    }
}

/** 待办集：三条递进横线 */
@Composable
fun IconChecklist(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val w = 3f * s
        drawLine(tint, Offset(4f * s, 6f * s), Offset(11f * s, 6f * s), w, StrokeCap.Round)
        drawLine(tint, Offset(4f * s, 12f * s), Offset(15.5f * s, 12f * s), w, StrokeCap.Round)
        drawLine(tint, Offset(4f * s, 18f * s), Offset(20f * s, 18f * s), w, StrokeCap.Round)
    }
}

/** 锁机 */
@Composable
fun IconLock(
    modifier: Modifier = Modifier,
    tint: Color,
    holeColor: Color = Color.White,
    iconSize: Dp = 24.dp
) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawRoundRect(
            tint, Offset(5f * s, 10.5f * s), Size(14f * s, 10f * s),
            CornerRadius(2.4f * s, 2.4f * s)
        )
        drawArc(
            tint, 180f, 180f, useCenter = false,
            topLeft = Offset(8.4f * s, 4.4f * s), size = Size(7.2f * s, 6.2f * s),
            style = Stroke(width = 2.4f * s)
        )
        drawLine(tint, Offset(8.4f * s, 7.6f * s), Offset(8.4f * s, 11f * s), 2.4f * s, StrokeCap.Round)
        drawLine(tint, Offset(15.6f * s, 7.6f * s), Offset(15.6f * s, 11f * s), 2.4f * s, StrokeCap.Round)
        drawCircle(holeColor, 1.7f * s, Offset(12f * s, 14.6f * s))
        drawRoundRect(
            holeColor, Offset(11.2f * s, 15.2f * s), Size(1.6f * s, 2.6f * s),
            CornerRadius(0.8f * s, 0.8f * s)
        )
    }
}

/** 统计数据：3/4 饼 + 右上小楔形 */
@Composable
fun IconPie(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawArc(
            tint, 0f, 270f, useCenter = true,
            topLeft = Offset(3f * s, 3f * s), size = Size(18f * s, 18f * s)
        )
        drawArc(
            tint, 268f, 44f, useCenter = true,
            topLeft = Offset(2f * s, 2f * s), size = Size(20f * s, 20f * s)
        )
    }
}

/** 我的 */
@Composable
fun IconPerson(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawCircle(tint, 4.1f * s, Offset(12f * s, 7.6f * s))
        drawArc(
            tint, 0f, 180f, useCenter = true,
            topLeft = Offset(4.4f * s, 13.4f * s), size = Size(15.2f * s, 7.4f * s)
        )
    }
}

// ===== 头部图标 =====

/** 均衡器：三根竖条 */
@Composable
fun IconBarChart(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawRoundRect(tint, Offset(4f * s, 12f * s), Size(3.6f * s, 8f * s), CornerRadius(1.8f * s))
        drawRoundRect(tint, Offset(10.2f * s, 4f * s), Size(3.6f * s, 16f * s), CornerRadius(1.8f * s))
        drawRoundRect(tint, Offset(16.4f * s, 8.5f * s), Size(3.6f * s, 11.5f * s), CornerRadius(1.8f * s))
    }
}

/** 秒表 */
@Composable
fun IconTimer(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawLine(tint, Offset(10f * s, 2f * s), Offset(14f * s, 2f * s), 2.2f * s, StrokeCap.Round)
        drawCircle(tint, 8f * s, Offset(12f * s, 13.5f * s), style = Stroke(width = 2.2f * s))
        drawLine(tint, Offset(12f * s, 13.5f * s), Offset(12f * s, 8.8f * s), 2.2f * s, StrokeCap.Round)
    }
}

/** 加号 */
@Composable
fun IconAdd(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawLine(tint, Offset(12f * s, 4.5f * s), Offset(12f * s, 19.5f * s), 2.2f * s, StrokeCap.Round)
        drawLine(tint, Offset(4.5f * s, 12f * s), Offset(19.5f * s, 12f * s), 2.2f * s, StrokeCap.Round)
    }
}

/** 双加号（待办集头部） */
@Composable
fun IconAddPlus(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawLine(tint, Offset(10f * s, 7.5f * s), Offset(10f * s, 18.5f * s), 2.2f * s, StrokeCap.Round)
        drawLine(tint, Offset(4.5f * s, 13f * s), Offset(15.5f * s, 13f * s), 2.2f * s, StrokeCap.Round)
        drawLine(tint, Offset(18.5f * s, 3f * s), Offset(18.5f * s, 9f * s), 1.9f * s, StrokeCap.Round)
        drawLine(tint, Offset(15.5f * s, 6f * s), Offset(21.5f * s, 6f * s), 1.9f * s, StrokeCap.Round)
    }
}

/** 竖三点 */
@Composable
fun IconMoreVert(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawCircle(tint, 2f * s, Offset(12f * s, 4.6f * s))
        drawCircle(tint, 2f * s, Offset(12f * s, 12f * s))
        drawCircle(tint, 2f * s, Offset(12f * s, 19.4f * s))
    }
}

/** 奖牌（统计页头部） */
@Composable
fun IconMedal(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawCircle(tint, 5.6f * s, Offset(12f * s, 9f * s))
        val ribbon = Path().apply {
            moveTo(8.8f * s, 13.6f * s)
            lineTo(6.4f * s, 21f * s)
            lineTo(12f * s, 18.2f * s)
            lineTo(17.6f * s, 21f * s)
            lineTo(15.2f * s, 13.6f * s)
            close()
        }
        drawPath(ribbon, tint)
        drawCircle(Color.White, 2.4f * s, Offset(12f * s, 9f * s))
    }
}

/** 分享（三圆连线） */
@Composable
fun IconShare(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawCircle(tint, 2.8f * s, Offset(17.5f * s, 5.5f * s))
        drawCircle(tint, 2.8f * s, Offset(6.5f * s, 12f * s))
        drawCircle(tint, 2.8f * s, Offset(17.5f * s, 18.5f * s))
        drawLine(tint, Offset(9f * s, 10.6f * s), Offset(15.2f * s, 6.9f * s), 1.8f * s, StrokeCap.Round)
        drawLine(tint, Offset(9f * s, 13.4f * s), Offset(15.2f * s, 17.1f * s), 1.8f * s, StrokeCap.Round)
    }
}

/** 日历 + 右箭头 */
@Composable
fun IconCalendarArrow(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val st = Stroke(width = 1.9f * s)
        drawRoundRect(
            tint, Offset(3.5f * s, 5f * s), Size(17f * s, 15.5f * s),
            CornerRadius(2.2f * s, 2.2f * s), style = st
        )
        drawLine(tint, Offset(3.5f * s, 9.6f * s), Offset(20.5f * s, 9.6f * s), 1.9f * s)
        drawLine(tint, Offset(8f * s, 3f * s), Offset(8f * s, 7f * s), 1.9f * s, StrokeCap.Round)
        drawLine(tint, Offset(16f * s, 3f * s), Offset(16f * s, 7f * s), 1.9f * s, StrokeCap.Round)
        val arrow = Path().apply {
            moveTo(8.5f * s, 14.5f * s)
            lineTo(14.5f * s, 14.5f * s)
            moveTo(12.5f * s, 12.5f * s)
            lineTo(14.5f * s, 14.5f * s)
            lineTo(12.5f * s, 16.5f * s)
        }
        drawPath(arrow, tint, style = Stroke(1.6f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** 齿轮（列表头设置） */
@Composable
fun IconSettings(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawCircle(tint, 5.6f * s, Offset(12f * s, 12f * s), style = Stroke(width = 3f * s))
        val teeth = listOf(
            Offset(12f * s, 3.5f * s) to Offset(12f * s, 6.5f * s),
            Offset(12f * s, 17.5f * s) to Offset(12f * s, 20.5f * s),
            Offset(3.5f * s, 12f * s) to Offset(6.5f * s, 12f * s),
            Offset(17.5f * s, 12f * s) to Offset(20.5f * s, 12f * s),
            Offset(6f * s, 6f * s) to Offset(8.1f * s, 8.1f * s),
            Offset(15.9f * s, 15.9f * s) to Offset(18f * s, 18f * s),
            Offset(18f * s, 6f * s) to Offset(15.9f * s, 8.1f * s),
            Offset(8.1f * s, 15.9f * s) to Offset(6f * s, 18f * s)
        )
        teeth.forEach { (a, b) -> drawLine(tint, a, b, 2.6f * s, StrokeCap.Round) }
    }
}

// ===== 箭头 =====

@Composable
fun IconChevronLeft(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val p = Path().apply {
            moveTo(15.5f * s, 4f * s)
            lineTo(7.5f * s, 12f * s)
            lineTo(15.5f * s, 20f * s)
        }
        drawPath(p, tint, style = Stroke(3.4f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconChevronRight(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val p = Path().apply {
            moveTo(8.5f * s, 4f * s)
            lineTo(16.5f * s, 12f * s)
            lineTo(8.5f * s, 20f * s)
        }
        drawPath(p, tint, style = Stroke(3.4f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconChevronDown(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val p = Path().apply {
            moveTo(5f * s, 9f * s)
            lineTo(12f * s, 16f * s)
            lineTo(19f * s, 9f * s)
        }
        drawPath(p, tint, style = Stroke(2.6f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
