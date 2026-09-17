package com.tomato.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 配色全部来自 ui-spec/spec.md（真机截图实测值）。
 */

// ===== 主色 Teal =====
val TealHeaderStart = Color(0xFF008A8A)   // 头部渐变 起（顶）
val TealHeaderMid = Color(0xFF12A09C)    // 头部渐变 中
val TealHeaderEnd = Color(0xFF1FA7A3)    // 头部渐变 止（底）

val TealPrimary = Color(0xFF009186)      // 强调色
val TealPrimaryDark = Color(0xFF00796B)  // 统计卡标题
val TealSegmentOn = Color(0xFF62B3A8)    // 分段器激活底
val TealSegmentOff = Color(0xFF5EB0A5)   // 分段器未激活字
val TealSegmentLine = Color(0xFFA8D5CD)  // 分段器描边
val TealButtonBg = Color(0xFFD8EBE6)     // 查看专注记录

// ===== 文字 =====
val InkMain = Color(0xFF37474F)          // 主文字
val InkNumber = Color(0xFF3A4F52)        // 大数字
val InkLabel = Color(0xFF4B5F66)         // 列标签
val InkSub = Color(0xFF7D939D)           // 次要（日期）
val InkMuted = Color(0xFF90A4AE)         // 图例时长
val InkLight = Color(0xFF4C4C4C)         // 浅底卡片上的深色文字
val White = Color(0xFFFFFFFF)

// ===== 背景 =====
val BgTodo = Color(0xFFFAFAFA)
val BgCollections = Color(0xFFFBFCFC)

// 统计页竖向渐变（顶部 → 底部）
val StatsBgStops = listOf(
    Color(0xFF20AAAA),
    Color(0xFF3DB1B1),
    Color(0xFF63BEBE),
    Color(0xFF8BCCCC),
    Color(0xFFB1D9D9),
    Color(0xFFEAF1F1)
)

// ===== 统计饼图扇区 =====
val PieMath1000 = Color(0xFFEF8B9C)
val PieMathStudy = Color(0xFF93C0BF)
val PieEnglish = Color(0xFFD5E8E8)
val PieMechanics = Color(0xFF64919E)
val PieLock = Color(0xFFF2D489)

val PieLabelLine = Color(0xFF90A4AE)
val PieLabelText = Color(0xFF546E7A)

// ===== 其它细节色 =====
val NavInactive = Color(0xFF3C4C52)      // 底部导航未选中
val NavDivider = Color(0xFFE3E9E7)       // 导航顶部分割线
val CollIcon = Color(0xFF7E9691)         // 待办集列表头右侧图标
val MiniIcon = Color(0xFF455A64)         // 统计卡头部小图标
val PieNote = Color(0xFF455A64)          // 饼图下方「总计…」
val Placeholder = Color(0xFF9AB0AB)      // 占位页文字
val SkyWhite = Color(0x1AFFFFFF)         // 卡片上的半透明白（云/天空）
