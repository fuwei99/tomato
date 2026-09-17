package com.tomato.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.theme.TealHeaderEnd
import com.tomato.app.ui.theme.TealHeaderMid
import com.tomato.app.ui.theme.TealHeaderStart
import com.tomato.app.ui.theme.White

/** 头部渐变：起 #008A8A → 中 #12A09C → 止 #1FA7A3（实测） */
private val HeaderGradient = Brush.verticalGradient(
    listOf(TealHeaderStart, TealHeaderMid, TealHeaderEnd)
)

/**
 * 番茄TODO 风格头部：总高 90dp = 状态栏 33dp + 内容区 57dp。
 * 需要配合 edge-to-edge（状态栏透明）使用。
 */
@Composable
fun AppHeader(
    title: String,
    showStudyModePill: Boolean = true,
    showPermissionText: Boolean = true,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(HeaderGradient)
    ) {
        StatusBarRow(Modifier.height(33.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                if (showStudyModePill) {
                    Spacer(Modifier.height(5.dp))
                    StudyModePill()
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                if (showPermissionText) {
                    Text(
                        text = "必开\n权限",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = White,
                        textAlign = TextAlign.Center
                    )
                }
                trailing()
            }
        }
    }
}

/** 学霸模式胶囊：10sp 白字 + 白色细描边 */
@Composable
fun StudyModePill(text: String = "点击开启学霸模式") {
    Box(
        Modifier
            .background(Color(0x1F002823), RoundedCornerShape(50))
            .border(1.dp, White, RoundedCornerShape(50))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = White)
    }
}

/**
 * 状态栏：33dp。时间 12.5sp + 右侧若干系统图标（demo 用简化绘制）。
 * TODO(M1)：接入真实系统时间。
 */
@Composable
fun StatusBarRow(
    modifier: Modifier = Modifier,
    time: String = "1:27",
    battery: Int = 94
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = White
        )
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 蓝牙
            CanvasIcon(tint = White, iconSize = 10.dp) { s ->
                val p = Path().apply {
                    moveTo(6.5f * s, 6.5f * s); lineTo(17.5f * s, 17.5f * s)
                    lineTo(12f * s, 22f * s); lineTo(12f * s, 2f * s)
                    lineTo(17.5f * s, 6.5f * s); lineTo(6.5f * s, 17.5f * s)
                }
                drawPath(p, White, style = Stroke(width = 1.9f * s))
            }
            // 月亮（一段粗圆弧近似月牙）
            CanvasIcon(tint = White, iconSize = 11.dp) { s ->
                drawArc(
                    color = White,
                    startAngle = 55f,
                    sweepAngle = 250f,
                    useCenter = false,
                    topLeft = Offset(3.5f * s, 3.5f * s),
                    size = Size(17f * s, 17f * s),
                    style = Stroke(width = 2.4f * s, cap = StrokeCap.Round)
                )
            }
            // 电池
            BatteryIndicator(level = battery)
        }
    }
}

/** 电池 + 充电闪电（简化绘制） */
@Composable
fun BatteryIndicator(level: Int, charging: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(22.dp)
                .height(10.dp)
                .border(0.7.dp, White, RoundedCornerShape(3.dp))
                .padding(0.9.dp)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF43D854), RoundedCornerShape(2.2.dp))
            )
            Text(
                text = level.toString(),
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B4D14),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(Modifier.width(1.2.dp))
        Box(
            Modifier
                .width(1.6.dp)
                .height(4.dp)
                .background(White, RoundedCornerShape(1.dp))
        )
        if (charging) {
            Canvas(Modifier.width(6.dp).height(9.dp)) {
                val p = Path().apply {
                    moveTo(size.width * 0.78f, 0f)
                    lineTo(0f, size.height * 0.58f)
                    lineTo(size.width * 0.4f, size.height * 0.58f)
                    lineTo(size.width * 0.22f, size.height)
                    lineTo(size.width, size.height * 0.4f)
                    lineTo(size.width * 0.58f, size.height * 0.4f)
                    close()
                }
                drawPath(p, White)
            }
        }
    }
}
