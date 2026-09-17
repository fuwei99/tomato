package com.tomato.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.TealPrimaryDark
import com.tomato.app.ui.theme.White

/**
 * 添加待办 / 高级设置 对话框里用到的通用小组件。
 * 配色对照截图：选中 = 浅蓝底 + teal 字；未选中 = 浅灰底 + 深灰字。
 */

private val ChipOnBg = Color(0xFFDDEBFA)
private val ChipOffBg = Color(0xFFF2F3F5)
private val ChipOffText = Color(0xFF5A6670)
private val UnderlineColor = Color(0xFF1A9E96)

/** 单个胶囊选项 */
@Composable
fun ChipItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) ChipOnBg else ChipOffBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (selected) TealPrimaryDark else ChipOffText,
            maxLines = 1
        )
    }
}

/**
 * 一行胶囊选项（等分宽度，保证三列对齐）。
 * [options] 与 [selectedIndex] 配套，回调给出 index。
 */
@Composable
fun ChipRow(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { i, text ->
            ChipItem(
                text = text,
                selected = i == selectedIndex,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(i) }
            )
        }
    }
}

/** 问号提示圆点（截图里输入框和「单次预期循环次数」右侧的小 ?） */
@Composable
fun PromptDot(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    color: Color = Color(0xFF37474F),
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(1.2.dp, color, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "?",
            fontSize = (size.value * 0.62f).sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 下划线输入行：左侧标签 / 大号 teal 占位符 + 底部 teal 细线 + 右侧可选小图标。
 * 空值时显示 teal 色占位文字（与截图一致），有值时显示深色内容。
 */
@Composable
fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    tealText: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    androidx.compose.foundation.layout.Column(modifier.fillMaxWidth()) {
        label?.let {
            Text(text = it, fontSize = 13.sp, color = Color(0xFF37474F))
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = if (tealText && value.isEmpty()) UnderlineColor else InkMain
                    ),
                    cursorBrush = SolidColor(UnderlineColor),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                )
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 16.sp,
                        color = if (tealText) UnderlineColor else Color(0xFF9AA5AD),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
            trailing?.let {
                Spacer(Modifier.width(6.dp))
                it()
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(UnderlineColor)
        )
    }
}

/** 圆形浅底图标按钮（详情卡右上那三个） */
@Composable
fun CircleIconButton(
    size: Dp = 44.dp,
    background: Color = Color(0xFFEEF3F4),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/** 自绘方形勾选框（高级设置里的「完成后第二天不再显示」） */
@Composable
fun SquareCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    accent: Color = TealPrimary
) {
    Box(
        modifier
            .size(size)
            .border(1.4.dp, if (checked) accent else Color(0xFF5A6670), RoundedCornerShape(2.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            IconCheck(tint = accent, iconSize = size - 4.dp)
        }
    }
}

/** 对话框里的链接式小按钮（「展开更多高级设置」） */
@Composable
fun DialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = ChipOnBg,
    textColor: Color = TealPrimary,
    fontSize: Int = 12
) {
    Box(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = 120.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = LocalTextStyle.current
        )
    }
}

/** 详情卡里的通用胶囊（编辑 / 排序·移动 / 删除 / 专注历史记录…） */
@Composable
fun CapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = ChipOffBg,
    textColor: Color = ChipOffText,
    fontSize: Int = 14,
    leading: (@Composable () -> Unit)? = null,
    bold: Boolean = false
) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.let {
            it()
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = fontSize.sp,
            color = textColor,
            fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1
        )
    }
}
