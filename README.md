# 番茄合体（Tomato）

Android **原生**（Kotlin + Jetpack Compose）实现，目标是对齐「番茄ToDo」的核心体验，并逐步合入「不做手机控」的能力。

当前阶段：**M0 · UI 复刻 Demo** —— 只还原界面，不接业务与数据层。

> ⚠️ 本轮**不做**「不做手机控」（D 模块）与 memos（C 模块），只复刻番茄TODO 部分：
> A. 番茄钟 + B. 任务清单。锁机 / 我的 两个 tab 仅占位。

---

## 一、快速开始

```bash
# 需要 JDK 17 + Android SDK（compileSdk 35）
./gradlew :app:assembleDebug
# 或直接用 Android Studio 打开本目录（Gradle Sync 后 Run 'app'）
```

最低支持 **Android 8.0（API 26）**，目标 **API 35**，主要面向 Redmi / HyperOS。

CI：`.github/workflows/android.yml`，push 到 `main` 自动执行
`compileDebugKotlin` → `assembleDebug` → 上传 debug APK 产物。

---

## 二、目录结构

```
.
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                      # 图标 / 主题 / strings
│       └── java/com/tomato/app/
│           ├── MainActivity.kt       # edge-to-edge 入口
│           └── ui/
│               ├── TomatoApp.kt      # 内容区 + 底部导航
│               ├── nav/Tab.kt        # 5 个 tab 枚举
│               ├── theme/            # Color / Type / Theme
│               ├── components/       # AppHeader / Icons / Decorations / TaskCard / StatCard / BottomNavBar
│               ├── data/DemoData.kt  # demo 假数据（M1 后由 Room 替换）
│               └── screen/           # Todo / Collections / Stats / Placeholder
├── reference/
│   └── tomato-ui-reference.html      # 网页高保真复刻（可直接在浏览器打开对比）
├── text/
│   ├── ui-spec/spec.md               # ★ 尺寸规格：所有 dp/sp/色值来源
│   │   └── screenshots/              # 原始截图 1080×2400 @DPR3
│   └── plans/plan.md                 # 总体开发计划（M0~M5）
└── .github/workflows/android.yml
```

---

## 三、UI 规格来源（重要）

所有尺寸**不是目测**，是从真机截图（1080×2400 px，DPR=3 ⇒ 逻辑画布 **360×800 dp**）
按 `像素 ÷ 3` 量出来的，写在 `text/ui-spec/spec.md`：

| 项 | 值 |
|---|---|
| 逻辑画布 | 360 × 800 dp |
| 状态栏 | 33 dp |
| App 头部（含状态栏） | 90 dp（内容区 57 dp） |
| 底部导航 | 52 dp（图标 20 dp / 文字 10 sp） |
| 待办卡片 | 340 × 68 dp，圆角 7 dp，间距 6 dp |
| 待办集卡片 | 352 × 53 dp，圆角 6 dp，间距 5 dp |
| 统计卡 | 342 dp 宽，圆角 12 dp，间距 14 dp |
| 饼图 | 半径 71 dp，起点 −90° |

图标与插画**全部用 Compose Canvas 手绘**（`ui/components/Icons.kt`、`Decorations.kt`），
因此不依赖 `material-icons-extended`，形状也和番茄ToDo 自带图标一致。

---

## 四、已知的取舍

1. **状态栏/导航栏用固定 dp**：头部预留 33 dp 状态栏、底部导航固定 52 dp，
   与截图 1:1 对齐；M1 再改成真实 `WindowInsets`（不同机型状态栏高度不一致）。
2. **卡片点击无行为**：`TaskCard` 的 `onClick` 已留好，M1 接「开始番茄」。
3. **未声明任何权限**：demo 不申请权限（见 `AndroidManifest.xml` 注释，按 M1/M2/M4 分阶段开启）。
4. **无数据层**：`ui/data/DemoData.kt` 是硬编码假数据，Room 在 M0 后半段接入。

---

## 五、下一步（M0 收尾 → M1）

- [ ] Room 数据层：`Task` / `TaskList` / `PomodoroSession` 实体 + DAO
- [ ] 番茄钟前台服务 + 通知 + 精确闹钟（`USE_EXACT_ALARM`）
- [ ] 用真实 `WindowInsets` 替换硬编码的 33dp / 52dp
- [ ] 待办卡片「开始」→ 启动计时，头部 `+` → 新建任务
