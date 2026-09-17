package com.tomato.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tomato.app.data.Graph
import com.tomato.app.data.db.TaskEntity
import com.tomato.app.ui.components.BottomNavBar
import com.tomato.app.ui.nav.Tab
import com.tomato.app.ui.screen.CollectionsScreen
import com.tomato.app.ui.screen.PlaceholderScreen
import com.tomato.app.ui.screen.StatsScreen
import com.tomato.app.ui.screen.TodoScreen

/**
 * 外壳：内容区（各自带 AppHeader + 真实 WindowInsets）+ 底部导航。
 * 列表数据统一在这里从 Room 收集，向下传递。
 */
@Composable
fun TomatoApp(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onStartFocus: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by Graph.repository.tasks.collectAsState(initial = emptyList())
    val groups by Graph.repository.groups.collectAsState(initial = emptyList())

    Column(modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (currentTab) {
                Tab.TODO -> TodoScreen(tasks = tasks, onStartFocus = onStartFocus)
                Tab.COLLECTIONS -> CollectionsScreen(groups = groups, onStartFocus = onStartFocus)
                Tab.LOCK -> PlaceholderScreen(tab = Tab.LOCK)
                Tab.STATS -> StatsScreen()
                Tab.ME -> PlaceholderScreen(tab = Tab.ME)
            }
        }
        BottomNavBar(current = currentTab, onSelect = onTabSelected)
    }
}
