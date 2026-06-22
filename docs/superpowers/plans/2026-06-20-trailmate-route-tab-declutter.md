# Route Tab Declutter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the `路线` tab so the default page separates map preview from hiking actions and removes the visible `地图状态与轻导航` diagnostics entry.

**Architecture:** Keep the existing `RouteDetailScreen` and route cockpit state engines. Change only the route tab presentation layer: the default cockpit becomes a map preview plus a separate action panel, while diagnostics remain available only as internal/secondary content when explicitly needed by tests or future settings flows.

**Tech Stack:** Kotlin, Jetpack Compose, Android instrumentation smoke tests, OpenSpec.

---

### Task 1: Lock The Default Route Tab Contract

**Files:**
- Modify: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`
- Verify: `./gradlew.bat :android-app:compileDebugAndroidTestKotlin`

- [x] **Step 1: Add/update failing UI assertions**

Update `routeTabShowsGpsAndTrackRecordingControls` so the default `路线` tab asserts:

```kotlin
compose.onNodeWithTag("route-cockpit").assertExists()
compose.onNodeWithText("全屏导航").assertExists()
compose.onNodeWithText("安全分享").assertExists()
compose.onAllNodesWithText("地图状态与轻导航").assertCountEquals(0)
compose.onAllNodesWithText("地图状态").assertCountEquals(0)
compose.onAllNodesWithText("本地路线、定位、离线包和图层说明").assertCountEquals(0)
compose.onNodeWithText("检查点与补给").assertExists()
compose.onNodeWithText("5 个检查点 · 补给/休息/风险").assertExists()
```

Updated `routeTabShowsGpsAndTrackRecordingControls` to lock the default route tab contract: no `地图状态与轻导航`, no default map diagnostics copy, visible `检查点与补给`, and separate navigation/share controls.

- [x] **Step 2: Verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :android-app:compileDebugAndroidTestKotlin --no-daemon
```

Observed before implementation: the connected instrumentation test failed because the old default UI still exposed `地图状态`.

### Task 2: Separate Map Preview From Action Panel

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Verify: `./gradlew.bat :android-app:testDebugUnitTest :android-app:compileDebugAndroidTestKotlin --no-daemon`

- [x] **Step 1: Remove default map status overlay**

In `RouteCockpitSection`, stop rendering `RouteMapStatusOverlay` inside the map preview. Leave the map with only locate and optional offline-map controls.

- [x] **Step 2: Move the action drawer below the map**

Keep `RouteCockpitActionDrawer`, but render it as a normal surface below the map preview instead of `Box.align(Alignment.BottomCenter)`.

- [x] **Step 3: Replace diagnostics toggle with route detail row**

Renamed the visible disclosure copy from `地图状态与轻导航` to `检查点与补给`, and use the subtitle `5 个检查点 · 补给/休息/风险` for the sample route. Diagnostics internals can remain behind the disclosure for now, but the default surface must not look like a map authorization/debug control.

- [x] **Step 4: Verify GREEN**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest :android-app:compileDebugAndroidTestKotlin --no-daemon
```

Observed: targeted connected instrumentation, unit tests, and Android test Kotlin compilation all passed.

### Task 3: Sync OpenSpec And Visual Evidence

**Files:**
- Modify: `openspec/changes/trailmate-route-fullscreen-navigation-focus/design.md`
- Modify: `openspec/changes/trailmate-route-fullscreen-navigation-focus/specs/mobile-product-ux/spec.md`
- Verify: `openspec validate trailmate-route-fullscreen-navigation-focus --strict`

- [x] **Step 1: Update design wording**

Document that the default route page uses a standalone map preview plus a separate action panel and does not show `地图状态与轻导航`.

- [x] **Step 2: Capture emulator evidence**

Install and launch the debug app, navigate to the route tab, and capture screenshots/XML under:

```text
outputs/qa/route-tab-declutter/
```

- [x] **Step 3: Final validation**

Run:

```powershell
openspec validate trailmate-route-fullscreen-navigation-focus --strict
```

Observed: change is valid.
