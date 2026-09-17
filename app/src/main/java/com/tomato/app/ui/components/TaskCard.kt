package com.tomato.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.theme.InkLight
import com.tomato.app.ui.theme.White

/**
 * 卡片视觉：背景渐变 + 是否用深色文字（浅底卡片）+ 插画装饰。
 * decoration 处于 BoxScope，可用 align/offset 做绝对定位（参考 HTML 的 .deco）。
 */
data class CardVisual(
    val brush: Brush,
    val lightText: Boolean = false,
    val decoration: (@Composable BoxScope.() -> Unit)? = null
)

/** 卡片文案统一样式（带 1dp 投影，浅底卡片无投影） */
private fun cardTextStyle(
    size: TextUnit,
    color: Color,
    weight: FontWeight,
    shadow: Shadow?
) = TextStyle(
    fontSize = size,
    fontWeight = weight,
    color = color,
    shadow = shadow
)

/**
 * 待办 / 待办集 卡片。
 * 待办：高 68dp、圆角 7dp、padding(13,14,11,14)、标题 15sp；
 * 待办集：高 53dp、圆角 6dp、padding(9,12,8,12)、标题 14sp。
 */
@Composable
fun TaskCard(
    title: String,
    minutes: Int,
    visual: CardVisual,
    modifier: Modifier = Modifier,
    height: Dp = 68.dp,
    cornerRadius: Dp = 7.dp,
    titleSize: TextUnit = 15.sp,
    timeSize: TextUnit = 12.sp,
    startLabel: String = "开始",
    contentPadding: PaddingValues = PaddingValues(start = 14.dp, end = 14.dp, top = 13.dp, bottom = 11.dp),
    onClick: () -> Unit = {}
) {
    val textColor = if (visual.lightText) InkLight else White
    val textShadow: Shadow? =
        if (visual.lightText) null
        else Shadow(color = Color(0x2E000000), offset = Offset(0f, 1.5f), blurRadius = 5f)

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(1.dp, shape)
            .clip(shape)
            .background(visual.brush)
            .clickable(onClick = onClick)
    ) {
        visual.decoration?.invoke(this)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = cardTextStyle(titleSize, textColor, FontWeight.Medium, textShadow)
                )
                Text(
                    text = "$minutes 分钟",
                    style = cardTextStyle(timeSize, textColor, FontWeight.Normal, textShadow)
                )
            }
            Text(
                text = startLabel,
                style = cardTextStyle(titleSize, textColor, FontWeight.Normal, textShadow)
            )
        }
    }
}
