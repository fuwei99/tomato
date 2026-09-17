package com.tomato.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.db.TaskListEntity
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.InkSub
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.TealPrimaryDark

/**
 * 排序 / 移动（弹窗式）。
 *
 * 上下移动、置顶置底、移动到指定待办集 —— 全部走 Repository 的
 * moveTaskUp / moveTaskDown / moveTaskToEdge / moveTaskToList。
 */
@Composable
fun SortMoveDialog(
    task: TaskEntity,
    lists: List<TaskListEntity>,
    siblingIds: List<Long>,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveTop: () -> Unit,
    onMoveBottom: () -> Unit,
    onMoveToList: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val index = siblingIds.indexOf(task.id)
    val canUp = index > 0
    val canDown = index in 0 until siblingIds.lastIndex

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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "排序与移动",
                    fontSize = 17.sp,
                    color = InkMain,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
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
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
            ) {
                Text(text = task.title, fontSize = 13.sp, color = InkSub, maxLines = 1)
                Spacer(Modifier.height(12.dp))

                // ===== 位置微调 =====
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortAction("上移", enabled = canUp, onClick = onMoveUp, modifier = Modifier.weight(1f))
                    SortAction("下移", enabled = canDown, onClick = onMoveDown, modifier = Modifier.weight(1f))
                    SortAction("置顶", enabled = canUp, onClick = onMoveTop, modifier = Modifier.weight(1f))
                    SortAction("置底", enabled = canDown, onClick = onMoveBottom, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(18.dp))

                // ===== 移动到待办集 =====
                Text(text = "移动到待办集", fontSize = 13.sp, color = InkMain)
                Spacer(Modifier.height(8.dp))
                lists.forEach { l ->
                    val current = l.id == task.listId
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (current) Color(0xFFF2F3F5) else Color.Transparent)
                            .clickable(enabled = !current) { onMoveToList(l.id) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(
                                    if (current) TealPrimary else Color(0xFFD6DDE0),
                                    RoundedCornerShape(50)
                                )
                        )
                        Text(
                            text = l.name,
                            fontSize = 14.sp,
                            color = if (current) TealPrimaryDark else InkMain,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        )
                        if (current) {
                            Text(text = "当前", fontSize = 11.sp, color = InkSub)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "拖动排序在下一版提供；这一版用上面的按钮调整顺序。",
                    fontSize = 10.sp,
                    color = InkSub
                )
            }
        }
    }
}

@Composable
private fun SortAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) Color(0xFFDDEBFA) else Color(0xFFF2F3F5))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (enabled) TealPrimaryDark else Color(0xFFB0B8BD)
        )
    }
}
