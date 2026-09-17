package com.tomato.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.tomato.app.data.Graph
import com.tomato.app.focus.FocusTimer
import com.tomato.app.ui.TomatoApp
import com.tomato.app.ui.nav.Tab
import com.tomato.app.ui.screen.FocusScreen
import com.tomato.app.ui.theme.TomatoTheme
import kotlinx.coroutines.launch

/**
 * 入口。
 * - edge-to-edge：状态栏 / 导航栏由各页用真实 WindowInsets 处理。
 * - Room：Graph.init 建库，首启写入种子数据。
 * - 专注页：FocusTimer 单例持有计时状态，返回列表后计时继续。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        Graph.init(this)
        lifecycleScope.launch { Graph.ensureSeed() }

        setContent {
            TomatoTheme {
                var tab by remember { mutableStateOf(Tab.TODO) }
                // true = 用户从专注页返回列表（计时仍在后台跑）
                var focusHidden by remember { mutableStateOf(false) }

                val focus by FocusTimer.state.collectAsState()
                val showFocus = focus.active && !focusHidden

                BackHandler(enabled = showFocus) { focusHidden = true }

                if (showFocus) {
                    FocusScreen(
                        state = focus,
                        onMinimize = { focusHidden = true },
                        onToggle = { if (focus.running) FocusTimer.pause() else FocusTimer.resume() },
                        onFinishRound = { FocusTimer.finishManually() },
                        onReset = { FocusTimer.reset() },
                        onGiveUp = {
                            FocusTimer.stop()
                            focusHidden = false
                        },
                        onFinish = {
                            FocusTimer.stop()
                            focusHidden = false
                        }
                    )
                } else {
                    TomatoApp(
                        currentTab = tab,
                        onTabSelected = { tab = it },
                        onStartFocus = { task ->
                            focusHidden = false
                            FocusTimer.start(task)
                        }
                    )
                }
            }
        }
    }
}
