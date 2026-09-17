package com.tomato.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.tomato.app.data.db.TaskTotalsRow
import com.tomato.app.data.model.TaskKind
import com.tomato.app.data.model.TimerMode
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.InkSub
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.TealPrimaryDark

/**
 * 长按待办卡片弹出的详情卡（截图 1）。
 *
 * 宽度 296dp，浮在列表之上、距底部留白，用 Dialog 实现而不是 BottomSheet
 * —— 因为「编辑」时要能在它之上再叠「编辑待办」对话框。
 */
@Composable
fun TaskDetailCard(
    task: TaskEntity,
    totals: TaskTotalsRow,
    weekDots: List<Boolean>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onSortMove: () -> Unit,
    onDelete: () -> Unit,
    onHistory: () -> Unit,
    onStats: () -> Unit,
    onTimerSettings: () -> Unit,
    onBackground: () -> Unit,
    onWhitelist: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(Modifier.fillMaxSize()) {
            // 遮罩区域：点击空白关闭
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0x33000000))
                    .clickable(onClick = onDismiss)
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 66.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xD9FFFFFF))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp)
                ) {
                    // ===== 标题 + 三个圆图标 =====
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkMain,
                                maxLines = 2
                            )
                            Text(
                                text = describe(task),
                                fontSize = 10.sp,
                                color = InkSub,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MiniCircleAction("定时功能", onTimerSettings) {
                                IconAlarm(tint = TealPrimaryDark, iconSize = 19.dp)
                            }
                            MiniCircleAction("更换背景", onBackground) {
                                IconPicture(tint = TealPrimaryDark, iconSize = 19.dp)
                            }
                            MiniCircleAction("独立白名单", onWhitelist) {
                                IconPhone(tint = TealPrimaryDark, iconSize = 19.dp)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // ===== 编辑 / 排序·移动 / 删除 =====
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        CapsuleButton(
                            text = "编辑",
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            background = Color(0xFFDCE9F5),
                            textColor = Color(0xFF2E5C6E)
                        )
                        CapsuleButton(
                            text = "排序 | 移动",
                            onClick = onSortMove,
                            modifier = Modifier.weight(1f),
                            background = Color(0xFFEDEDED),
                            textColor = Color(0xFF37474F)
                        )
                        CapsuleButton(
                            text = "删除",
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            background = Color(0xFFFBE2DC),
                            textColor = Color(0xFFD9503F)
                        )
                    }

                    Spacer(Modifier.height(9.dp))

                    // ===== 专注历史记录 / 数据统计 =====
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        CapsuleButton(
                            text = "专注历史记录",
                            onClick = onHistory,
                            modifier = Modifier.weight(1f),
                            background = Color(0xFFEDF4F1),
                            textColor = TealPrimaryDark,
                            fontSize = 15,
                            leading = { IconHistory(tint = TealPrimaryDark, iconSize = 19.dp) }
                        )
                        CapsuleButton(
                            text = "数据统计",
                            onClick = onStats,
                            modifier = Modifier.weight(1f),
                            background = Color(0xFFEDF4F1),
                            textColor = TealPrimaryDark,
                            fontSize = 15,
                            leading = { IconPie(tint = TealPrimaryDark, iconSize = 19.dp) }
                        )
                    }

                    Spacer(Modifier.height(9.dp))

                    WeekHeatRow(dots = weekDots)

                    Spacer(Modifier.height(9.dp))

                    // ===== 累计专注 =====
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F9F9))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(text = "累计专注", fontSize = 13.sp, color = InkMain)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            BigNumber(
                                value = totals.count.toString(),
                                unit = "次",
                                color = Color(0xFF2A6FBF)
                            )
                            Spacer(Modifier.width(34.dp))
                            val (h, m) = splitHm(totals.sec)
                            Row(verticalAlignment = Alignment.Bottom) {
                                BigNumText(h.toString(), TealPrimaryDark)
                                UnitText("小时")
                                Spacer(Modifier.width(4.dp))
                                BigNumText(m.toString(), TealPrimaryDark)
                                UnitText("分钟")
                            }
                        }
                    }

                    Spacer(Modifier.height(9.dp))

                    // ===== 定时功能 =====
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F9F9))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(text = "定时功能", fontSize = 13.sp, color = InkMain)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "没有设置定时提醒或定时学霸",
                            fontSize = 11.sp,
                            color = InkSub
                        )
                    }
                }
            }
        }
    }
}

// ===== 内部小组件 =====

@Composable
private fun MiniCircleAction(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircleIconButton(size = 38.dp, background = Color(0xFFEDF1F2), onClick = onClick) {
            icon()
        }
        Text(
            text = label,
            fontSize = 8.5.sp,
            color = Color(0xFF4B5F66),
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun BigNumber(value: String, unit: String, color: Color) {
    Row(verticalAlignment = Alignment.Bottom) {
        BigNumText(value, color)
        UnitText(unit)
    }
}

@Composable
private fun BigNumText(value: String, color: Color) {
    Text(
        text = value,
        fontSize = 28.sp,
        fontWeight = FontWeight.Light,
        color = color,
        letterSpacing = (-0.5).sp
    )
}

@Composable
private fun UnitText(unit: String) {
    Text(
        text = unit,
        fontSize = 10.sp,
        color = Color(0xFF4B5F66),
        modifier = Modifier.padding(start = 1.dp, bottom = 3.dp)
    )
}

private fun splitHm(sec: Long): Pair<Long, Long> {
    val totalMin = sec / 60L
    return (totalMin / 60L) to (totalMin % 60L)
}

/** 一行描述：类型 · 计时方式 · 时长 [· 循环] */
private fun describe(task: TaskEntity): String {
    val kind = TaskKind.from(task.kind).label
    val mode = TimerMode.from(task.timerMode)
    val time = when (mode) {
        TimerMode.NONE -> "不计时"
        else -> "${task.minutes} 分钟"
    }
    val cycle = if (task.cycleTarget > 1) " · ${task.cycleTarget} 轮" else ""
    return "$kind · ${mode.label} · $time$cycle"
}
