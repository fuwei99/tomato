package com.tomato.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tomato.app.data.Graph
import com.tomato.app.data.db.DayFocusRow
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.db.TaskTotalsRow
import com.tomato.app.ui.components.AddTaskDialog
import com.tomato.app.ui.components.ConfirmDialog
import com.tomato.app.ui.components.SortMoveDialog
import com.tomato.app.ui.components.TaskDetailCard
import kotlinx.coroutines.launch
/** 待办页可能弹出的所有弹层（用一个 sealed class 托管，避免一堆 boolean） */
sealed interface TodoDialog {
    /** 长按详情卡 */
    data class Detail(val taskId: Long) : TodoDialog

    /** 新建（editing = null）或编辑 */
    data class Add(val editing: TaskEntity?) : TodoDialog

    /** 排序 / 移动 */
    data class SortMove(val taskId: Long) : TodoDialog

    /** 删除确认 */
    data class ConfirmDelete(val taskId: Long) : TodoDialog
}

/**
 * 弹层宿主：把「长按详情 → 编辑 / 排序移动 / 删除」这条链路串起来。
 *
 * 用 [TaskEntity] 列表做数据源（待办页与待办集页共用），
 * 所有写操作都直接打 [Graph.repository]，列表由 Room 的 Flow 自动刷新。
 */
@Composable
fun TodoDialogHost(
    tasks: List<TaskEntity>,
    dialog: TodoDialog?,
    onDialogChange: (TodoDialog?) -> Unit,
    onStartFocus: (TaskEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    val lists by Graph.repository.taskLists.collectAsState(initial = emptyList())

    when (val d = dialog) {
        null -> Unit

        is TodoDialog.Detail -> {
            val task = tasks.firstOrNull { it.id == d.taskId }
            if (task == null) {
                // 任务被删了 / 列表刷新掉了，自动关闭
                LaunchedEffect(d.taskId) { onDialogChange(null) }
                return
            }

            val totals by remember(task.id) { Graph.repository.taskTotals(task.id) }
                .collectAsState(initial = TaskTotalsRow.Empty)
            val weekRows by remember { Graph.repository.weekFocus() }
                .collectAsState(initial = emptyList<DayFocusRow>())

            TaskDetailCard(
                task = task,
                totals = totals,
                weekDots = weekDotFlags(weekRows),
                onDismiss = { onDialogChange(null) },
                onEdit = { onDialogChange(TodoDialog.Add(task)) },
                onSortMove = { onDialogChange(TodoDialog.SortMove(task.id)) },
                onDelete = { onDialogChange(TodoDialog.ConfirmDelete(task.id)) },
                onHistory = { /* 专注历史记录：下一版做独立页 */ },
                onStats = { /* 数据统计：下一版做单任务统计页 */ },
                onTimerSettings = { onDialogChange(TodoDialog.Add(task)) },
                onBackground = { /* 更换背景：下一版做样式选择器 */ },
                onWhitelist = { /* 独立白名单：M4 随锁机一起做 */ }
            )
        }

        is TodoDialog.Add -> {
            AddTaskDialog(
                lists = lists,
                initial = d.editing,
                onDismiss = { onDialogChange(null) },
                onConfirm = { draft ->
                    scope.launch {
                        if (draft.isEditing) {
                            val origin = tasks.firstOrNull { it.id == draft.id }
                            if (origin != null) {
                                Graph.repository.updateTask(
                                    origin.copy(
                                        title = draft.title.trim(),
                                        minutes = draft.effectiveMinutes,
                                        note = draft.note,
                                        styleKey = draft.styleKey,
                                        kind = draft.kind.ordinal,
                                        timerMode = draft.timerMode.ordinal,
                                        durationPreset = draft.preset.ordinal,
                                        cycleTarget = draft.cycleTarget.coerceAtLeast(1),
                                        breakMinutes = draft.breakMinutes.coerceAtLeast(0),
                                        hideNextDay = draft.hideNextDay
                                    )
                                )
                            }
                        } else {
                            Graph.repository.insertTask(
                                listId = draft.listId,
                                title = draft.title.trim(),
                                minutes = draft.effectiveMinutes,
                                kind = draft.kind,
                                timerMode = draft.timerMode,
                                preset = draft.preset,
                                cycleTarget = draft.cycleTarget,
                                breakMinutes = draft.breakMinutes,
                                hideNextDay = draft.hideNextDay,
                                note = draft.note,
                                styleKey = draft.styleKey
                            )
                        }
                        onDialogChange(null)
                    }
                }
            )
        }

        is TodoDialog.SortMove -> {
            val task = tasks.firstOrNull { it.id == d.taskId }
            if (task == null) {
                LaunchedEffect(d.taskId) { onDialogChange(null) }
                return
            }
            var siblingIds by remember(task.id) { mutableStateOf<List<Long>>(emptyList()) }
            LaunchedEffect(task.id, tasks) {
                siblingIds = Graph.repository.siblingIds(task.id)
            }

            SortMoveDialog(
                task = task,
                lists = lists,
                siblingIds = siblingIds,
                onMoveUp = { scope.launch { Graph.repository.moveTaskUp(task.id) } },
                onMoveDown = { scope.launch { Graph.repository.moveTaskDown(task.id) } },
                onMoveTop = { scope.launch { Graph.repository.moveTaskToEdge(task.id, toTop = true) } },
                onMoveBottom = { scope.launch { Graph.repository.moveTaskToEdge(task.id, toTop = false) } },
                onMoveToList = { target ->
                    scope.launch {
                        Graph.repository.moveTaskToList(task.id, target)
                        onDialogChange(null)
                    }
                },
                onDismiss = { onDialogChange(TodoDialog.Detail(task.id)) }
            )
        }

        is TodoDialog.ConfirmDelete -> {
            val task = tasks.firstOrNull { it.id == d.taskId }
            ConfirmDialog(
                title = "确认删除「${task?.title ?: "该待办"}」？",
                message = "删除后该待办的专注记录仍会保留在统计中。",
                onConfirm = {
                    scope.launch {
                        Graph.repository.deleteTask(d.taskId)
                        onDialogChange(null)
                    }
                },
                onDismiss = { onDialogChange(TodoDialog.Detail(d.taskId)) }
            )
        }
    }
}

/** 周一到周日 7 天是否有记录（无数据时全 false，仍画出 7 个灰点） */
private fun weekDotFlags(rows: List<DayFocusRow>): List<Boolean> {
    val map = rows.associate { it.dayIndex to it.sec }
    return (0..6).map { (map[it] ?: 0L) > 0L }
}
