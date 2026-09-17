package com.tomato.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.tomato.app.ui.TomatoApp
import com.tomato.app.ui.nav.Tab
import com.tomato.app.ui.theme.TomatoTheme

/**
 * Demo 入口：只做界面复刻，无业务逻辑。
 * edge-to-edge：状态栏透明，头部 AppHeader 自己画 33dp 状态栏留白。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 内容绘制到系统栏之下（与 AppHeader 的 33dp 留白配合）
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TomatoTheme {
                var tab by remember { mutableStateOf(Tab.TODO) }
                TomatoApp(currentTab = tab, onTabSelected = { tab = it })
            }
        }
    }
}
