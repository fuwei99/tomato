package com.tomato.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TomatoColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = White,
    secondary = TealPrimaryDark,
    background = BgTodo,
    surface = White,
    onSurface = InkMain
)

/**
 * 说明：页面头部/卡片背景都在各组件里用自定义 Brush 绘制，
 * 这里的 colorScheme 只用于 Material3 组件默认值与文字色兜底。
 */
@Composable
fun TomatoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TomatoColorScheme,
        typography = TomatoTypography,
        content = content
    )
}
