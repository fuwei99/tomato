package com.tomato.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.data.TaskGroup
import com.tomato.app.ui.components.AppHeader
import com.tomato.app.ui.components.IconAdd
import com.tomato.app.ui.components.IconAddPlus
import com.tomato.app.ui.components.IconBarChart
import com.tomato.app.ui.components.IconChevronDown
import com.tomato.app.ui.components.IconMoreVert
import com.tomato.app.ui.components.IconPie
import com.tomato.app.ui.components.IconSettings
import com.tomato.app.ui.components.IconTimer
import com.tomato.app.ui.components.TaskCard
import com.tomato.app.ui.data.TaskStyles
import com.tomato.app.ui.theme.BgCollections
import com.tomato.app.ui.theme.CollIcon
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.White

/**
 * 待办集：列表头（48dp 白底 + 左侧 4×26dp 竖条）+ 卡片（高 53dp、圆角 6dp）。
 *
 * 默认**全部折叠**，点头部左侧的箭头按钮展开/收起（带旋转 + 高度动画）。
 * 卡片区左右内边距 4dp、间距 5dp。
 */
@Composable
fun CollectionsScreen(
    groups: List<TaskGroup>,
    onStartFocus: (com.tomato.app.data.db.TaskEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 默认折叠：展开的待办集 id 集合，初始为空
    var expandedIds by remember { mutableStateOf(setOf<Long>()) }
    var dialog by remember { mutableStateOf<TodoDialog?>(null) }
    val allTasks = remember(groups) { groups.flatMap { it.tasks } }

    Column(modifier.fillMaxSize().background(BgCollections)) {
        AppHeader(title = "待办集") {
            IconBarChart(tint = White, iconSize = 20.dp)
            IconTimer(tint = White, iconSize = 20.dp)
            IconAddPlus(
                tint = White,
                iconSize = 20.dp,
                modifier = Modifier.clickable { dialog = TodoDialog.Add(null) }
            )
            IconMoreVert(tint = White, iconSize = 20.dp)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(groups, key = { it.list.id }) { group ->
                val expanded = group.list.id in expandedIds
                CollectionListHeader(
                    name = group.list.name,
                    count = group.tasks.size,
                    expanded = expanded,
                    onToggle = {
                        expandedIds = if (expanded) expandedIds - group.list.id
                        else expandedIds + group.list.id
                    }
                )
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        group.tasks.forEach { task ->
                            TaskCard(
                                title = task.title,
                                minutes = task.minutes,
                                visual = TaskStyles[task.styleKey],
                                height = 53.dp,
                                cornerRadius = 6.dp,
                                titleSize = 14.sp,
                                timeSize = 11.sp,
                                contentPadding = PaddingValues(
                                    start = 12.dp, end = 12.dp, top = 9.dp, bottom = 8.dp
                                ),
                                onStartClick = { onStartFocus(task) },
                                onLongClick = { dialog = TodoDialog.Detail(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    TodoDialogHost(
        tasks = allTasks,
        dialog = dialog,
        onDialogChange = { dialog = it },
        onStartFocus = onStartFocus
    )
}

/**
 * 列表头：高 48dp，白底，左侧 teal 竖条 4×26dp（右圆角 2dp），距标题 10dp。
 * 最左侧箭头为展开/收起开关（默认收起）。
 */
@Composable
fun CollectionListHeader(
    name: String,
    count: Int = 0,
    expanded: Boolean = false,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val chevronAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "chevron"
    )

    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(1.dp)
            .background(White)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(width = 4.dp, height = 26.dp)
                .background(
                    TealPrimary,
                    RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = InkMain,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        )
        Text(
            text = count.toString(),
            fontSize = 11.sp,
            color = CollIcon,
            modifier = Modifier.padding(end = 12.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 展开 / 收起按钮
            Box(
                Modifier
                    .size(24.dp)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                IconChevronDown(
                    tint = CollIcon,
                    iconSize = 17.dp,
                    modifier = Modifier.rotate(chevronAngle)
                )
            }
            IconPie(tint = CollIcon, iconSize = 17.dp)
            IconSettings(tint = CollIcon, iconSize = 17.dp)
            IconAdd(tint = CollIcon, iconSize = 17.dp)
        }
    }
}
