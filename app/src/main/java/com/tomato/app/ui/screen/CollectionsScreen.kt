package com.tomato.app.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.tomato.app.ui.data.DemoCollection
import com.tomato.app.ui.data.DemoCollections
import com.tomato.app.ui.theme.BgCollections
import com.tomato.app.ui.theme.CollIcon
import com.tomato.app.ui.theme.InkMain
import com.tomato.app.ui.theme.TealPrimary
import com.tomato.app.ui.theme.TomatoTheme
import com.tomato.app.ui.theme.White

/**
 * 待办集：列表头（48dp 白底 + 左侧 4×26dp 竖条）+ 卡片（高 53dp、圆角 6dp）。
 * 卡片区左右内边距 4dp、间距 5dp。
 */
@Composable
fun CollectionsScreen(
    collections: List<DemoCollection> = DemoCollections,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(BgCollections)) {
        AppHeader(title = "待办集") {
            IconBarChart(tint = White, iconSize = 20.dp)
            IconTimer(tint = White, iconSize = 20.dp)
            IconAddPlus(tint = White, iconSize = 20.dp)
            IconMoreVert(tint = White, iconSize = 20.dp)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 4.dp, top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            collections.forEach { collection ->
                item { CollectionListHeader(name = collection.name) }
                items(collection.tasks, key = { it.title }) { task ->
                    TaskCard(
                        title = task.title,
                        minutes = task.minutes,
                        visual = task.visual,
                        height = 53.dp,
                        cornerRadius = 6.dp,
                        titleSize = 14.sp,
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}

/** 列表头：高 48dp，白底，左侧 teal 竖条 4×26dp（右圆角 2dp），距标题 10dp */
@Composable
fun CollectionListHeader(
    name: String,
    modifier: Modifier = Modifier
) {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconChevronDown(tint = CollIcon, iconSize = 17.dp)
            IconPie(tint = CollIcon, iconSize = 17.dp)
            IconSettings(tint = CollIcon, iconSize = 17.dp)
            IconAdd(tint = CollIcon, iconSize = 17.dp)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionsScreenPreview() {
    TomatoTheme { CollectionsScreen() }
}
