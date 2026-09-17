package com.tomato.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 专注页底部控制栏图标（仍然是 24×24 Canvas 手绘，风格与 Icons.kt 一致）。
 */

/** 亮度：太阳 + 四周光芒 */
@Composable
fun IconBrightness(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawCircle(tint, 4.2f * s, Offset(12f * s, 12f * s))
        for (i in 0 until 8) {
            val a = Math.toRadians((i * 45).toDouble())
            val c = Math.cos(a).toFloat()
            val si = Math.sin(a).toFloat()
            drawLine(
                tint,
                Offset(12f * s + c * 6.6f * s, 12f * s + si * 6.6f * s),
                Offset(12f * s + c * 9.2f * s, 12f * s + si * 9.2f * s),
                1.9f * s,
                StrokeCap.Round
            )
        }
    }
}

/** 背景：图片框 + 山 + 太阳 */
@Composable
fun IconImage(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawRoundRect(
            tint, Offset(3f * s, 5f * s), Size(18f * s, 14f * s),
            CornerRadius(2.4f * s, 2.4f * s), style = Stroke(width = 1.9f * s)
        )
        drawCircle(tint, 1.6f * s, Offset(8f * s, 9.4f * s))
        val hill = Path().apply {
            moveTo(5f * s, 16.4f * s)
            lineTo(10.4f * s, 11.4f * s)
            lineTo(14.6f * s, 16.4f * s)
            close()
        }
        drawPath(hill, tint)
        drawLine(
            tint, Offset(14.2f * s, 16.4f * s), Offset(19.4f * s, 12.2f * s),
            1.6f * s, StrokeCap.Round
        )
    }
}

/** 暂停：两根竖条 */
@Composable
fun IconPause(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawRoundRect(
            tint, Offset(8.2f * s, 5.5f * s), Size(3f * s, 13f * s),
            CornerRadius(1.2f * s, 1.2f * s)
        )
        drawRoundRect(
            tint, Offset(12.8f * s, 5.5f * s), Size(3f * s, 13f * s),
            CornerRadius(1.2f * s, 1.2f * s)
        )
    }
}

/** 继续：三角形 */
@Composable
fun IconPlay(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val p = Path().apply {
            moveTo(8.6f * s, 5.6f * s)
            lineTo(18f * s, 12f * s)
            lineTo(8.6f * s, 18.4f * s)
            close()
        }
        drawPath(p, tint)
    }
}

/** 重置：环形箭头 */
@Composable
fun IconReset(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawArc(
            tint, 35f, 300f, useCenter = false,
            topLeft = Offset(4f * s, 4f * s), size = Size(16f * s, 16f * s),
            style = Stroke(width = 2.2f * s, cap = StrokeCap.Round)
        )
        val head = Path().apply {
            moveTo(4.2f * s, 8.4f * s)
            lineTo(4.2f * s, 3.6f * s)
            lineTo(8.8f * s, 5.2f * s)
        }
        drawPath(
            head, tint,
            style = Stroke(width = 2.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** 放弃：叉号 */
@Composable
fun IconClose(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        drawLine(
            tint, Offset(6f * s, 6f * s), Offset(18f * s, 18f * s),
            2.4f * s, StrokeCap.Round
        )
        drawLine(
            tint, Offset(18f * s, 6f * s), Offset(6f * s, 18f * s),
            2.4f * s, StrokeCap.Round
        )
    }
}

/** 完成：对勾 */
@Composable
fun IconCheck(modifier: Modifier = Modifier, tint: Color, iconSize: Dp = 24.dp) {
    CanvasIcon(modifier, tint, iconSize) { s ->
        val p = Path().apply {
            moveTo(5.5f * s, 12.6f * s)
            lineTo(10.2f * s, 17.4f * s)
            lineTo(18.5f * s, 7.2f * s)
        }
        drawPath(
            p, tint,
            style = Stroke(width = 2.6f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
