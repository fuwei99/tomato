# 番茄合体（Tomato）

Android **原生**（Kotlin + Jetpack Compose）实现，目标是对齐「番茄ToDo」的核心体验，并逐步合入「不做手机控」的能力。

当前阶段：**M2 · 可用的番茄钟** —— UI 已复刻，Room 数据层、专注计时（三模式 + 循环休息）、
待办增删改查（含编辑 / 排序移动 / 删除）、统计页全部图表都已接通真实数据。

> ⚠️ 本项目**不做**「不做手机控」（D 模块）与 memos（C 模块），只做番茄TODO 部分：
> A. 番茄钟 + B. 任务清单。锁机 / 我的 两个 tab 仍为占位。

---

## 一、快速开始

```bash
# 需要 JDK 17 + Android SDK（compileSdk 35）
./gradlew :app:assembleDebug
# 或直接用 Android Studio 打开本目录（Gradle Sync 后 Run 'app'）
```

最低支持 **Android 8.0（API 26）**，目标 **API 35**，主要面向 Redmi / HyperOS。

CI：`.github/workflows/android.yml`，push 到 `main` 自动执行
`compileDebugKotlin` → `assembleDebug` → 上传 debug APK 产物（artifact 名 `tomato-debug-apk`）。

---

## 二、目录结构

```
.
├── app/
│   ├── build.gradle.kts              # KSP + Room + Compose
│   └── src/main/java/com/tomato/app/
│       ├── MainActivity.kt           # edge-to-edge 入口 / 专注页路由 / 种子注入
│       ├── data/
│       │   ├── Graph.kt              # 服务定位器（DB 单例）
│       │   ├── Stats.kt              # Repository 门面 + StatsSnapshot / TaskGroup
│       │   ├── model/Enums.kt        # TaskKind / TimerMode / DurationPreset
│       │   └── db/                   # Entities / Daos / TomatoDatabase（Room）
│       ├── focus/FocusTimer.kt       # ★ 计时后端（进程内单例，三模式 + 循环）
│       └── ui/
│           ├── TomatoApp.kt          # 内容区 + 底部导航
│           ├── nav/Tab.kt
│           ├── theme/                # Color / Type / Theme
│           ├── components/
│           │   ├── AppHeader / Icons / FocusIcons / Decorations
│           │   ├── TaskCard.kt       # 任务卡（长按 = 详情卡）
│           │   ├── Charts.kt         # ★ 图表：柱状 / 周热力 / 月历热力 / 24h
│           │   ├── ChoiceChips.kt    # 胶囊 / 下划线输入框 / 勾选框 …
│           │   ├── AddTaskDialog.kt  # ★ 添加 / 编辑待办
│           │   ├── AdvancedSettingsDialog.kt  # ★ 高级设置（叠在上一层的 Dialog）
│           │   ├── TaskDetailCard.kt # ★ 长按详情卡
│           │   ├── SortMoveDialog.kt # ★ 排序 / 移动
│           │   ├── ConfirmDialog.kt  # ★ 二次确认
│           │   └── StatCard / BottomNavBar / FocusScene
│           ├── data/
│           │   ├── DemoData.kt       # TaskStyles 样式表 + DemoSeed 种子
│           │   └── Format.kt         # 时间/时长格式化
│           └── screen/
│               ├── TodoScreen.kt     # 待办页（列表 + 弹层入口）
│               ├── TodoDialogHost.kt # ★ 统一托管所有弹层
│               ├── CollectionsScreen.kt
│               ├── StatsScreen.kt    # ★ 统计页（7 张卡，全部真实数据）
│               ├── FocusScreen.kt    # 专注页
│               └── PlaceholderScreen.kt
├── reference/tomato-ui-reference.html  # 网页高保真复刻
├── text/
│   ├── ui-spec/spec.md               # ★ 尺寸规格：所有 dp/sp/色值来源
│   └── plans/plan.md                 # 总体开发计划（M0~M5）
└── .github/workflows/android.yml
```

---

## 三、数据层（Room）

三张表，`version = 2`（`fallbackToDestructiveMigration`，demo 阶段不写迁移），schema 导出到 `app/schemas/`：

| 表 | 说明 | 关键字段 |
|---|---|---|
| `task_lists` | 待办集 | `id / name / sortOrder / archived` |
| `tasks` | 任务 | `id / listId / title / minutes / styleKey / note / done / sortOrder`<br>`kind / timerMode / durationPreset / cycleTarget / breakMinutes`<br>`hideNextDay / hiddenUntil` |
| `pomodoro_sessions` | 每次番茄 | `id / taskId / taskTitle / startedAt / endedAt / focusSec / breakSec / mode / completed` |

**枚举持久化**：`kind / timerMode / durationPreset / mode` 都存 `ordinal`（`Int`），Entity 字段类型即 `Int`，
UI 侧统一走 `Xxx.from(v)` 转换并对越界值兜底 —— 不写 `@TypeConverter`，KSP 零风险。

**「完成后第二天不再显示」** 用 `hideNextDay`（配置）+ `hiddenUntil`（运行期状态）两列实现：
完成后只写 `hiddenUntil = 次日 0 点`，查询用 `hiddenUntil = 0 OR hiddenUntil <= now` 过滤，到点自动回归，无需回写。

**排序**：`sortOrder` 按索引重写（不是交换原值，避免历史脏数据导致换不动），
`moveTaskUp/Down/ToEdge/ToList` 全部在 `db.withTransaction {}` 里完成。

- 卡片外观**不入库**，只存 `styleKey`，渲染时查 `ui/data/DemoData.kt` 的 `TaskStyles`。
- 首启 `DemoSeed` 注入（与截图一致：2 个待办集 / 12 个任务），只写一次，之后完全是用户数据。
- UI 不直接碰 DAO，统一走 `TomatoRepository`（`Flow` 驱动，列表自动刷新）。

---

## 四、专注页（点击任务卡「开始」进入）

**计时后端 `FocusTimer`**（`focus/FocusTimer.kt`）：

- 用 `SystemClock.elapsedRealtime()` 的**绝对截止时刻**计时，不累加 delta —— 息屏、丢帧、切后台都不走偏。
- 100 ms 一跳刷新 UI，圆环是连续动画而不是每秒跳一格。
- **三种模式**（新建待办时选）：
  - **倒计时**：有终点，归零自动完成；圆环按剩余比例走。
  - **正向计时**：无终点，计时向上累加，控制栏中间变成「完成」，用户手动结束。
  - **不计时**：不显示时间，只显示「专注中」，同样手动结束。
- **循环休息**：一个番茄 = 专注 N 分钟 + 自动休息 M 分钟，共 K 轮（高级设置里配）。
  状态机 `专注 → 休息 → 专注…`，**最后一轮结束不再休息**；圆环在休息阶段换成青绿色，
  多轮时显示「第 i / K 轮」角标。
- 开始即插一条 `completed = false` 的会话行；之后每次暂停 / 重置 / 结束，以及运行中每 15 s 回写一次，异常退出也留痕。
- **有效专注门槛 60 秒**：低于它按「放弃」落库（`completed = false`），不计入统计。
- 返回列表（`BackHandler` 或左上箭头）只是把页面藏起来，**计时继续**。

**页面**：雪原 / 黄昏 / 夜空三套 Canvas 背景（飘雪、云飘移、星星闪烁），
圆环倒计时 + 头部圆点 + 呼吸光晕，底部控制栏 **亮度 / 背景 / 暂停|完成 / 重置 / 放弃**。

---

## 五、待办的三条交互链路

| 入口 | 弹出 | 能做什么 |
|---|---|---|
| 点头部 **`+`** | 「添加待办」对话框（304 dp） | 名称、类型（普通番茄钟/定目标/养习惯）、计时方式（倒计时/正向/不计时）、时长（25/35/自定义）、归属待办集、**展开更多高级设置** |
| 点「展开更多高级设置」 | 「高级设置」对话框（叠在上一层） | 完成后第二天不再显示、任务备注、**单次预期循环次数 K**、**自定义休息时间 M**，并实时回显「专注 N + 休息 M，共 K 轮」 |
| **长按**任务卡 | 详情卡（296 dp 半透明浮层） | 编辑 / 排序·移动 / 删除 / 专注历史记录 / 数据统计 / 定时功能 / 更换背景 / 独立白名单；含周热力点与「累计专注」大数字 |

- **排序·移动**：上移 / 下移 / 置顶 / 置底 / 移动到其他待办集（越界按钮自动置灰，当前所属集合打勾置灰）。
- **删除**：二次确认（提示专注记录仍保留在统计中）。
- 弹层全部由 `TodoDialogHost` 一个 `sealed interface TodoDialog` 托管，避免一堆 boolean。

---

## 六、统计页图表（全部真实数据）

| 卡片 | 图表 | 数据源 |
|---|---|---|
| 累计专注 | 三列大数字 | `SUM(focusSec)` / `COUNT(*)` / 日均 |
| 当日专注 | 两列大数字 + 周热力点 | 今日区间 |
| 专注时长分布 | **饼图 + 图例** | 区间内按任务 `GROUP BY` |
| 每日专注 | **圆角柱状图** | 区间内按天 `GROUP BY` |
| 本周打卡 | **7 圆点热力行** | 本周一~周日 |
| 本月专注时段分布 | **24 小时柱状图** | 本月按小时 `GROUP BY` |
| 本月专注日历 | **月历热力图**（每天一个色块，深浅按时长） | 本月按天 `GROUP BY` |

顶部分段器 **日 / 周 / 月 / 自定义** 会真实切换查询区间（日=今天；周=本周一 0 点起 7 天；
月=本月 1 号到下月 1 号；自定义=近 30 天），饼图 / 柱状图 / 日均同步刷新。
所有图表都是 **Compose Canvas 手绘**，不引任何图表库。

---

## 七、UI 规格来源（重要）

所有尺寸**不是目测**，是从真机截图（1080×2400 px，DPR=3 ⇒ 逻辑画布 **360×800 dp**）
按 `像素 ÷ 3` 量出来的，写在 `text/ui-spec/spec.md`：

| 项 | 值 |
|---|---|
| 逻辑画布 | 360 × 800 dp |
| App 头部 | 真实 `WindowInsets.statusBars` + 内容区 57 dp |
| 底部导航 | 52 dp + 真实 `navigationBars` 内边距 |
| 待办卡片 | 340 × 68 dp，圆角 7 dp，间距 6 dp |
| 待办集卡片 | 352 × 53 dp，圆角 6 dp，间距 5 dp |
| 统计卡 | 342 dp 宽，圆角 12 dp，间距 14 dp |
| 饼图 | 半径 71 dp，起点 −90° |
| 添加待办对话框 | 304 dp 宽，圆角 16 dp |
| 长按详情卡 | 296 dp 宽，圆角 16 dp |

图标与插画**全部用 Compose Canvas 手绘**，不依赖 `material-icons-extended`。

---

## 八、已知取舍 / 下一步

1. **待办集默认折叠**：点头部左侧箭头展开（旋转 + 高度动画），当前不会记住展开状态。
2. **拖动排序**未做，这一版用弹窗式的上移/下移/置顶/置底。
3. **详情卡里的「专注历史记录 / 数据统计 / 更换背景 / 独立白名单」**目前是空实现（下一版做各自页面）。
4. **亮度按钮**只做应用层遮罩调暗，未申请 `WRITE_SETTINGS`。
5. **未上前台服务 / 通知 / 精确闹钟**：进程被杀后计时不会自动恢复（M3）。
6. **未声明任何权限**：见 `AndroidManifest.xml` 注释，按 M3/M4 分阶段开启。

下一步：番茄钟前台服务 + 常驻通知 + `USE_EXACT_ALARM`、单任务统计页、锁机（D 模块）。
