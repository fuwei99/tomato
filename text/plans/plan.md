# 番茄合体 App 开发计划

> 目标：做一个**本地优先、开源**的 Android 效率工具，把「番茄ToDo」（A. 番茄钟 + B. 任务清单）
> 与「不做手机控」（D. 手机管控）合到一个 App 里，并逐步长出自己的 C 模块（memos/记录）。

---

## 0. 技术选型

| 层 | 选择 | 理由 |
|---|---|---|
| 语言 | Kotlin | 官方首选，Compose 一等公民 |
| UI | Jetpack Compose + Material 3 | 声明式，UI 复刻效率高 |
| 架构 | 单向数据流 + UseCase（MVI-lite） | 状态可预测，便于录屏/调试 |
| 数据 | Room（SQLite）+ DataStore（偏好） | 本地优先，离线可用 |
| 异步 | Kotlin Coroutines + Flow | 与 Room/Compose 天然配合 |
| 依赖注入 | 手动 / Hilt（M1 再定） | demo 阶段不引入 |
| 计时 | 前台服务 + 精确闹钟 + 通知 | 后台可靠是番茄钟的命 |
| 管控 | UsageStats + AccessibilityService + 悬浮窗 | D 模块（M4） |
| 最小版本 | minSdk 26 / targetSdk 35 | 覆盖 Redmi / HyperOS |

---

## 1. 模块划分

| 代号 | 模块 | 里程碑 | 本轮 |
|---|---|---|---|
| **A** | 番茄钟（计时 / 学霸模式 / 白噪音） | M1 | ✅ UI |
| **B** | 任务清单（待办 / 待办集 / 标签 / 提醒） | M2 | ✅ UI |
| C | memos（速记 / 复盘） | M5 | ❌ 暂缓 |
| **D** | 不做手机控（锁机 / 应用限时 / 使用统计） | M4 | ❌ 暂缓 |
| — | 统计（累计/当日/分布图） | M3 | ✅ UI |

---

## 2. 数据模型（草案）

```kotlin
Task            id, listId, title, note, estimatedPomodoros, dueAt,
                remindAt, repeatRule, tags, priority, done, createdAt, sortOrder
TaskList        id, name, color, emoji, archived, sortOrder        // 待办集
PomodoroSession id, taskId, startedAt, endedAt, durationSec, focusSec,
                breakSec, completed, interruptedReason
Tag             id, name, color
Memo            id, content, createdAt, pinned, linkedTaskId
BlockRule       id, scope(apps/时间段), type(锁机/限时/间隔), params, enabled
UsageRecord     id, packageName, date, foregroundSec, openCount
```

---

## 3. 目录结构

```
com.tomato.app
├── MainActivity.kt
├── ui/            theme / components / screen / navigation
├── data/          local(db, dao, entity) / repository / prefs
├── domain/        model / usecase
├── service/       PomodoroService, TimerNotification, AlarmReceiver
├── block/         (M4) UsageStats, BlockAccessibilityService, Overlay
└── util/          time / format / constants
```

---

## 4. 里程碑

| 阶段 | 内容 | 交付判定 |
|---|---|---|
| **M0** | 工程脚手架 + 三屏 UI 1:1 复刻（待办 / 待办集 / 统计）+ 底部导航 | 与截图逐像素对比通过；CI 能打出 APK |
| **M1** | 番茄钟：前台服务计时、通知、精确闹钟、学霸模式开关、番茄记录落库 | 息屏/切后台 25 分钟不掉，结束有通知 |
| **M2** | 任务清单：增删改、待办集、标签、提醒、排序、拖拽 | 数据重启不丢 |
| **M3** | 统计：按日/周/月/自定义聚合，饼图 + 时段分布 + 导出 | 与番茄ToDo 口径一致 |
| **M4** | 不做手机控：UsageStats 统计、锁机、应用限时、无障碍服务 | 锁机期间目标 App 打不开 |
| **M5** | memos、主题/图标、备份导出（JSON）、开源合规 | README + License 完备 |

---

## 5. 权限清单（分阶段申请，用前才要）

| 权限 | 阶段 | 用途 |
|---|---|---|
| `FOREGROUND_SERVICE` / `..._SPECIAL_USE` | M1 | 番茄计时保活 |
| `POST_NOTIFICATIONS` | M1 | 计时通知 |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | M1 | 番茄结束提醒 |
| `RECEIVE_BOOT_COMPLETED` / `VIBRATE` | M2 | 开机恢复提醒 / 震动 |
| `PACKAGE_USAGE_STATS` | M4 | 使用时长统计 |
| `BIND_ACCESSIBILITY_SERVICE` | M4 | 强制锁机 |
| `SYSTEM_ALERT_WINDOW` | M4 | 锁机遮罩 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | M4 | 省电策略白名单 |

原则：**不提前声明、不用不申请**，每个权限都要有可见的用途说明。

---

## 6. HyperOS / MIUI 保活要点

1. 自启动 + 后台弹出界面 + 锁定应用（引导用户手动开，App 内给跳转指引）。
2. 省电策略设为「无限制」，关闭「智能省电」对该 App 的限制。
3. 计时用**前台服务 + 可见通知**，不要依赖 WorkManager 做秒级计时。
4. 结束时间用 `setExactAndAllowWhileIdle` 兜底，服务被杀也能响铃。
5. 提供「权限体检」页，逐项检测并一键跳转。

---

## 7. 约束

- **本地优先**：数据只存本地，不强制登录；备份/导出走用户显式操作。
- **开源**：无第三方埋点、无广告 SDK；依赖全部使用可公开的开源库。
- **可验证**：每个里程碑都要有 CI（编译 + 静态检查）和真机验证清单。
