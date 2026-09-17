package com.tomato.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/** 专注页可选背景（底部「背景」按钮循环切换） */
enum class FocusScene(val label: String) {
    SNOW("雪原"),
    SUNSET("黄昏"),
    NIGHT("夜空");

    fun next(): FocusScene = FocusScene.entries[(ordinal + 1) % FocusScene.entries.size]
}

private data class ScenePalette(
    val sky: List<Color>,
    val far: List<Color>,
    val near: List<Color>,
    val ground: List<Color>,
    val orb: Color?,
    val orbRadiusDp: Float,
    val orbY: Float,
    val particle: Color,
    val starry: Boolean
)

private val SnowScene = ScenePalette(
    sky = listOf(Color(0xFF123C5E), Color(0xFF2E6C93), Color(0xFF7FB3CE)),
    far = listOf(Color(0xFF8FB9D6), Color(0xFFBBD6E6)),
    near = listOf(Color(0xFFDCEBF5), Color(0xFFF3FAFE)),
    ground = listOf(Color(0xFFEAF4FA), Color(0xFFFFFFFF)),
    orb = Color(0xFFE9F3FF),
    orbRadiusDp = 15f,
    orbY = 0.16f,
    particle = Color(0xCCFFFFFF),
    starry = false
)

private val SunsetScene = ScenePalette(
    sky = listOf(Color(0xFF2A1A48), Color(0xFF7B3C68), Color(0xFFD9684B), Color(0xFFF6B26B)),
    far = listOf(Color(0xFF6B3F5E), Color(0xFFB2604F)),
    near = listOf(Color(0xFF452740), Color(0xFF7A3E52)),
    ground = listOf(Color(0xFF2C1A31), Color(0xFF4C2C3C)),
    orb = Color(0xFFFFD79A),
    orbRadiusDp = 26f,
    orbY = 0.24f,
    particle = Color(0x33FFFFFF),
    starry = false
)

private val NightScene = ScenePalette(
    sky = listOf(Color(0xFF04091B), Color(0xFF0A1730), Color(0xFF16305C)),
    far = listOf(Color(0xFF163663), Color(0xFF23507C)),
    near = listOf(Color(0xFF0B1730), Color(0xFF14294A)),
    ground = listOf(Color(0xFF0A1424), Color(0xFF132238)),
    orb = Color(0xFFF4F7FF),
    orbRadiusDp = 18f,
    orbY = 0.15f,
    particle = Color(0xDDFFFFFF),
    starry = true
)

private fun palette(scene: FocusScene): ScenePalette = when (scene) {
    FocusScene.SNOW -> SnowScene
    FocusScene.SUNSET -> SunsetScene
    FocusScene.NIGHT -> NightScene
}

private data class Particle(val x: Float, val y: Float, val scale: Float, val seed: Float)

/**
 * 专注页背景：天空渐变 + 远/近两层山峦 + 地面 + 飘雪/星空粒子（持续动画）。
 * 全部 Canvas 手绘，不依赖任何图片资源。
 */
@Composable
fun FocusSceneBackground(
    scene: FocusScene,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    val transition = rememberInfiniteTransition(label = "focusScene")
    // 0 → 1 循环，驱动粒子下落 / 星星闪烁 / 云飘移
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing)
        ),
        label = "t"
    )

    val particles = remember {
        List(64) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                scale = Random.nextFloat(),
                seed = Random.nextFloat()
            )
        }
    }

    val pal = palette(scene)

    Canvas(modifier = modifier) {
        drawScene(pal = pal, t = t, particles = particles)
        // 顶部/底部各压一层暗角，保证白字可读
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0x33000000), Color.Transparent, Color(0x22000000))
            ),
            alpha = alpha
        )
    }
}

private fun DrawScope.drawScene(
    pal: ScenePalette,
    t: Float,
    particles: List<Particle>
) {
    val w = size.width
    val h = size.height

    // 天空
    drawRect(brush = Brush.verticalGradient(pal.sky, startY = 0f, endY = h * 0.80f))

    // 星星（夜空）
    if (pal.starry) {
        particles.forEach { p ->
            val tw = 0.25f + 0.75f * abs(sin(t * 8f + p.seed * 40f))
            drawCircle(
                color = pal.particle.copy(alpha = tw),
                radius = (0.6f + p.scale * 1.1f).dp.toPx(),
                center = Offset(p.x * w, p.y * h * 0.62f)
            )
        }
    }

    // 日/月
    pal.orb?.let { color ->
        drawCircle(
            color = color,
            radius = pal.orbRadiusDp.dp.toPx(),
            center = Offset(w * 0.74f, h * pal.orbY)
        )
        drawCircle(
            color = color.copy(alpha = 0.16f),
            radius = pal.orbRadiusDp.dp.toPx() * 2.1f,
            center = Offset(w * 0.74f, h * pal.orbY)
        )
    }

    // 云（黄昏）
    if (!pal.starry) {
        val cy = h * 0.30f
        for (i in 0..2) {
            val base = ((t * 0.25f + i * 0.37f) % 1.3f) - 0.15f
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = 26.dp.toPx() + i * 6.dp.toPx(),
                center = Offset(base * w, cy + i * 14.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = 18.dp.toPx() + i * 4.dp.toPx(),
                center = Offset(base * w + 22.dp.toPx(), cy + i * 14.dp.toPx())
            )
        }
    }

    // 远山
    val far = Path().apply {
        moveTo(0f, h * 0.70f)
        lineTo(w * 0.18f, h * 0.545f)
        lineTo(w * 0.34f, h * 0.665f)
        lineTo(w * 0.52f, h * 0.505f)
        lineTo(w * 0.70f, h * 0.655f)
        lineTo(w * 0.86f, h * 0.575f)
        lineTo(w, h * 0.66f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(far, Brush.verticalGradient(pal.far, startY = h * 0.50f, endY = h * 0.86f))

    // 近山
    val near = Path().apply {
        moveTo(0f, h * 0.795f)
        lineTo(w * 0.24f, h * 0.685f)
        lineTo(w * 0.46f, h * 0.80f)
        lineTo(w * 0.68f, h * 0.70f)
        lineTo(w * 0.88f, h * 0.80f)
        lineTo(w, h * 0.755f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(near, Brush.verticalGradient(pal.near, startY = h * 0.68f, endY = h))

    // 地面（雪原/土地）
    drawRect(
        brush = Brush.verticalGradient(pal.ground, startY = h * 0.80f, endY = h),
        topLeft = Offset(0f, h * 0.80f),
        size = androidx.compose.ui.geometry.Size(w, h * 0.20f)
    )

    // 飘雪（非夜空场景）
    if (!pal.starry) {
        particles.forEach { p ->
            val drift = sin(t * 6.2831855f + p.seed * 25f) * 9.dp.toPx()
            val y = ((p.y + t * (0.5f + p.scale * 0.7f)) % 1f) * h
            drawCircle(
                color = pal.particle.copy(alpha = 0.25f + p.scale * 0.5f),
                radius = (0.7f + p.scale * 1.5f).dp.toPx(),
                center = Offset(p.x * w + drift, y)
            )
        }
    }
}
