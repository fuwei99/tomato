package com.tomato.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tomato.app.ui.components.BottomNavBar
import com.tomato.app.ui.nav.Tab
import com.tomato.app.ui.screen.CollectionsScreen
import com.tomato.app.ui.screen.PlaceholderScreen
import com.tomato.app.ui.screen.StatsScreen
import com.tomato.app.ui.screen.TodoScreen

/**
 * Demo 外壳：内容区（各自带 AppHeader）+ 底部 52dp 导航。
 * 状态栏由 AppHeader 自行绘制 33dp 留白，配合 edge-to-edge 使用。
 */
@Composable
fun TomatoApp(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (currentTab) {
                Tab.TODO -> TodoScreen()
                Tab.COLLECTIONS -> CollectionsScreen()
                Tab.LOCK -> PlaceholderScreen(tab = Tab.LOCK)
                Tab.STATS -> StatsScreen()
                Tab.ME -> PlaceholderScreen(tab = Tab.ME)
            }
        }
        BottomNavBar(current = currentTab, onSelect = onTabSelected)
    }
}
