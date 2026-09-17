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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomato.app.ui.components.AppHeader
import com.tomato.app.ui.components.IconAdd
import com.tomato.app.ui.components.IconBarChart
import com.tomato.app.ui.components.IconMoreVert
import com.tomato.app.ui.components.IconTimer
import com.tomato.app.ui.components.TaskCard
import com.tomato.app.ui.data.DemoTask
import com.tomato.app.ui.data.DemoTasks
import com.tomato.app.ui.theme.BgTodo
import com.tomato.app.ui.theme.TomatoTheme
import com.tomato.app.ui.theme.White

/**
 * 待办页（A. 番茄钟 + B. 任务清单 的入口）。
 * 列表：左右边距 10dp，卡片间距 6dp，顶部 9dp，底部 14dp。
 */
@Composable
fun TodoScreen(
    tasks: List<DemoTask> = DemoTasks,
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
            contentPadding = PaddingValues(horizontal = 10.dp, top = 9.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tasks, key = { it.title }) { task ->
                TaskCard(
                    title = task.title,
                    minutes = task.minutes,
                    visual = task.visual,
                    onClick = { /* TODO(M1)：点击「开始」启动番茄 */ }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TodoScreenPreview() {
    TomatoTheme { TodoScreen() }
}
