package com.tomato.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.nav.Tab
import com.tomato.app.ui.theme.NavDivider
import com.tomato.app.ui.theme.NavInactive
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.White

/**
 * 底部导航：总高 52dp，白底、顶部 0.5dp 分割线、底部 2dp 内边距。
 * 每项：图标 20dp + 文字 10sp（选中加粗），图标与文字间距 2dp。
 */
@Composable
fun BottomNavBar(
    current: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth().background(White)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(NavDivider)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(bottom = 2.dp)
        ) {
            Tab.entries.forEach { tab ->
                NavItem(
                    tab = tab,
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) TealPrimary else NavInactive
    Column(
        modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
    ) {
        NavIcon(tab = tab, tint = tint)
        Text(
            text = tab.label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = tint
        )
    }
}

@Composable
private fun NavIcon(tab: Tab, tint: Color) {
    val size = 20.dp
    when (tab) {
        Tab.TODO -> IconTodo(tint = tint, iconSize = size)
        Tab.COLLECTIONS -> IconChecklist(tint = tint, iconSize = size)
        Tab.LOCK -> IconLock(tint = tint, iconSize = size, holeColor = White)
        Tab.STATS -> IconPie(tint = tint, iconSize = size)
        Tab.ME -> IconPerson(tint = tint, iconSize = size)
    }
}
