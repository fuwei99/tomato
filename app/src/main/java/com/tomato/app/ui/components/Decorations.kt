package com.tomato.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * 待办卡片上的插画装饰（全部 Canvas 绘制，坐标按卡片尺寸比例）。
 * 原则：观感像就行，不追求像素级。
 */

/** 日落：山丘剪影 + 举手小人 */
@Composable
fun SunsetDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val hill = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.78f)
            quadraticBezierTo(w * 0.26f, h * 0.30f, w * 0.50f, h * 0.68f)
            quadraticBezierTo(w * 0.76f, h * 0.99f, w, h * 0.62f)
            lineTo(w, h)
            close()
        }
        drawPath(hill, Color(0xFF2E2317))

        val px = w * 0.585f
        val py = h * 0.40f
        val t = h * 0.028f
        drawCircle(Color(0xFF241A10), h * 0.062f, Offset(px, py))
        drawLine(Color(0xFF241A10), Offset(px, py + h * 0.07f), Offset(px, py + h * 0.30f), t, StrokeCap.Round)
        drawLine(Color(0xFF241A10), Offset(px, py + h * 0.13f), Offset(px - w * 0.048f, py + h * 0.03f), t, StrokeCap.Round)
        drawLine(Color(0xFF241A10), Offset(px, py + h * 0.13f), Offset(px + w * 0.048f, py + h * 0.03f), t, StrokeCap.Round)
        drawLine(Color(0xFF241A10), Offset(px, py + h * 0.29f), Offset(px - w * 0.018f, py + h * 0.50f), t, StrokeCap.Round)
        drawLine(Color(0xFF241A10), Offset(px, py + h * 0.29f), Offset(px + w * 0.018f, py + h * 0.50f), t, StrokeCap.Round)
    }
}

/** 云海（机械听课） */
@Composable
fun CloudDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFFE9C9D6), Offset(w * 0.30f, h * 0.82f), Size(w * 0.95f, h * 0.52f))
        drawOval(Color(0xFFF6ECF1), Offset(w * 0.52f, h * 0.34f), Size(w * 0.58f, h * 0.44f))
        drawOval(Color(0xFFF6ECF1), Offset(w * 0.74f, h * 0.16f), Size(w * 0.40f, h * 0.34f))
        drawOval(Color(0xB3FFFFFF), Offset(w * 0.16f, h * 0.30f), Size(w * 0.36f, h * 0.24f))
        drawOval(Color(0xB3FFFFFF), Offset(w * 0.30f, h * 0.16f), Size(w * 0.24f, h * 0.18f))
    }
}

/** 月牙（线性代数听课） */
@Composable
fun MoonDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val moon = Path().apply {
            arcTo(
                rect = Rect(Offset(w * 0.55f, h * 0.14f), Size(w * 0.30f, h * 0.72f)),
                startAngleDegrees = 90f, sweepAngleDegrees = 180f, forceMoveTo = true
            )
            arcTo(
                rect = Rect(Offset(w * 0.40f, h * 0.14f), Size(w * 0.44f, h * 0.72f)),
                startAngleDegrees = 270f, sweepAngleDegrees = -180f, forceMoveTo = false
            )
            close()
        }
        drawPath(moon, Color(0xFFFDFDF2))
        drawCircle(Color(0xCCFFFFFF), h * 0.03f, Offset(w * 0.30f, h * 0.30f))
        drawCircle(Color(0x99FFFFFF), h * 0.02f, Offset(w * 0.62f, h * 0.82f))
    }
}

/** 水上玻璃瓶（旋转体听课刷题） */
@Composable
fun BottleDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        // 瓶身
        drawRoundRect(
            Color(0x26E6F5F5), Offset(cx - w * 0.15f, h * 0.34f),
            Size(w * 0.30f, h * 0.54f), CornerRadius(w * 0.05f, w * 0.05f)
        )
        // 瓶颈
        drawRect(Color(0x26E6F5F5), Offset(cx - w * 0.06f, h * 0.20f), Size(w * 0.12f, h * 0.16f))
        // 瓶盖
        drawRoundRect(
            Color(0xFFE5CD72), Offset(cx - w * 0.07f, h * 0.10f),
            Size(w * 0.14f, h * 0.12f), CornerRadius(w * 0.02f, w * 0.02f)
        )
        // 玻璃描边
        drawRoundRect(
            Color(0xB3FFFFFF), Offset(cx - w * 0.15f, h * 0.34f),
            Size(w * 0.30f, h * 0.54f), CornerRadius(w * 0.05f, w * 0.05f),
            style = Stroke(width = h * 0.018f)
        )
        // 水面反光
        drawOval(Color(0x1AFFFFFF), Offset(cx - w * 0.22f, h * 0.86f), Size(w * 0.44f, h * 0.08f))
    }
}

/** 黑色木椅（设计毕业设计报告） */
@Composable
fun ChairDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Color(0xFF1D1D1D)
        val lw = h * 0.045f
        drawLine(c, Offset(w * 0.37f, h * 0.10f), Offset(w * 0.37f, h * 0.66f), lw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.70f, h * 0.10f), Offset(w * 0.70f, h * 0.66f), lw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.37f, h * 0.17f), Offset(w * 0.70f, h * 0.17f), lw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.37f, h * 0.36f), Offset(w * 0.70f, h * 0.36f), lw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.27f, h * 0.66f), Offset(w * 0.80f, h * 0.66f), lw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.33f, h * 0.66f), Offset(w * 0.30f, h * 0.98f), lw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.74f, h * 0.66f), Offset(w * 0.78f, h * 0.98f), lw, StrokeCap.Round)
    }
}

/** 红叶枝（背诵单词） */
@Composable
fun LeafDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(
            Color(0xFF6D4436), Offset(w * 0.98f, -h * 0.05f), Offset(w * 0.46f, h * 0.72f),
            strokeWidth = h * 0.022f, cap = StrokeCap.Round
        )
        val leaves = listOf(
            0.90f to 0.16f, 0.80f to 0.32f, 0.72f to 0.50f, 0.86f to 0.42f,
            0.63f to 0.62f, 0.55f to 0.70f, 0.48f to 0.74f
        )
        val colors = listOf(
            Color(0xFFA93428), Color(0xFFC1483A), Color(0xFF8E2B22),
            Color(0xFFD05A48), Color(0xFFB8402F), Color(0xFF9C2F24), Color(0xFFC74A38)
        )
        leaves.forEachIndexed { i, (fx, fy) ->
            rotate(degrees = -35f + i * 11f, pivot = Offset(w * fx, h * fy)) {
                drawOval(
                    colors[i],
                    Offset(w * fx - w * 0.018f, h * fy - h * 0.17f),
                    Size(w * 0.036f, h * 0.19f)
                )
            }
        }
    }
}

/** 顶部暖色光晕（机械刷题） */
@Composable
fun GlowDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w * 0.60f, h * 0.26f)
        val r = w * 0.42f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xF2FFF3C4), Color(0x00FFF3C4)),
                center = c, radius = r
            ),
            radius = r, center = c
        )
    }
}

/** 半透明云带（政治听课） */
@Composable
fun SkyDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawOval(Color(0x1FFFFFFF), Offset(w * 0.30f, h * 0.16f), Size(w * 0.62f, h * 0.42f))
        drawOval(Color(0x19FFFFFF), Offset(w * 0.02f, h * 0.52f), Size(w * 0.56f, h * 0.38f))
    }
}

/** 黄色花瓣（考研阅读两篇） */
@Composable
fun PetalDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val brush = Brush.linearGradient(
            listOf(Color(0xFFE2BD7C), Color(0xFFEFD494), Color(0xFFF7E8BD))
        )
        val petal = Path().apply {
            moveTo(w * 0.46f, h * 1.06f)
            quadraticBezierTo(w * 0.58f, h * 0.38f, w * 0.86f, h * 0.04f)
            quadraticBezierTo(w * 0.80f, h * 0.32f, w * 0.60f, h * 1.08f)
            close()
        }
        drawPath(petal, brush)
        val petal2 = Path().apply {
            moveTo(w * 0.72f, h * 1.08f)
            quadraticBezierTo(w * 0.80f, h * 0.52f, w * 0.94f, h * 0.42f)
            quadraticBezierTo(w * 0.90f, h * 0.62f, w * 0.82f, h * 1.10f)
            close()
        }
        drawPath(petal2, Color(0xFFF0D9A2))
    }
}

/** 皮卡丘（英语改错，简笔） */
@Composable
fun PikachuDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.62f
        val yellow = Color(0xFFF6D41F)
        // 耳朵
        drawPath(
            Path().apply {
                moveTo(cx - w * 0.10f, cy - h * 0.24f)
                lineTo(cx - w * 0.20f, h * 0.02f)
                lineTo(cx - w * 0.02f, cy - h * 0.22f)
                close()
            }, yellow
        )
        drawPath(
            Path().apply {
                moveTo(cx + w * 0.08f, cy - h * 0.26f)
                lineTo(cx + w * 0.18f, h * 0.00f)
                lineTo(cx + w * 0.00f, cy - h * 0.24f)
                close()
            }, yellow
        )
        // 身体
        drawOval(yellow, Offset(cx - w * 0.16f, cy - h * 0.30f), Size(w * 0.32f, h * 0.62f))
        // 眼睛 / 腮红 / 嘴
        drawCircle(Color(0xFF222222), w * 0.018f, Offset(cx - w * 0.05f, cy - h * 0.08f))
        drawCircle(Color(0xFF222222), w * 0.018f, Offset(cx + w * 0.05f, cy - h * 0.08f))
        drawCircle(Color(0xBFE8604E), w * 0.035f, Offset(cx - w * 0.11f, cy + h * 0.06f))
        drawCircle(Color(0xBFE8604E), w * 0.035f, Offset(cx + w * 0.11f, cy + h * 0.06f))
        drawArc(
            Color(0xFF222222), 20f, 140f, useCenter = false,
            topLeft = Offset(cx - w * 0.035f, cy + h * 0.02f),
            size = Size(w * 0.07f, h * 0.10f),
            style = Stroke(width = h * 0.012f)
        )
        // 尾巴
        drawPath(
            Path().apply {
                moveTo(cx + w * 0.16f, cy + h * 0.16f)
                lineTo(cx + w * 0.30f, cy + h * 0.02f)
                lineTo(cx + w * 0.26f, cy + h * 0.18f)
                lineTo(cx + w * 0.34f, cy + h * 0.22f)
                close()
            }, Color(0xFFE8B800)
        )
    }
}
