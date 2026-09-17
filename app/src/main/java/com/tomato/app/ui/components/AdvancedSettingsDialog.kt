package com.tomato.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.InkSub

/**
 * 高级设置对话框（截图 3）——叠在「添加待办」之上。
 *
 * 三项：
 * - 完成后第二天不再显示（勾选框）
 * - 任务备注
 * - 单次预期循环次数（一个番茄 = 专注 N 分钟 + 休息 M 分钟，共 K 轮）
 * - 自定义休息时间（分钟）
 */
@Composable
fun AdvancedSettingsDialog(
    draft: TaskDraft,
    onDraftChange: (TaskDraft) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier
                .width(304.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            // ===== 标题栏 =====
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "高级设置",
                    fontSize = 17.sp,
                    color = InkMain,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    IconCheck(tint = InkMain, iconSize = 22.dp)
                }
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    IconClose(tint = InkMain, iconSize = 20.dp)
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 20.dp)
            ) {
                Spacer(Modifier.height(6.dp))

                // ===== 完成后第二天不再显示 =====
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SquareCheckbox(
                        checked = draft.hideNextDay,
                        onClick = { onDraftChange(draft.copy(hideNextDay = !draft.hideNextDay)) }
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "完成后第二天不再显示",
                        fontSize = 14.sp,
                        color = InkMain,
                        modifier = Modifier.clickable {
                            onDraftChange(draft.copy(hideNextDay = !draft.hideNextDay))
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ===== 任务备注 =====
                UnderlineField(
                    value = draft.note,
                    onValueChange = { onDraftChange(draft.copy(note = it)) },
                    placeholder = "任务备注",
                    tealText = true
                )

                Spacer(Modifier.height(18.dp))

                // ===== 单次预期循环次数 =====
                UnderlineField(
                    value = draft.cycleTarget.toString(),
                    onValueChange = { s ->
                        val v = s.filter { it.isDigit() }.take(2)
                        onDraftChange(draft.copy(cycleTarget = (v.toIntOrNull() ?: 1).coerceIn(1, 99)))
                    },
                    placeholder = "单次预期循环次数",
                    tealText = true,
                    keyboardType = KeyboardType.Number,
                    trailing = { PromptDot(size = 18.dp, color = Color(0xFF37474F)) }
                )

                Spacer(Modifier.height(18.dp))

                // ===== 自定义休息时间 =====
                UnderlineField(
                    value = draft.breakMinutes.toString(),
                    onValueChange = { s ->
                        val v = s.filter { it.isDigit() }.take(3)
                        onDraftChange(draft.copy(breakMinutes = (v.toIntOrNull() ?: 0).coerceIn(0, 120)))
                    },
                    placeholder = "自定义休息时间",
                    tealText = true,
                    keyboardType = KeyboardType.Number,
                    trailing = {
                        Text(text = "分钟", fontSize = 11.sp, color = InkSub)
                    }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "循环说明：专注 ${draft.effectiveMinutes} 分钟 + 休息 " +
                        "${draft.breakMinutes.coerceAtLeast(0)} 分钟，共 ${draft.cycleTarget.coerceAtLeast(1)} 轮",
                    fontSize = 10.sp,
                    color = InkSub
                )
            }
        }
    }
}
