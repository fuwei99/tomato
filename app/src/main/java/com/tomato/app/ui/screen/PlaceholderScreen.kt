package com.tomato.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.components.AppHeader
import com.tomato.app.ui.components.IconLock
import com.tomato.app.ui.components.IconPerson
import com.tomato.app.ui.nav.Tab
import com.tomato.app.ui.theme.BgTodo
import com.tomato.app.ui.theme.Placeholder
import com.tomato.app.ui.theme.TomatoTheme
import com.tomato.app.ui.theme.White

/**
 * 占位页：锁机（M4）/ 我的（M5）。
 * 本轮只做 UI 复刻，未提供截图，用图标 + 一行说明代替。
 */
@Composable
fun PlaceholderScreen(
    tab: Tab,
    message: String = "界面复刻演示 · 此页未提供截图",
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(BgTodo)) {
        AppHeader(
            title = tab.label,
            showStudyModePill = false,
            showPermissionText = false
        )
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (tab) {
                Tab.LOCK -> IconLock(tint = Placeholder, iconSize = 54.dp, holeColor = White)
                Tab.ME -> IconPerson(tint = Placeholder, iconSize = 54.dp)
                else -> Spacer(Modifier.height(54.dp))
            }
            Text(
                text = message,
                fontSize = 14.sp,
                color = Placeholder,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LockPlaceholderPreview() {
    TomatoTheme { PlaceholderScreen(tab = Tab.LOCK) }
}
