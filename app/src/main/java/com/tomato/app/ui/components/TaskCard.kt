package com.tomato.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
@OptIn(ExperimentalFoundationApi::class)
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
    /** 副标题（类型 / 计时方式），空则不显示 */
    subtitle: String? = null,
    onClick: () -> Unit = {},
    /** 传了它，「开始」二字单独可点（整卡不再响应单击） */
    onStartClick: (() -> Unit)? = null,
    /** 长按卡片（弹详情卡） */
    onLongClick: (() -> Unit)? = null,
    /** 左侧完成勾选框；传了它才画出来 */
    onToggleDone: (() -> Unit)? = null
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
            .combinedClickable(
                enabled = onStartClick == null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        visual.decoration?.invoke(this)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onToggleDone?.let {
                DoneCheckbox(
                    checked = false,
                    tint = textColor,
                    onClick = it,
                    modifier = Modifier.padding(end = 9.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = cardTextStyle(titleSize, textColor, FontWeight.Medium, textShadow),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle ?: "$minutes 分钟",
                    style = cardTextStyle(timeSize, textColor, FontWeight.Normal, textShadow),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val startModifier =
                if (onStartClick != null) Modifier.clickable(onClick = onStartClick) else Modifier
            Text(
                text = startLabel,
                style = cardTextStyle(titleSize, textColor, FontWeight.Normal, textShadow),
                modifier = startModifier.padding(start = 10.dp, top = 6.dp, bottom = 6.dp)
            )
        }
    }
}

/** 卡片左上角的完成勾选框（画在卡片自身文字色上，不引 material 组件） */
@Composable
private fun DoneCheckbox(
    checked: Boolean,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(20.dp)
            .border(1.4.dp, tint.copy(alpha = 0.85f), RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Canvas(Modifier.size(11.dp)) {
                val w = size.width
                val h = size.height
                drawLine(
                    color = tint,
                    start = Offset(w * 0.05f, h * 0.55f),
                    end = Offset(w * 0.38f, h * 0.88f),
                    strokeWidth = w * 0.16f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.38f, h * 0.88f),
                    end = Offset(w * 0.95f, h * 0.12f),
                    strokeWidth = w * 0.16f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
