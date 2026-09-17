package com.tomato.app.ui.data

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.db.TaskListEntity
import com.tomato.app.data.model.DurationPreset
import com.tomato.app.data.model.TaskKind
import com.tomato.app.data.model.TimerMode
import com.tomato.app.ui.components.BottleDecoration
import com.tomato.app.ui.components.CardVisual
import com.tomato.app.ui.components.ChairDecoration
import com.tomato.app.ui.components.CloudDecoration
import com.tomato.app.ui.components.GlowDecoration
import com.tomato.app.ui.components.LeafDecoration
import com.tomato.app.ui.components.MoonDecoration
import com.tomato.app.ui.components.PetalDecoration
import com.tomato.app.ui.components.PikachuDecoration
import com.tomato.app.ui.components.SkyDecoration
import com.tomato.app.ui.components.SunsetDecoration

/**
 * 卡片视觉样式表（渐变 + 插画），DB 里只存 styleKey，渲染时查这里。
 * 尺寸/配色来源：text/ui-spec/spec.md（1px = 1dp，与网页复刻完全一致）。
 */

// ===== 渐变工具 =====
// 说明：Color 是 value class，不能做 vararg 参数，故写死段数

/** 三段斜向渐变（≈ CSS 100~115deg：左上 → 右下） */
internal fun diag(c1: Color, c2: Color, c3: Color): Brush =
    Brush.linearGradient(listOf(c1, c2, c3), start = Offset.Zero, end = Offset.Infinite)

/** 两段斜向渐变（待办集卡片） */
internal fun diag(c1: Color, c2: Color): Brush =
    Brush.linearGradient(listOf(c1, c2), start = Offset.Zero, end = Offset.Infinite)

/** 竖向渐变（180deg） */
internal fun vert(c1: Color, c2: Color, c3: Color): Brush =
    Brush.verticalGradient(listOf(c1, c2, c3))

object TaskStyles {

    private val map: Map<String, CardVisual> = linkedMapOf(
        "leaf" to CardVisual(
            brush = diag(Color(0xFF92A6C3), Color(0xFFA9BAD0), Color(0xFFC0CEDB)),
            decoration = {
                LeafDecoration(Modifier.align(Alignment.CenterEnd).width(197.dp).fillMaxHeight())
            }
        ),
        "petal" to CardVisual(
            brush = diag(Color(0xFFB6BCC2), Color(0xFFD3D5D1), Color(0xFFE2E1D8)),
            decoration = {
                PetalDecoration(Modifier.align(Alignment.CenterEnd).width(255.dp).fillMaxHeight())
            }
        ),
        "pika" to CardVisual(
            brush = diag(Color(0xFFEFA17F), Color(0xFFF3B294), Color(0xFFF6C0A8)),
            decoration = {
                PikachuDecoration(
                    Modifier.align(Alignment.TopStart).offset(x = 150.dp, y = 5.dp)
                        .size(width = 78.dp, height = 58.dp)
                )
            }
        ),
        "cloud" to CardVisual(
            brush = diag(Color(0xFF7B90C1), Color(0xFF9FA4CB), Color(0xFFD3B0C6)),
            decoration = {
                CloudDecoration(Modifier.align(Alignment.CenterEnd).width(289.dp).fillMaxHeight())
            }
        ),
        "sunset" to CardVisual(
            brush = vert(Color(0xFFC5A059), Color(0xFFD9BD7C), Color(0xFFE8D298)),
            decoration = {
                GlowDecoration(Modifier.fillMaxSize())
                SunsetDecoration(Modifier.align(Alignment.BottomStart).width(340.dp).height(42.dp))
            }
        ),
        "moon" to CardVisual(
            brush = diag(Color(0xFF12907F), Color(0xFF1BA390), Color(0xFF2CBDAE)),
            decoration = {
                MoonDecoration(
                    Modifier.align(Alignment.TopStart).offset(x = 190.dp, y = 20.dp).size(32.dp)
                )
            }
        ),
        "bottle" to CardVisual(
            brush = vert(Color(0xFF16606A), Color(0xFF0F4953), Color(0xFF0A333D)),
            decoration = {
                BottleDecoration(
                    Modifier.align(Alignment.TopStart).offset(x = 154.dp, y = 13.dp)
                        .size(width = 27.dp, height = 46.dp)
                )
            }
        ),
        "sky" to CardVisual(
            brush = diag(Color(0xFF4C7EC3), Color(0xFF5D8CCF), Color(0xFF6E9ADA)),
            decoration = {
                SkyDecoration(Modifier.align(Alignment.CenterEnd).width(272.dp).fillMaxHeight())
            }
        ),
        "chair" to CardVisual(
            brush = diag(Color(0xFFA596A6), Color(0xFFC2B1B9), Color(0xFFD3C5C7)),
            lightText = true,
            decoration = {
                ChairDecoration(
                    Modifier.align(Alignment.TopStart).offset(x = 252.dp, y = 16.dp)
                        .size(width = 40.dp, height = 46.dp)
                )
            }
        ),
        // 待办集（纯色渐变）
        "pink" to CardVisual(diag(Color(0xFFF2A3BD), Color(0xFFF4AEC5))),
        "gray" to CardVisual(diag(Color(0xFF6C6C6C), Color(0xFF757575))),
        "navy" to CardVisual(diag(Color(0xFF2A3153), Color(0xFF2E3860)))
    )

    operator fun get(key: String): CardVisual = map[key] ?: map.getValue("leaf")
}

/**
 * 首启种子数据（与截图内容一致）。写入 Room 后即由用户数据接管。
 */
object DemoSeed {

    val lists = listOf(
        TaskListEntity(id = 1, name = "默认"),
        TaskListEntity(id = 2, name = "10.30任务")
    )

    private data class Row(
        val listId: Long,
        val title: String,
        val minutes: Int,
        val style: String,
        val kind: TaskKind = TaskKind.POMODORO,
        val mode: TimerMode = TimerMode.COUNTDOWN,
        val cycles: Int = 1,
        val breakMin: Int = 5
    )

    private val rows = listOf(
        Row(1, "背诵单词", 35, "leaf"),
        Row(1, "考研阅读两篇", 40, "petal"),
        Row(1, "英语改错", 25, "pika"),
        Row(1, "机械听课", 120, "cloud"),
        Row(1, "机械刷题", 180, "sunset"),
        Row(1, "线性代数听课", 60, "moon"),
        Row(1, "旋转体听课刷题", 60, "bottle"),
        Row(1, "政治听课", 60, "sky"),
        Row(1, "设计毕业设计报告", 35, "chair"),
        Row(2, "凸轮机构", 120, "pink"),
        Row(2, "数学试卷", 25, "gray"),
        Row(2, "清理房间", 35, "navy")
    )

    /** 新建任务时轮换取用的样式，让新卡片不至于全是一个颜色 */
    private val newTaskStyleOrder = listOf(
        "leaf", "petal", "pika", "cloud", "sunset", "moon", "bottle", "sky", "chair"
    )
    private var styleCursor = 0

    fun nextStyleKey(): String {
        val key = newTaskStyleOrder[styleCursor % newTaskStyleOrder.size]
        styleCursor++
        return key
    }

    fun tasks(): List<TaskEntity> =
        rows.mapIndexed { i, r ->
            // 种子写入后把游标推到后面，避免用户新建任务时又撞上已有样式
            styleCursor = newTaskStyleOrder.size
            TaskEntity(
                listId = r.listId,
                title = r.title,
                minutes = r.minutes,
                styleKey = r.style,
                sortOrder = i,
                kind = r.kind.ordinal,
                timerMode = r.mode.ordinal,
                durationPreset = DurationPreset.of(r.minutes).ordinal,
                cycleTarget = r.cycles,
                breakMinutes = r.breakMin
            )
        }
}

// ===== 统计页数据结构 =====

/** 饼图扇区（由 PomodoroSession 聚合而来） */
data class PieSlice(
    val name: String,
    val time: String,
    val percent: Float,   // 0f ~ 1f
    val color: Color
)

/** 统计页大数字列：label + 若干（值, 单位） */
data class StatColumn(
    val label: String,
    val values: List<Pair<String, String?>>
)
