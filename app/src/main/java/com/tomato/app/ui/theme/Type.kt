package com.tomato.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 字号层级见 ui-spec/spec.md 第 3 节。
 */
val TomatoTypography = Typography(
    // 头部标题 15sp bold
    titleLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold),
    // 卡片标题 15sp
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
    // 卡片时长 / 次要 12sp
    bodyMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    // 列标签 9.5sp
    labelSmall = TextStyle(fontSize = 9.5.sp, fontWeight = FontWeight.Normal),
    // 底部导航 10sp
    labelMedium = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal)
)
