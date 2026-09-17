package com.tomato.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.app.ui.theme.TealHeaderEnd
import com.tomato.app.ui.theme.TealHeaderMid
import com.tomato.app.ui.theme.TealHeaderStart
import com.tomato.app.ui.theme.White

/** 头部渐变：起 #008A8A → 中 #12A09C → 止 #1FA7A3（实测） */
private val HeaderGradient = Brush.verticalGradient(
    listOf(TealHeaderStart, TealHeaderMid, TealHeaderEnd)
)

/** 头部内容区固定 57dp（设计稿 90dp = 状态栏 33dp + 内容 57dp） */
private val HeaderContentHeight = 57.dp

/**
 * 番茄TODO 风格头部：真实状态栏高度（WindowInsets.statusBars）+ 内容区 57dp。
 * 需配合 edge-to-edge 使用 —— 状态栏由系统绘制，这里只负责留白。
 */
@Composable
fun AppHeader(
    title: String,
    showStudyModePill: Boolean = true,
    showPermissionText: Boolean = true,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(HeaderGradient)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(HeaderContentHeight)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                if (showStudyModePill) {
                    Spacer(Modifier.height(5.dp))
                    StudyModePill()
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                if (showPermissionText) {
                    Text(
                        text = "必开\n权限",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = White
                    )
                }
                trailing()
            }
        }
    }
}

/** 学霸模式胶囊：10sp 白字 + 白色细描边 */
@Composable
fun StudyModePill(text: String = "点击开启学霸模式") {
    Box(
        Modifier
            .background(Color(0x1F002823), RoundedCornerShape(50))
            .border(1.dp, White, RoundedCornerShape(50))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = White)
    }
}
