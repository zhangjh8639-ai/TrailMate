# TrailMate Production Mobile UX Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the current TrailMate Android Compose prototype into a production-grade Chinese mobile experience with clean page responsibilities, polished outdoor visual hierarchy, and preserved real GPS, GPX import, track recording, route assessment, and gear matching behavior.

**Architecture:** Keep existing domain engines and persistence contracts intact. Use `HomeScreen.kt` as the bottom-tab shell during the first pass, then extract focused page composables so each bottom tab owns one product job. Route detail remains inside the route flow, but its four tabs are split into smaller files and its diagnostics move behind secondary disclosure.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Compose UI tests, existing `TrailMateTheme`, existing GPX/location/map/model engines, OpenSpec `trailmate-production-mobile-ux-system`.

---

## Scope Check

This plan covers the first production UX refactor slice: information architecture, page extraction, first-screen hierarchy, and key visual components. It does not add a backend, account server, community route library, ecommerce equipment recommendations, rescue guarantees, or full turn-by-turn navigation.

The design source is the user-provided three-screen reference direction: warm ivory background, moss green actions, large route/map visuals, thin dividers, iOS-like sheets, restrained status cards, and Chinese-first copy.

## File Structure

Create:

- `android-app/src/main/java/com/trailmate/app/feature/home/HomeDashboardScreen.kt` - Home tab only: greeting, weather/location, GPX import, current route card, three quick starts, recent track entry when available.
- `android-app/src/main/java/com/trailmate/app/feature/route/RouteWorkspaceScreen.kt` - Route tab only: route import, current route preparation state, route list, import queue.
- `android-app/src/main/java/com/trailmate/app/feature/data/DataScreen.kt` - Data tab only: recent track review, plan-vs-actual summary, historical activities, capability trend entry.
- `android-app/src/main/java/com/trailmate/app/feature/profile/ProfileSettingsScreen.kt` - Me tab only: profile summary, permissions, map authorization, privacy/data controls.
- `android-app/src/main/java/com/trailmate/app/feature/route/detail/RouteAssessmentTab.kt` - Route detail assessment content.
- `android-app/src/main/java/com/trailmate/app/feature/route/detail/RouteCockpitTab.kt` - Route detail map cockpit content.
- `android-app/src/main/java/com/trailmate/app/feature/route/detail/RoutePlanTab.kt` - Route detail plan content.
- `android-app/src/main/java/com/trailmate/app/feature/route/detail/RouteGearTab.kt` - Route detail route-specific gear checklist.
- `android-app/src/main/java/com/trailmate/app/core/design/TrailMateScaffold.kt` - Shared page scaffold, empty state, status strip, and action sheet helpers.

Modify:

- `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt` - Keep state ownership and bottom navigation; delegate tab bodies to extracted screens.
- `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt` - Keep route-detail shell and orchestration; move tab bodies into `feature/route/detail`.
- `android-app/src/main/java/com/trailmate/app/feature/gear/MyGearScreen.kt` - Align route checklist, inventory, detail, and bottom sheet behavior with the production design contract.
- `android-app/src/main/java/com/trailmate/app/core/design/TrailMateComponents.kt` - Keep existing components; move only broadly reused new primitives into `TrailMateScaffold.kt`.
- `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt` - Update and add Compose smoke tests for page responsibilities.
- `openspec/changes/trailmate-production-mobile-ux-system/tasks.md` - Mark this plan complete after it is saved and validated.

Do not modify:

- GPX parsers/importers, track recording engines, map diagnostics engines, route assessment engines, gear matching engines, persistence codecs, or foreground service code unless a test failure proves the UI refactor exposed a real contract issue.

## Task 1: Add Page Responsibility Smoke Tests

**Files:**

- Modify: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Replace the current broad bottom-tab smoke expectations with page-contract assertions**

Add these tests near the existing home and tab tests:

```kotlin
@Test
fun homeDashboardFocusesOnTodayRoutePreparation() {
    compose.setContent {
        TrailMateTheme {
            HomeScreen(
                profile = savedProfile(),
                initialImportedRoute = TrailMateSampleData.importedTargetRoute,
                initialHistoricalActivities = TrailMateSampleData.historicalActivities
            )
        }
    }

    compose.onNodeWithText("准备走哪条线？").assertExists()
    compose.onNodeWithText("导入 GPX 文件").assertExists()
    compose.onNodeWithText("当前路线评估").assertExists()
    compose.onNodeWithText("快速开始").performScrollTo().assertExists()
    compose.onAllNodesWithText("今日概览").assertCountEquals(0)
    compose.onAllNodesWithText("历史 GPX 能力证据").assertCountEquals(0)
    compose.onAllNodesWithText("181cm / 76kg").assertCountEquals(0)
    compose.onAllNodesWithText("本地数据").assertCountEquals(0)
}

@Test
fun routeWorkspaceOwnsRouteImportAndCurrentRouteEntry() {
    compose.setContent {
        TrailMateTheme {
            HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
        }
    }

    compose.onNodeWithTag("home-tab-路线").performClick()
    compose.onNodeWithText("导入 GPX 文件").assertExists()
    compose.onNodeWithText("继续准备").assertExists()
    compose.onAllNodesWithText("风险因素").assertCountEquals(0)
    compose.onAllNodesWithText("现场状态").assertCountEquals(0)
}

@Test
fun dataAndProfileTabsOwnEvidenceAndPrivacyControls() {
    compose.setContent {
        TrailMateTheme {
            HomeScreen(
                initialImportedRoute = TrailMateSampleData.importedTargetRoute,
                initialHistoricalActivities = TrailMateSampleData.historicalActivities,
                initialTrackRecording = recordedTrack()
            )
        }
    }

    compose.onNodeWithTag("home-tab-数据").performClick()
    compose.onNodeWithText("轨迹复盘").assertExists()
    compose.onNodeWithText("历史活动").performScrollTo().assertExists()
    compose.onAllNodesWithText("清除本地数据").assertCountEquals(0)

    compose.onNodeWithTag("home-tab-我的").performClick()
    compose.onNodeWithText("基础档案").assertExists()
    compose.onNodeWithText("数据与隐私").performScrollTo().assertExists()
    compose.onNodeWithText("清除本地数据").performScrollTo().assertExists()
    compose.onAllNodesWithText("轨迹复盘").assertCountEquals(0)
    compose.onAllNodesWithText("历史活动").assertCountEquals(0)
}
```

- [x] **Step 2: Run the focused connected test class and confirm the new assertions fail before implementation**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest
```

Expected: the new tests fail because the current Home tab still shows `今日概览`, the Route tab still exposes full route-detail content, and privacy controls still live in the Data tab.

## Task 2: Add Shared Production Mobile UI Primitives

**Files:**

- Create: `android-app/src/main/java/com/trailmate/app/core/design/TrailMateScaffold.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Create the shared scaffold file**

Create `TrailMateScaffold.kt` with these composables:

```kotlin
package com.trailmate.app.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TrailMatePageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (caption != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                trailing()
            }
        }
        content()
    }
}

@Composable
fun TrailMateActionRow(
    label: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (caption != null) {
                    Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun TrailMateEmptyState(
    title: String,
    actionLabel: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    onAction: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (caption != null) {
            Text(caption, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(actionLabel)
        }
    }
}

@Composable
fun TrailMateStatusStrip(
    items: List<TrailMateStatusStripItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = item.color.copy(alpha = 0.12f),
                onClick = item.onClick
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.label, style = MaterialTheme.typography.labelMedium, color = item.color)
                    Text(item.value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

data class TrailMateStatusStripItem(
    val label: String,
    val value: String,
    val color: Color,
    val onClick: () -> Unit
)
```

- [x] **Step 2: Compile to catch missing imports**

Run:

```powershell
.\gradlew.bat :android-app:compileDebugKotlin
```

Expected: the first run fails if `ColumnScope` import is missing. Add `import androidx.compose.foundation.layout.ColumnScope` if needed, then rerun until `BUILD SUCCESSFUL`.

## Task 3: Extract and Redesign the Home Dashboard

**Files:**

- Create: `android-app/src/main/java/com/trailmate/app/feature/home/HomeDashboardScreen.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Create the Home dashboard composable**

Create `HomeDashboardScreen.kt`:

```kotlin
package com.trailmate.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateActionRow
import com.trailmate.app.core.design.TrailMateEmptyState
import com.trailmate.app.core.design.TrailMatePageScaffold
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState

@Composable
fun HomeDashboardScreen(
    route: ImportedRoute?,
    latestTrackRecording: TrackRecordingState,
    onImportRoute: () -> Unit,
    onOpenRoute: () -> Unit,
    onOpenAssessment: () -> Unit,
    onOpenNavigation: () -> Unit,
    onOpenGear: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrailMatePageScaffold(
        title = "下午好，",
        caption = "准备走哪条线？",
        modifier = modifier
    ) {
        TrailMateActionRow(
            label = "导入 GPX 文件",
            caption = "选择目标路线后评估难度、装备和现场状态",
            onClick = onImportRoute
        )

        if (route == null) {
            TrailMateEmptyState(
                title = "还没有目标路线",
                caption = "导入 GPX 后，TrailMate 会给出路线评估、装备清单和轻导航入口。",
                actionLabel = "导入路线",
                onAction = onImportRoute
            )
        } else {
            HomePrimaryRouteCard(route = route, onOpenRoute = onOpenRoute)
        }

        Text(
            text = "快速开始",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeQuickStartButton("路线评估", Modifier.weight(1f), onOpenAssessment)
            HomeQuickStartButton("轻导航", Modifier.weight(1f), onOpenNavigation)
            HomeQuickStartButton("装备清单", Modifier.weight(1f), onOpenGear)
        }

        if (latestTrackRecording.pointCount > 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("最近记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${latestTrackRecording.routeName ?: "徒步记录"} / 已记录 ${latestTrackRecording.pointCount} 个点",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

The helper functions `HomePrimaryRouteCard` and `HomeQuickStartButton` can be moved from the current `HomeScreen.kt` if they already exist under different names. If their names differ, keep their existing implementation and expose wrappers with these exact names so the new file remains small.

- [x] **Step 2: Replace the old Home tab body in `HomeScreen.kt`**

In the selected tab branch for Home, call:

```kotlin
HomeDashboardScreen(
    route = importedRoute,
    latestTrackRecording = latestTrackRecording,
    onImportRoute = { selectedTab = HomeTab.ROUTE },
    onOpenRoute = { selectedTab = HomeTab.ROUTE },
    onOpenAssessment = { selectedTab = HomeTab.ROUTE },
    onOpenNavigation = { selectedTab = HomeTab.ROUTE },
    onOpenGear = { selectedTab = HomeTab.GEAR }
)
```

Remove the old `TodayOverviewCard` call from the Home tab. Keep any existing sample or import logic reachable from Route tab.

- [x] **Step 3: Run the focused Home smoke test**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#homeDashboardFocusesOnTodayRoutePreparation
```

Expected: PASS. The Home tab shows route preparation and no longer shows `今日概览`, body metrics, historical evidence, or local data controls.

## Task 4: Extract the Route Workspace From Route Detail

**Files:**

- Create: `android-app/src/main/java/com/trailmate/app/feature/route/RouteWorkspaceScreen.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Create a route workspace screen that does not render the full detail by default**

Create `RouteWorkspaceScreen.kt`:

```kotlin
package com.trailmate.app.feature.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateActionRow
import com.trailmate.app.core.design.TrailMateEmptyState
import com.trailmate.app.core.design.TrailMatePageScaffold
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.model.ImportedRoute

@Composable
fun RouteWorkspaceScreen(
    route: ImportedRoute?,
    importQueue: GpxImportQueue,
    onImportRoute: () -> Unit,
    onOpenRouteDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrailMatePageScaffold(
        title = "路线",
        caption = "管理目标路线和准备进度",
        modifier = modifier
    ) {
        TrailMateActionRow(
            label = "导入 GPX 文件",
            caption = "解析路线距离、爬升、检查点和风险输入",
            onClick = onImportRoute
        )
        if (route == null) {
            TrailMateEmptyState(
                title = "等待目标路线",
                caption = "导入 GPX 后再进入路线评估和轻导航。",
                actionLabel = "选择路线文件",
                onAction = onImportRoute
            )
        } else {
            TrailMateActionRow(
                label = route.routeName,
                caption = "${route.distanceKm} km / 累计爬升 ${route.ascentMeters} m",
                onClick = onOpenRouteDetail
            )
            Text(
                text = "继续准备",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("评估路线难度")
                Text("检查装备清单")
                Text("保存离线路线包")
            }
        }
        if (importQueue.jobs.isNotEmpty()) {
            Text(
                text = "导入队列",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

- [x] **Step 2: Add a route-detail open state to `HomeScreen.kt`**

Add state near the existing selected tab state:

```kotlin
var isRouteDetailOpen by rememberSaveable { mutableStateOf(false) }
```

In the Route tab branch:

```kotlin
if (isRouteDetailOpen && importedRoute != null) {
    RouteDetailScreen(
        route = importedRoute,
        inventory = inventory,
        aiGearAdvisorResponse = aiGearAdvisorResponse,
        initialTrackRecording = latestTrackRecording,
        initialOfflineRoutePackKeys = offlineRoutePackKeys,
        amapPrivacyConsent = amapPrivacyConsent,
        onInventoryChanged = onInventoryChanged,
        onTrackRecordingChanged = onTrackRecordingChanged,
        onOfflineRoutePackKeysChanged = onOfflineRoutePackKeysChanged,
        onAmapPrivacyConsentChanged = onAmapPrivacyConsentChanged
    )
} else {
    RouteWorkspaceScreen(
        route = importedRoute,
        importQueue = gpxImportQueue,
        onImportRoute = { startTargetRouteImport() },
        onOpenRouteDetail = { isRouteDetailOpen = true }
    )
}
```

Use the current parameter names from `HomeScreen.kt` when wiring `RouteDetailScreen`; do not change domain objects to satisfy this extraction.

- [x] **Step 3: Run the route workspace smoke test**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#routeWorkspaceOwnsRouteImportAndCurrentRouteEntry
```

Expected: PASS. The Route tab shows route management and does not show full assessment or live cockpit content until the current route is opened.

## Task 5: Move Data and Privacy Into Their Correct Tabs

**Files:**

- Create: `android-app/src/main/java/com/trailmate/app/feature/data/DataScreen.kt`
- Create: `android-app/src/main/java/com/trailmate/app/feature/profile/ProfileSettingsScreen.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Create `DataScreen.kt`**

```kotlin
package com.trailmate.app.feature.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateEmptyState
import com.trailmate.app.core.design.TrailMatePageScaffold
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.TrackRecordingState

@Composable
fun DataScreen(
    latestTrackRecording: TrackRecordingState,
    historicalActivities: List<HistoricalActivity>,
    onOpenRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrailMatePageScaffold(
        title = "数据",
        caption = "轨迹复盘和能力趋势",
        modifier = modifier
    ) {
        if (latestTrackRecording.pointCount == 0) {
            TrailMateEmptyState(
                title = "完成一次记录后会出现复盘",
                caption = "开始徒步并记录轨迹后，这里会展示距离、时长、爬升和计划偏差。",
                actionLabel = "去路线页",
                onAction = onOpenRoute
            )
        } else {
            Text("本次活动复盘")
            Text("${latestTrackRecording.routeName ?: "徒步记录"} / 已记录本次路线表现")
        }

        Text("历史活动")
        if (historicalActivities.isEmpty()) {
            Text("完成更多活动后，会在这里看到距离、爬升和时长变化。")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                historicalActivities.forEach { activity ->
                    Text("${activity.routeName} / ${activity.distanceKm} km / +${activity.ascentMeters} m")
                }
            }
        }
    }
}
```

- [x] **Step 2: Create `ProfileSettingsScreen.kt`**

```kotlin
package com.trailmate.app.feature.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.trailmate.app.core.design.TrailMateActionRow
import com.trailmate.app.core.design.TrailMatePageScaffold
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.HistoricalActivity

@Composable
fun ProfileSettingsScreen(
    profile: BaselineProfile,
    historicalActivities: List<HistoricalActivity>,
    historyImportUiState: HistoricalActivityImportUiState,
    onPickHistoryGpx: () -> Unit,
    onClearLocalData: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrailMatePageScaffold(
        title = "我的",
        caption = "档案、权限、隐私和本机数据管理。",
        modifier = modifier
    ) {
        Text("基础档案")
        Text("运动习惯、户外经验和身体信息仅用于路线评估。")
        TrailMateActionRow(
            label = "历史资料管理",
            caption = "历史 GPX 仅作为本机路线评估的背景资料。",
            onClick = onPickHistoryGpx
        )
        Text("权限状态")
        Text("定位 / 通知 / 地图授权")
        Text("数据与隐私")
        TrailMateActionRow(
            label = "清除本地数据",
            caption = "删除本机保存的路线、装备、历史活动和轨迹记录",
            onClick = onClearLocalData
        )
        Text("地图设置")
        Text("高德地图授权和离线地图入口")
    }
}
```

If `ExerciseFrequency.label` or `ExperienceLevel.label` is private in the current model, replace those two labels with existing display helpers from `HomeScreen.kt`; do not expose raw enum names to the UI.

- [x] **Step 3: Wire Data and Me tabs in `HomeScreen.kt`**

In the Data tab branch:

```kotlin
DataScreen(
    latestTrackRecording = latestTrackRecording,
    historicalActivities = historicalActivities,
    onOpenRoute = { selectedTab = HomeTab.ROUTE }
)
```

In the Me tab branch:

```kotlin
ProfileSettingsScreen(
    profile = profile,
    historicalActivities = historicalActivities,
    historyImportUiState = historyImportUiState,
    onPickHistoryGpx = { historyPicker.launch(GPX_MIME_TYPES) },
    onClearLocalData = { requestClearLocalData() }
)
```

Keep the existing clear-data confirmation dialog in `HomeScreen.kt` so destructive action behavior does not change.

- [x] **Step 4: Run the Data/Profile contract smoke test**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#dataAndProfileTabsOwnEvidenceAndPrivacyControls
```

Expected: PASS. Data owns track review and history. Me owns profile, permissions, and privacy controls.

## Task 6: Split Route Detail Tabs and Collapse Diagnostics

**Files:**

- Create: `android-app/src/main/java/com/trailmate/app/feature/route/detail/RouteAssessmentTab.kt`
- Create: `android-app/src/main/java/com/trailmate/app/feature/route/detail/RouteCockpitTab.kt`
- Create: `android-app/src/main/java/com/trailmate/app/feature/route/detail/RoutePlanTab.kt`
- Create: `android-app/src/main/java/com/trailmate/app/feature/route/detail/RouteGearTab.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Move the assessment tab entry into `RouteAssessmentTab.kt`**

Create the file as an internal wrapper around the current production assessment surface:

```kotlin
@Composable
internal fun RouteAssessmentTab(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    mapReadiness: TrailMapReadiness,
    trackRecording: TrackRecordingState,
    showUserLocationOnAmap: Boolean,
    locationSnapshot: TrailMateLocationSnapshot,
    checkpointDetailFor: (HikePlanCheckpoint) -> HikeCheckpointDetail,
    onLocateRequested: () -> Unit,
    onCheckpointFocused: (HikePlanCheckpoint) -> Unit
) {
    ReferenceRouteSurface(...)
}
```

The current production assessment surface is the reference route map, not the stale private `AssessmentTab`; keep that existing surface and expose it through a focused tab-owned wrapper.

- [x] **Step 2: Move the cockpit tab entry into `RouteCockpitTab.kt`**

Create the file with the current cockpit inputs and delegate to the existing implementation:

```kotlin
@Composable
internal fun RouteCockpitTab(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    liveGuidance: LiveCheckpointGuidance,
    mapReadiness: TrailMapReadiness,
    offlineRoutePackReady: Boolean,
    gearRecommendations: List<GearRecommendation>,
    /* existing callbacks and GPS/map inputs */
) {
    RouteCockpitTabContent(...)
}
```

Keep the existing long argument list for this pass and delegate to `RouteCockpitTabContent` in `RouteDetailScreen.kt`. Do not change the engines behind GPS, track recording, deviation recovery, offline readiness, or safety share.

- [x] **Step 3: Make diagnostics secondary**

In the cockpit tab, keep these labels out of the first visible block:

```kotlin
val secondaryDiagnostics = listOf(
    "现场详情",
    "地图图层",
    "高德上线检查",
    "位置可靠性"
)
```

Render them under a single collapsed row:

```kotlin
RouteCockpitDiagnosticsDisclosure(
    expanded = diagnosticsExpanded,
    onToggle = { diagnosticsExpanded = !diagnosticsExpanded }
)
```

When expanded, render the existing diagnostic panels in their current order so tests that verify high-value setup details can scroll to them.

- [x] **Step 4: Move plan and route gear tabs into focused files**

Create `RoutePlanTab.kt` with the existing plan rendering and keep checkpoint text advisory:

```kotlin
@Composable
internal fun RoutePlanTab(plan: HikePlanSummary) {
    plan.checkpoints.forEach { checkpoint -> ... }
}
```

Create `RouteGearTab.kt` with current route-only gear content:

```kotlin
@Composable
internal fun RouteGearTab(
    recommendations: List<GearRecommendation>,
    inventory: GearInventory,
    aiGearAdvisorPresentation: AiGearAdvisorPresentation,
    onAddGearRequested: (String) -> Unit
)
```

Move private helper functions with the tab they serve. Helpers shared by two or more tabs stay in `RouteDetailScreen.kt` until a second pass proves they deserve a shared file.

- [x] **Step 5: Run route detail smoke tests**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#routeDetailShowsAssessmentRoutePlanAndGearTabs
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#routeTabShowsGpsAndTrackRecordingControls
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#routeTabStartsAndAdvancesActiveHike
```

Expected: PASS. Route detail keeps the existing capabilities while the cockpit first screen focuses on map, status, current checkpoint, and primary action.

## Task 7: Polish Gear Around Route Checklist First

**Files:**

- Modify: `android-app/src/main/java/com/trailmate/app/feature/gear/MyGearScreen.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Keep route checklist as the default tab**

Confirm the internal default remains:

```kotlin
var selectedTab by rememberSaveable { mutableStateOf(GearTab.RouteList) }
```

The top structure should stay:

```kotlin
Text("装备")
RouteContextPill("龙井山脊 · 谨慎尝试")
TrailMateSegmentedControl(
    options = listOf("路线清单", "我的装备", "详情"),
    selectedOption = selectedTab.label,
    onOptionSelected = { label -> selectedTab = GearTab.fromLabel(label) }
)
```

- [x] **Step 2: Make adding owned gear feel like an iOS bottom sheet**

Use the existing add panel, but present it visually as a bottom action surface with:

```kotlin
Surface(
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    tonalElevation = 8.dp,
    shadowElevation = 10.dp,
    color = MaterialTheme.colorScheme.surface
) {
    AddBrandGearPanel(...)
}
```

Keep the same save callback:

```kotlin
onAddBrandGear(category, brand, model, available)
```

- [x] **Step 3: Run gear smoke tests**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#redesignedChinesePrototypeShowsGearCoachWorkspace
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#homeGearAddFlowPrefillsCategoryAndUpdatesRouteMatch
```

Expected: PASS. The Gear tab opens on route checklist, supports owned gear addition, and does not introduce purchase or ad language.

## Task 8: Full Verification and Visual Review

**Files:**

- Modify: `openspec/changes/trailmate-production-mobile-ux-system/tasks.md`

- [x] **Step 1: Run unit tests**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 2: Run connected smoke tests**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 3: Run debug build**

Run:

```powershell
.\gradlew.bat :android-app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 4: Validate OpenSpec**

Run:

```powershell
openspec validate trailmate-production-mobile-ux-system --strict
```

Expected: `Change 'trailmate-production-mobile-ux-system' is valid`.

- [x] **Step 5: Perform visual review against the three reference screens**

Capture these screens on an emulator:

- Home tab with imported route.
- Route tab before opening detail.
- Route detail cockpit.
- Gear tab route checklist with add-owned-gear sheet.
- Data tab with recorded track.
- Me tab with privacy controls.

Review each screenshot against this checklist:

- Ivory background and moss green hierarchy match the reference tone.
- One clear primary action per first screen.
- No exposed body metrics, history evidence package, AI input, or technical diagnostics in the main visual area.
- Bottom tabs do not duplicate the same product job.
- Route cockpit shows map/current checkpoint/status/action without requiring scroll.
- Gear page reads as preparation tooling, not a store.

- [x] **Step 6: Update OpenSpec task checkboxes**

After all verification passes, mark the completed items in:

```text
openspec/changes/trailmate-production-mobile-ux-system/tasks.md
```

Do not mark `Run unit tests, debug build, and connected smoke tests` complete until all three commands above have passed in the current worktree.

## Self-Review

Spec coverage:

- Home, Route, Gear, Data, Me, Onboarding, Route Assessment, Route Cockpit, Route Plan, and Route Gear are all covered by either direct implementation tasks or verification requirements.
- Evidence backgrounding is covered by Home, Assessment, Data, and Me tests.
- GPS and track recording preservation is covered by existing route cockpit and foreground recording smoke tests.
- Visual quality is covered by shared UI primitives and the screenshot review checklist.

Type consistency:

- New UI files take existing model types: `ImportedRoute`, `GpxImportQueue`, `TrackRecordingState`, `HistoricalActivity`, `BaselineProfile`.
- `HomeScreen.kt` remains the state owner during this phase, which avoids moving persistence callbacks during visual refactor.
- Route detail tab extraction groups existing arguments into UI-state/action bundles only inside the route detail layer.

Execution handoff:

- Use Subagent-Driven execution for Tasks 1-8.
- Dispatch one fresh subagent per task.
- Review tests and diff after each task before continuing.
