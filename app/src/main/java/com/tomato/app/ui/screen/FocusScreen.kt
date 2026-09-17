package com.tomato.app.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.focus.FocusState
import com.tomato.app.ui.components.FocusScene
import com.tomato.app.ui.components.FocusSceneBackground
import com.tomato.app.ui.components.IconBrightness
import com.tomato.app.ui.components.IconCheck
import com.tomato.app.ui.components.IconChevronLeft
import com.tomato.app.ui.components.IconClose
import com.tomato.app.ui.components.IconImage
import com.tomato.app.ui.components.IconPause
import com.tomato.app.ui.components.IconPlay
import com.tomato.app.ui.components.IconReset
import com.tomato.app.ui.theme.White
import kotlin.math.cos
import kotlin.math.sin

/** 专注页：点击任务卡「开始」后全屏进入（背景动画 + 圆环倒计时 + 控制栏） */
@Composable
fun FocusScreen(
    state: FocusState,
    onMinimize: () -> Unit,      // 返回列表，计时继续
    onToggle: () -> Unit,        // 暂停 / 继续
    onReset: () -> Unit,
    onGiveUp: () -> Unit,        // 放弃（按已专注时长落库）
    onFinish: () -> Unit,        // 完成后返回
    modifier: Modifier = Modifier
) {
    var scene by remember { mutableStateOf(FocusScene.SNOW) }
    var dimIndex by remember { mutableIntStateOf(0) }
    val dimLevels = remember { listOf(0f, 0.18f, 0.40f) }

    // 进场动画：背景 1.10 → 1.00 + 淡入，内容上浮
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 560, easing = FastOutSlowInEasing))
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF0B1520))
    ) {
        // 背景（可切换，带交叉淡入）
        Crossfade(
            targetState = scene,
            animationSpec = tween(durationMillis = 600),
            label = "sceneCrossfade"
        ) { s ->
            FocusSceneBackground(
                scene = s,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.10f - 0.10f * enter.value)
                    .alpha(enter.value)
            )
        }

        // 亮度遮罩（不申请系统设置权限，只做应用层调暗）
        if (dimLevels[dimIndex] > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimLevels[dimIndex]))
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .alpha(enter.value)
                .offset(y = ((1f - enter.value) * 26f).dp)
        ) {
            FocusTopBar(title = state.title, onMinimize = onMinimize)

            Spacer(Modifier.weight(1f))

            Text(
                text = quoteFor(state.taskId),
                fontSize = 15.sp,
                lineHeight = 26.sp,
                color = White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 46.dp)
            )

            Spacer(Modifier.height(34.dp))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FocusRing(state = state)
            }

            Spacer(Modifier.weight(1f))

            if (state.finished) {
                FinishBar(onFinish = onFinish)
            } else {
                FocusControls(
                    running = state.running,
                    onBrightness = { dimIndex = (dimIndex + 1) % dimLevels.size },
                    onScene = { scene = scene.next() },
                    onToggle = onToggle,
                    onReset = onReset,
                    onGiveUp = onGiveUp
                )
            }

            Spacer(Modifier.height(26.dp))
        }
    }
}

// ===== 顶部 =====

@Composable
private fun FocusTopBar(title: String, onMinimize: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconChevronLeft(
            tint = White,
            iconSize = 20.dp,
            modifier = Modifier.clickable(onClick = onMinimize)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = White,
            maxLines = 1
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(20.dp))
    }
}

// ===== 圆环倒计时 =====

@Composable
private fun FocusRing(state: FocusState, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ringPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.965f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier.size(220.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 8.dp.toPx()

            // 呼吸光晕
            if (state.running) {
                drawCircle(
                    color = White.copy(alpha = 0.10f),
                    radius = r * pulse,
                    style = Stroke(width = 20.dp.toPx())
                )
            }
            // 轨道
            drawCircle(
                color = White.copy(alpha = 0.22f),
                radius = r,
                style = Stroke(width = 4.dp.toPx())
            )
            // 剩余进度
            val sweep = if (state.finished) 360f else 360f * state.progress
            drawArc(
                color = White,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            // 进度头部圆点
            if (!state.finished && state.progress > 0.005f) {
                val a = Math.toRadians((-90f + sweep).toDouble())
                drawCircle(
                    color = White,
                    radius = 4.5.dp.toPx(),
                    center = Offset(
                        center.x + cos(a).toFloat() * r,
                        center.y + sin(a).toFloat() * r
                    )
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.finished) {
                Text(
                    text = "完成",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Light,
                    color = White,
                    letterSpacing = 2.sp
                )
            } else {
                Text(
                    text = formatClock(state.remainMillis),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Light,
                    color = White,
                    letterSpacing = (-1).sp
                )
            }
            Text(
                text = when {
                    state.finished -> "又一个番茄，很好"
                    state.running -> "专注中"
                    else -> "已暂停"
                },
                fontSize = 12.sp,
                color = White.copy(alpha = 0.78f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ===== 控制栏 =====

@Composable
private fun FocusControls(
    running: Boolean,
    onBrightness: () -> Unit,
    onScene: () -> Unit,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onGiveUp: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlItem(label = "亮度", onClick = onBrightness) {
            IconBrightness(tint = White, iconSize = 22.dp)
        }
        ControlItem(label = "背景", onClick = onScene) {
            IconImage(tint = White, iconSize = 22.dp)
        }
        ControlItem(label = if (running) "暂停" else "继续", onClick = onToggle) {
            if (running) IconPause(tint = White, iconSize = 22.dp)
            else IconPlay(tint = White, iconSize = 22.dp)
        }
        ControlItem(label = "重置", onClick = onReset) {
            IconReset(tint = White, iconSize = 22.dp)
        }
        ControlItem(label = "放弃", onClick = onGiveUp) {
            IconClose(tint = White, iconSize = 22.dp)
        }
    }
}

@Composable
private fun ControlItem(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(
        Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        icon()
        Text(text = label, fontSize = 10.sp, color = White.copy(alpha = 0.92f))
    }
}

@Composable
private fun FinishBar(onFinish: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            Modifier
                .background(Color(0x33FFFFFF), androidx.compose.foundation.shape.RoundedCornerShape(50))
                .clickable(onClick = onFinish)
                .padding(horizontal = 34.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconCheck(tint = White, iconSize = 18.dp)
            Text(text = "完成，返回列表", fontSize = 13.sp, color = White)
        }
    }
}

// ===== 工具 =====

private val Quotes = listOf(
    "种一棵树最好的时间是十年前，其次是现在。",
    "专注，是把有限的自己押在最重要的一件事上。",
    "不要等，时机永远不会恰到好处。",
    "慢一点没关系，停下来了才可惜。",
    "今天的克制，是明天的自由。"
)

private fun quoteFor(taskId: Long): String {
    val i = (taskId % Quotes.size).toInt()
    return Quotes[if (i < 0) 0 else i]
}

private fun formatClock(ms: Long): String {
    val total = (ms + 999L) / 1000L
    val m = total / 60L
    val s = total % 60L
    return "%02d:%02d".format(m, s)
}
