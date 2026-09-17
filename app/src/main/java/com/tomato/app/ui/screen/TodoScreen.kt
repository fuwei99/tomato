package com.tomato.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.ui.components.AppHeader
import com.tomato.app.ui.components.IconAdd
import com.tomato.app.ui.components.IconBarChart
import com.tomato.app.ui.components.IconMoreVert
import com.tomato.app.ui.components.IconTimer
import com.tomato.app.ui.components.TaskCard
import com.tomato.app.ui.data.TaskStyles
import com.tomato.app.ui.theme.BgTodo
import com.tomato.app.ui.theme.White

/**
 * 待办页（A. 番茄钟 + B. 任务清单 的入口）。
 * 数据来自 Room（Graph.repository.tasks）。
 * 列表：左右边距 10dp，卡片间距 6dp，顶部 9dp，底部 14dp。
 */
@Composable
fun TodoScreen(
    tasks: List<TaskEntity>,
    onStartFocus: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(BgTodo)) {
        AppHeader(title = "待办") {
            IconBarChart(tint = White, iconSize = 20.dp)
            IconTimer(tint = White, iconSize = 20.dp)
            IconAdd(tint = White, iconSize = 20.dp)
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
                    onStartClick = { onStartFocus(task) }
                )
            }
        }
    }
}
