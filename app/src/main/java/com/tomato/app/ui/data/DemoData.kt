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
 * Demo 阶段的假数据，全部照抄截图内容（见 text/ui-spec/spec.md）。
 * M1/M2 接入 Room 后替换为数据库数据。
 *
 * 卡片 = 340×68dp（左右边距 10dp），装饰用 BoxScope 绝对定位，
 * 坐标与 reference/tomato-ui-reference.html 中的 .deco 完全一致（1px = 1dp）。
 */

// ===== 渐变工具 =====

// 说明：Color 是 value class，不能做 vararg 参数，故写死 3 段渐变
/** 斜向渐变（≈ CSS 100~115deg：左上 → 右下） */
internal fun diag(c1: Color, c2: Color, c3: Color): Brush =
    Brush.linearGradient(listOf(c1, c2, c3), start = Offset.Zero, end = Offset.Infinite)

/** 两段斜向渐变（待办集卡片） */
internal fun diag(c1: Color, c2: Color): Brush =
    Brush.linearGradient(listOf(c1, c2), start = Offset.Zero, end = Offset.Infinite)

/** 竖向渐变（180deg） */
internal fun vert(c1: Color, c2: Color, c3: Color): Brush =
    Brush.verticalGradient(listOf(c1, c2, c3))

// ===== 待办页数据 =====

data class DemoTask(
    val title: String,
    val minutes: Int,
    val visual: CardVisual
)

val DemoTasks: List<DemoTask> = listOf(
    DemoTask(
        "背诵单词", 35,
        CardVisual(
            brush = diag(Color(0xFF92A6C3), Color(0xFFA9BAD0), Color(0xFFC0CEDB)),
            decoration = {
                LeafDecoration(Modifier.align(Alignment.CenterEnd).width(197.dp).fillMaxHeight())
            }
        )
    ),
    DemoTask(
        "考研阅读两篇", 40,
        CardVisual(
            brush = diag(Color(0xFFB6BCC2), Color(0xFFD3D5D1), Color(0xFFE2E1D8)),
            decoration = {
                PetalDecoration(Modifier.align(Alignment.CenterEnd).width(255.dp).fillMaxHeight())
            }
        )
    ),
    DemoTask(
        "英语改错", 25,
        CardVisual(
            brush = diag(Color(0xFFEFA17F), Color(0xFFF3B294), Color(0xFFF6C0A8)),
            decoration = {
                PikachuDecoration(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 150.dp, y = 5.dp)
                        .size(width = 78.dp, height = 58.dp)
                )
            }
        )
    ),
    DemoTask(
        "机械听课", 120,
        CardVisual(
            brush = diag(Color(0xFF7B90C1), Color(0xFF9FA4CB), Color(0xFFD3B0C6)),
            decoration = {
                CloudDecoration(Modifier.align(Alignment.CenterEnd).width(289.dp).fillMaxHeight())
            }
        )
    ),
    DemoTask(
        "机械刷题", 180,
        CardVisual(
            brush = vert(Color(0xFFC5A059), Color(0xFFD9BD7C), Color(0xFFE8D298)),
            decoration = {
                GlowDecoration(Modifier.fillMaxSize())
                SunsetDecoration(Modifier.align(Alignment.BottomStart).width(340.dp).height(42.dp))
            }
        )
    ),
    DemoTask(
        "线性代数听课", 60,
        CardVisual(
            brush = diag(Color(0xFF12907F), Color(0xFF1BA390), Color(0xFF2CBDAE)),
            decoration = {
                MoonDecoration(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 190.dp, y = 20.dp)
                        .size(32.dp)
                )
            }
        )
    ),
    DemoTask(
        "旋转体听课刷题", 60,
        CardVisual(
            brush = vert(Color(0xFF16606A), Color(0xFF0F4953), Color(0xFF0A333D)),
            decoration = {
                BottleDecoration(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 154.dp, y = 13.dp)
                        .size(width = 27.dp, height = 46.dp)
                )
            }
        )
    ),
    DemoTask(
        "政治听课", 60,
        CardVisual(
            brush = diag(Color(0xFF4C7EC3), Color(0xFF5D8CCF), Color(0xFF6E9ADA)),
            decoration = {
                SkyDecoration(Modifier.align(Alignment.CenterEnd).width(272.dp).fillMaxHeight())
            }
        )
    ),
    DemoTask(
        "设计毕业设计报告", 35,
        CardVisual(
            brush = diag(Color(0xFFA596A6), Color(0xFFC2B1B9), Color(0xFFD3C5C7)),
            lightText = true,
            decoration = {
                ChairDecoration(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 252.dp, y = 16.dp)
                        .size(width = 40.dp, height = 46.dp)
                )
            }
        )
    )
)

// ===== 待办集数据 =====

data class DemoCollection(
    val name: String,
    val tasks: List<DemoTask>
)

val DemoCollections: List<DemoCollection> = listOf(
    DemoCollection(
        "10.30任务",
        listOf(
            DemoTask("凸轮机构", 120, CardVisual(diag(Color(0xFFF2A3BD), Color(0xFFF4AEC5)))),
            DemoTask("数学试卷", 25, CardVisual(diag(Color(0xFF6C6C6C), Color(0xFF757575)))),
            DemoTask("清理房间", 35, CardVisual(diag(Color(0xFF2A3153), Color(0xFF2E3860))))
        )
    )
)

// ===== 统计页数据 =====

data class PieSlice(
    val name: String,
    val time: String,
    val percent: Float,   // 0f ~ 1f
    val color: Color
)

val DemoPieSlices: List<PieSlice> = listOf(
    PieSlice("数学1000题", "36小时51分", 0.684f, Color(0xFFEF8B9C)),
    PieSlice("数学学习", "13小时8分", 0.244f, Color(0xFF93C0BF)),
    PieSlice("英语阅读练习-翻译-作文", "2小时0分", 0.037f, Color(0xFFD5E8E8)),
    PieSlice("机械学习", "1小时30分", 0.028f, Color(0xFF64919E)),
    PieSlice("锁机", "25分钟", 0.008f, Color(0xFFF2D489))
)

/** 统计页大数字：label + 若干（值, 单位） */
data class StatColumn(
    val label: String,
    val values: List<Pair<String, String?>>
)

val CumulativeStats = listOf(
    StatColumn("次数", listOf("337" to null)),
    StatColumn("时长", listOf("342" to "小时", "11" to "分钟")),
    StatColumn("日均时长", listOf("4" to "小时", "57" to "分钟"))
)

val TodayStats = listOf(
    StatColumn("次数", listOf("0" to null)),
    StatColumn("时长", listOf("0" to "分钟"))
)
