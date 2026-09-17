package com.tomato.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tomato.app.data.Graph
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.data.model.TimerMode
import com.tomato.app.ui.components.AppHeader
import com.tomato.app.ui.components.IconAdd
import com.tomato.app.ui.components.IconBarChart
import com.tomato.app.ui.components.IconMoreVert
import com.tomato.app.ui.components.IconTimer
import com.tomato.app.ui.components.TaskCard
import com.tomato.app.ui.data.TaskStyles
import com.tomato.app.ui.theme.BgTodo
import com.tomato.app.ui.theme.White
import kotlinx.coroutines.launch

/**
 * 待办页（A. 番茄钟 + B. 任务清单 的入口）。
 *
 * 交互：
 * - 点卡片上的「开始」→ 进入专注页
 * - 长按卡片 → 弹出详情卡（编辑 / 排序·移动 / 删除 / 专注历史 / 数据统计）
 * - 点头部 `+` → 新建待办
 *
 * 列表：左右边距 10dp，卡片间距 6dp，顶部 9dp，底部 14dp。
 */
@Composable
fun TodoScreen(
    tasks: List<TaskEntity>,
    onStartFocus: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialog by remember { mutableStateOf<TodoDialog?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize().background(BgTodo)) {
        AppHeader(title = "待办") {
            IconBarChart(tint = White, iconSize = 20.dp)
            IconTimer(tint = White, iconSize = 20.dp)
            IconAdd(
                tint = White,
                iconSize = 20.dp,
                modifier = Modifier.clickable { dialog = TodoDialog.Add(null) }
            )
            IconMoreVert(tint = White, iconSize = 20.dp)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 9.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    title = task.title,
                    minutes = task.minutes,
                    visual = TaskStyles[task.styleKey],
                    subtitle = subtitleOf(task),
                    startLabel = if (task.done) "✓" else "开始",
                    onStartClick = { onStartFocus(task) },
                    onLongClick = { dialog = TodoDialog.Detail(task.id) },
                    onToggleDone = { scope.launch { Graph.repository.setDone(task.id, !task.done) } }
                )
            }
        }
    }

    TodoDialogHost(
        tasks = tasks,
        dialog = dialog,
        onDialogChange = { dialog = it },
        onStartFocus = onStartFocus
    )
}

internal fun subtitleOf(task: TaskEntity): String? {
    val mode = TimerMode.from(task.timerMode)
    val parts = mutableListOf<String>()
    parts += if (mode == TimerMode.NONE) "不计时" else "${task.minutes} 分钟"
    if (mode == TimerMode.STOPWATCH) parts += "正向"
    if (task.cycleTarget > 1) parts += "${task.cycleTarget}轮"
    return if (parts.size == 1) null else parts.joinToString(" · ")
}
