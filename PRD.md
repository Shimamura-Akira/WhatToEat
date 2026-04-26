# 项目需求文档 (PRD): “今天吃什么” (Random Choice Maker)

## 1. 项目概述
这是一个为“选择困难症”设计的安卓应用。用户可以自定义候选菜单列表（如：火锅、烧烤、拉面），点击按钮后，应用会在现有的菜单中随机抽取一项作为最终决定，并带有简单的抽奖动画效果。

## 2. 技术栈要求
*   **开发语言**: Java 
*   **UI 布局**: XML 布局体系
*   **顶部导航**: **Toolbar (Material Components)**，摒弃传统的 ActionBar。
*   **核心组件**: Activity, RecyclerView, ViewBinding
*   **数据存储**: SharedPreferences (配合 Gson 将 List 转换为 JSON 字符串存储)
*   **异步机制**: Kotlin Coroutines (协程) 或 Handler (用于实现文本滚动动画)

## 3. 核心功能需求

### 3.1 菜单列表管理 (增删查)
*   **展示**: 在页面下方使用 `RecyclerView` 展示所有保存的候选菜单。
*   **添加**: 页面提供一个悬浮按钮 (FloatingActionButton)，点击弹出一个输入框 Dialog。用户输入菜单名点击“确定”后，添加到列表中并保存。
    *   *边界条件*: 如果输入为空，或者与列表中已有项重复，需弹出 Toast 提示并阻止添加。
*   **删除**: 列表中的每一个 Item 右侧带有一个删除图标，点击可删除该菜单项并更新存储。

### 3.2 随机抽签功能
*   **触发**: 页面上方有一个醒目的“抽签”按钮 (Button)。
*   **动画效果 (文字滚动)**: 点击“抽签”后，屏幕中间的巨大 `TextView` 开始快速随机显示列表中的菜单名（模拟老虎机或转盘效果），持续约 2 秒钟。
*   **结果展示**: 2秒后动画停止，定格在最终抽中的菜单上，并可以弹出一张图片或简单的 `AlertDialog` 恭喜用户。
    *   *边界条件*: 如果列表为空，点击按钮时需 Toast 提示“请先添加候选菜单”。如果列表只有 1 个选项，直接显示结果，跳过动画。

### 3.3 数据持久化
*   应用每次启动时，从 `SharedPreferences` 中读取 JSON 并解析为 List。
*   每次列表发生 **添加** 或 **删除** 操作时，同步将最新的 List 转换为 JSON 保存。

## 4. 界面布局规划 (UI Layout)
整个应用使用 ConstraintLayout 作为根布局，自上而下分为以下区域：

1.  **顶部导航栏 (Toolbar Layout)**
    *   使用 `<androidx.appcompat.widget.Toolbar>` 放置在屏幕最顶部，设置标题为“今天吃什么”。
2.  **顶部展示区 (Header & Result)**
    *   位于 Toolbar 下方。一个大字号居中的 `TextView`，在抽签时用于展示快速滚动的文字和最终结果。
3.  **操作区 (Action)**
    *   一个显眼的“开始决定!” `Button`，居中放置在展示区下方。
4.  **列表区 (Data List)**
    *   一个 `RecyclerView` 占据下方的剩余屏幕空间，展示菜单列表。单行 Item 包含：文字(居左) + 删除按钮(居右)。
5.  **悬浮操作 (FAB)**
    *   右下角放置一个 `FloatingActionButton`，图标为 `+`。

## 5. 给 AI Agent 的开发指令
请遵循以下现代 Android 开发规范：
1.  **主题与 Toolbar**: 
    *   请在 `themes.xml` 中将基础主题修改为 `.NoActionBar`（如 `Theme.MaterialComponents.DayNight.NoActionBar`）。
    *   在 `MainActivity` 中通过 `setSupportActionBar(binding.toolbar)` 将 Toolbar 设置为活动的 Action Bar。
2.  **UI 绑定**: 使用 **ViewBinding** 替代 `findViewById`。
3.  **数据模型**: 定义 Data Class，例如 `data class FoodItem(val id: String, val name: String)` (推荐用 UUID 生成独立 id 防止删除错乱)。
4.  **适配器**: 使用 `ListAdapter` 和 `DiffUtil` 来替换普通的 `RecyclerView.Adapter`，以获得更好的列表刷新性能和动画。
5.  **代码分步生成建议**:
    *   第一步：修改 `themes.xml`，生成 `activity_main.xml` (包含 Toolbar) 与基础 `MainActivity`。
    *   第二步：完成 `item_food.xml`，RecyclerView 适配器 (`ListAdapter`) 与数据存储逻辑 (`Gson` + `SharedPreferences`)。
    *   第三步：实现 FAB 弹窗添加逻辑、列表 Item 删除逻辑。
    *   第四步：实现 2秒文字随机高速滚动的动画效果及抽取结果展示。