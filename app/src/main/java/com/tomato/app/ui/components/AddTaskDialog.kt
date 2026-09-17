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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.db.TaskListEntity
import com.tomato.app.data.model.DurationPreset
import com.tomato.app.data.model.TaskKind
import com.tomato.app.data.model.TimerMode
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.InkSub
import com.tomato.app.ui.theme.TealPrimary

/**
 * 新建 / 编辑任务时在 UI 上流转的数据（尚未落库）。
 */
data class TaskDraft(
    val id: Long = 0L,
    val listId: Long = 1L,
    val title: String = "",
    val kind: TaskKind = TaskKind.POMODORO,
    val timerMode: TimerMode = TimerMode.COUNTDOWN,
    val preset: DurationPreset = DurationPreset.M25,
    val minutes: Int = 25,
    val cycleTarget: Int = 1,
    val breakMinutes: Int = 5,
    val note: String = "",
    val hideNextDay: Boolean = false,
    val styleKey: String = "leaf"
) {
    val isEditing: Boolean get() = id > 0L

    /** 时长：自定义时用输入框，否则用预设 */
    val effectiveMinutes: Int
        get() = if (preset == DurationPreset.CUSTOM) minutes.coerceAtLeast(1) else preset.minutes

    companion object {
        fun from(task: TaskEntity) = TaskDraft(
            id = task.id,
            listId = task.listId,
            title = task.title,
            kind = TaskKind.from(task.kind),
            timerMode = TimerMode.from(task.timerMode),
            preset = DurationPreset.from(task.durationPreset),
            minutes = task.minutes,
            cycleTarget = task.cycleTarget,
            breakMinutes = task.breakMinutes,
            note = task.note,
            hideNextDay = task.hideNextDay,
            styleKey = task.styleKey
        )
    }
}

/**
 * 添加 / 编辑待办对话框（截图 2）。
 *
 * 三级胶囊：类型 / 计时方式 / 时长，加底部「展开更多高级设置」。
 * 高级设置是**叠在这个 Dialog 之上的另一个 Dialog**（见 AdvancedSettingsDialog），
 * 这样和图里「小卡片压在大卡片上」的层次一致。
 */
@Composable
fun AddTaskDialog(
    lists: List<TaskListEntity>,
    initial: TaskEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (TaskDraft) -> Unit
) {
    var draft by remember(initial?.id) {
        mutableStateOf(initial?.let { TaskDraft.from(it) } ?: TaskDraft())
    }
    var showAdvanced by remember { mutableStateOf(false) }

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
                    .padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (draft.isEditing) "编辑待办" else "添加待办",
                    fontSize = 17.sp,
                    color = InkMain,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            if (draft.title.isNotBlank()) onConfirm(draft)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    IconCheck(
                        tint = if (draft.title.isNotBlank()) InkMain else Color(0xFFBFC7CB),
                        iconSize = 22.dp
                    )
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
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 18.dp, end = 18.dp, bottom = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ===== 任务名称 =====
                UnderlineField(
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    placeholder = "请输入待办名称",
                    tealText = true,
                    trailing = { PromptDot(size = 18.dp, color = Color(0xFF37474F)) }
                )

                Spacer(Modifier.height(18.dp))

                // ===== 类型 =====
                ChipRow(
                    options = TaskKind.entries.map { it.label },
                    selectedIndex = draft.kind.ordinal,
                    onSelect = { draft = draft.copy(kind = TaskKind.entries[it]) }
                )

                Spacer(Modifier.height(10.dp))

                // ===== 计时方式 =====
                ChipRow(
                    options = TimerMode.entries.map { it.label },
                    selectedIndex = draft.timerMode.ordinal,
                    onSelect = { draft = draft.copy(timerMode = TimerMode.entries[it]) }
                )

                Spacer(Modifier.height(10.dp))

                // ===== 时长 =====
                ChipRow(
                    options = DurationPreset.entries.map { it.label },
                    selectedIndex = draft.preset.ordinal,
                    onSelect = {
                        val p = DurationPreset.entries[it]
                        draft = draft.copy(
                            preset = p,
                            minutes = if (p == DurationPreset.CUSTOM) draft.minutes else p.minutes
                        )
                    }
                )

                // 选「自定义」时补一个分钟输入
                if (draft.preset == DurationPreset.CUSTOM) {
                    Spacer(Modifier.height(12.dp))
                    UnderlineField(
                        value = draft.minutes.toString(),
                        onValueChange = { s ->
                            val v = s.filter { it.isDigit() }.take(4)
                            draft = draft.copy(minutes = v.toIntOrNull() ?: 0)
                        },
                        label = "自定义时长（分钟）",
                        keyboardType = KeyboardType.Number
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ===== 归属待办集（截图里没有，但新建任务必须知道放到哪）=====
                if (lists.size > 1 || draft.isEditing) {
                    Text(text = "归属待办集", fontSize = 12.sp, color = InkSub)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        lists.take(2).forEach { l ->
                            ChipItem(
                                text = l.name,
                                selected = l.id == draft.listId,
                                modifier = Modifier.weight(1f),
                                onClick = { draft = draft.copy(listId = l.id) }
                            )
                        }
                        if (lists.size < 2) Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "*倒计时25分钟即标准番茄钟时间",
                    fontSize = 10.sp,
                    color = InkSub,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // ===== 展开更多高级设置 =====
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DialogTextButton(
                        text = "展开更多高级设置",
                        onClick = { showAdvanced = true }
                    )
                }
            }
        }
    }

    // 叠在上面的高级设置
    if (showAdvanced) {
        AdvancedSettingsDialog(
            draft = draft,
            onDraftChange = { draft = it },
            onDismiss = { showAdvanced = false },
            onConfirm = {
                showAdvanced = false
                if (draft.title.isNotBlank()) onConfirm(draft)
            }
        )
    }
}
