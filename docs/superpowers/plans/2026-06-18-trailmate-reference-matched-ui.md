# TrailMate Reference Matched UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the Android Compose prototype so Home, Route, and Gear match the three user-provided TrailMate visual references while keeping profile/history data as backend evidence instead of primary UI.

**Architecture:** Keep existing state, GPX import, assessment, hike plan, and gear matching logic. Replace the presentation layer in `HomeScreen.kt`, `RouteDetailScreen.kt`, `MyGearScreen.kt`, and shared design components with reference-matched Compose sections. Use Compose smoke tests to protect visible Chinese copy, navigation, route import, light navigation, and gear add flows.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android instrumented Compose tests, JUnit.

---

## Files

- Modify: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`
  - Update smoke tests to expect the new reference-matched page structure.
  - Add checks that Home no longer surfaces historical GPX and body/profile evidence on the default route-preparation surface.
- Modify: `android-app/src/main/java/com/trailmate/app/core/design/TrailMateTheme.kt`
  - Tune colors toward off-white background, deep TrailMate green, gray dividers, amber warning, and restrained typography.
- Modify: `android-app/src/main/java/com/trailmate/app/core/design/TrailMateComponents.kt`
  - Add compact cards, segmented controls, icon badges, status chips, route stat rows, and bottom-nav helpers that mimic the reference density.
- Modify: `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`
  - Rebuild Home as: brand/weather header, route import row, current route assessment card, quick-start cards, today overview, bottom nav.
  - Keep history import available from Profile/hidden evidence management, not the main Home surface.
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Rebuild route detail as: title/header, `评估 / 路线 / 计划 / 装备`, map-like route canvas, floating assessment/progress cards, checkpoint panel, action buttons.
- Modify: `android-app/src/main/java/com/trailmate/app/feature/gear/MyGearScreen.kt`
  - Rebuild gear as: title + route pill, route list tabs, AI recommendation panel, dense gear checklist rows, add-existing-gear panel styled as a bottom sheet.

## Task 1: UI Smoke Tests

- [ ] **Step 1: Add failing tests for the new Home surface**

Expected visible Home copy with an imported route:

```kotlin
compose.onNodeWithText("下午好，").assertExists()
compose.onNodeWithText("准备走哪条线？").assertExists()
compose.onNodeWithText("导入 GPX 文件").assertExists()
compose.onNodeWithText("当前路线评估").assertExists()
compose.onNodeWithText("龙井山脊").assertExists()
compose.onNodeWithText("谨慎尝试").assertExists()
compose.onNodeWithText("快速开始").assertExists()
compose.onNodeWithText("今日概览").assertExists()
compose.onAllNodesWithText("0/3 GPX").assertCountEquals(0)
compose.onAllNodesWithText("181cm / 76kg").assertCountEquals(0)
```

- [ ] **Step 2: Add failing tests for the new Route surface**

Expected visible Route copy:

```kotlin
compose.onNodeWithText("路线评估").assertExists()
compose.onNodeWithText("风险因素").assertExists()
compose.onNodeWithText("路线进度").assertExists()
compose.onNodeWithText("当前检查点").assertExists()
compose.onNodeWithText("仅提供轻导航，不替代路标与离线地图").assertExists()
compose.onNodeWithText("标记下一检查点").assertExists()
```

- [ ] **Step 3: Add failing tests for the new Gear surface**

Expected visible Gear copy:

```kotlin
compose.onNodeWithText("AI 装备建议").assertExists()
compose.onNodeWithText("必备装备").assertExists()
compose.onNodeWithText("查看匹配").assertExists()
compose.onNodeWithText("添加已有装备").assertExists()
compose.onNodeWithText("保存到我的装备").assertExists()
```

- [ ] **Step 4: Run tests and verify red**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest --no-daemon --tests "*TrailMateAppSmokeTest"
```

Expected: FAIL on the new reference-matched copy because production UI still uses the old panel style.

## Task 2: Reference-Matched Compose UI

- [ ] **Step 1: Update the theme and shared components**
  - Use off-white `background`, white `surface`, deep green `primary`, amber `secondary`, gray dividers.
  - Add reusable compact card/status components instead of large generic panels wherever useful.

- [ ] **Step 2: Rebuild Home**
  - Keep route import callbacks and sample GPX test tag.
  - Remove Home default display of body metrics, historical GPX progress, and capability evidence.
  - Show the reference-matched current-route card and quick-start shortcuts.

- [ ] **Step 3: Rebuild Route Detail**
  - Keep all four tabs and active hike state behavior.
  - Replace the old route sketch panel with map-like route canvas and floating result/progress cards.
  - Rename active-hike advance action to `标记下一检查点`.

- [ ] **Step 4: Rebuild Gear**
  - Keep add-brand-gear callbacks and matching logic.
  - Present recommendations as dense checklist rows with status dots and route match actions.
  - Render the add form as a bottom-sheet-like panel.

## Task 3: Verification

- [ ] **Step 1: Run full unit tests**

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --no-daemon
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run full connected UI tests**

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest --no-daemon
```

Expected: 21+ tests, 0 failures.

- [ ] **Step 3: Install and capture screenshots**

```powershell
.\gradlew.bat :android-app:installDebug --no-daemon
adb -s emulator-5554 shell am start -n com.trailmate.app/.MainActivity
```

Capture Home, Route, and Gear screenshots under `%TEMP%`.

## Self-Review

- Spec coverage: covers Home, Route, Gear, evidence hiding, light navigation, and gear add flow.
- Placeholder scan: no TBD/TODO placeholders.
- Type consistency: keeps existing `HomeScreen`, `RouteDetailScreen`, and `MyGearScreen` public APIs so the app shell and persistence logic remain stable.
