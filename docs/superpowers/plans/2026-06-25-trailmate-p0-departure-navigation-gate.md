# TrailMate P0 Departure Navigation Gate Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent TrailMate from showing field-start actions until the route, offline route pack, required offline base map, GPS, and critical gear checks are ready.

**Architecture:** Keep `DepartureReadinessEngine` as the safety checklist source of truth, then make `RouteCockpitPresentationEngine` consume every non-start departure repair action before falling through to `START_HIKE`. Keep UI changes narrow by changing route workspace copy from vague preparation language to route preview plus departure check language.

**Tech Stack:** Kotlin model logic, Android Compose UI, JUnit unit tests, OpenSpec change validation.

---

## File Structure

- Modify `android-app/src/test/java/com/trailmate/app/core/model/RouteCockpitPresentationEngineTest.kt`
  - Replace unsafe offline-base-map expectations with P0 blocking behavior.
- Modify `android-app/src/main/java/com/trailmate/app/core/model/RouteCockpitPresentationEngine.kt`
  - Remove the offline-base-map exception from pre-start repair-action gating.
- Create `android-app/src/test/java/com/trailmate/app/feature/route/RouteWorkspacePrimaryActionCopyTest.kt`
  - Pure test for imported-route route-card copy.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteWorkspaceScreen.kt`
  - Extract route-card primary action copy and use `查看路线与出发检查`.
- Modify `openspec/changes/trailmate-p0-departure-navigation-gate/tasks.md`
  - Check off completed items only after tests pass.

---

## Task 1: Cockpit Blocks Required Offline Base Map Repair

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteCockpitPresentationEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteCockpitPresentationEngine.kt`

- [x] **Step 1: Write failing tests**

In `RouteCockpitPresentationEngineTest.kt`, change these three tests:

```kotlin
@Test
fun primaryActionBlocksStartWhenRequiredOfflineBaseMapIsMissing() {
    val presentation = RouteCockpitPresentationEngine.build(
        route = sampleRoute,
        plan = samplePlan,
        session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
        liveGuidance = sampleGuidance,
        mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
        departureReadiness = departureReadiness(
            gpsEnabled = true,
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 0
        ),
        locationSnapshot = locatedSnapshot,
        locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
        trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
        wasRecentlyOffRoute = false,
        nowEpochMillis = NOW_EPOCH_MILLIS
    )

    assertEquals("导入离线地图包", presentation.primaryAction.label)
    assertEquals(RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP, presentation.primaryAction.kind)
    val baseMapItem = presentation.readinessItems.first { it.label == "离线地图包" }
    assertEquals(RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP, baseMapItem.actionKind)
}

@Test
fun primaryActionBlocksStartWhenTargetOfflineBaseMapRegionIsMissing() {
    val presentation = RouteCockpitPresentationEngine.build(
        route = sampleRoute,
        plan = samplePlan,
        session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
        liveGuidance = sampleGuidance,
        mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
        departureReadiness = departureReadiness(
            gpsEnabled = true,
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 0,
            targetOfflineBaseMapRegionLabel = "杭州市"
        ),
        locationSnapshot = locatedSnapshot,
        locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
        trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
        wasRecentlyOffRoute = false,
        nowEpochMillis = NOW_EPOCH_MILLIS
    )

    assertEquals("导入离线地图包", presentation.primaryAction.label)
    assertEquals(RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP, presentation.primaryAction.kind)
    assertEquals("杭州市未下载", presentation.readinessItems.first { it.label == "离线地图包" }.value)
}

@Test
fun primaryActionBlocksStartUntilOfflineBaseMapTilesAreVerified() {
    val presentation = RouteCockpitPresentationEngine.build(
        route = sampleRoute,
        plan = samplePlan,
        session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
        liveGuidance = sampleGuidance,
        mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
        departureReadiness = departureReadiness(
            gpsEnabled = true,
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 1,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = false
        ),
        locationSnapshot = locatedSnapshot,
        locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
        trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
        wasRecentlyOffRoute = false,
        nowEpochMillis = NOW_EPOCH_MILLIS
    )

    assertEquals("飞行模式验证底图", presentation.primaryAction.label)
    assertEquals(RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP, presentation.primaryAction.kind)
    val baseMapItem = presentation.readinessItems.first { it.label == "离线地图包" }
    assertEquals(RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP, baseMapItem.actionKind)
}
```

- [x] **Step 2: Run tests to verify red**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteCockpitPresentationEngineTest" --no-daemon
```

Expected: the three updated tests fail because the current primary action is `开始徒步并记录轨迹`.

- [x] **Step 3: Implement minimal gate fix**

In `RouteCockpitPresentationEngine.kt`, replace:

```kotlin
if (
    session.status == HikeSessionStatus.READY &&
    !departureReadiness.primaryActionLabel.isStartHikeAction() &&
    !departureReadiness.primaryActionLabel.isOfflineBaseMapRepairAction()
) {
    return departureReadiness.primaryRepairAction()
}
```

with:

```kotlin
if (
    session.status == HikeSessionStatus.READY &&
    !departureReadiness.primaryActionLabel.isStartHikeAction()
) {
    return departureReadiness.primaryRepairAction()
}
```

Do not remove `isOfflineBaseMapRepairAction()` because it is still used by `primaryRepairAction()`.

- [x] **Step 4: Run tests to verify green**

Run the same focused test command.

Expected: `BUILD SUCCESSFUL`.

---

## Task 2: Route Workspace Copy Distinguishes Preview From Start

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/feature/route/RouteWorkspacePrimaryActionCopyTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteWorkspaceScreen.kt`

- [x] **Step 1: Write failing copy test**

Create `RouteWorkspacePrimaryActionCopyTest.kt`:

```kotlin
package com.trailmate.app.feature.route

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteWorkspacePrimaryActionCopyTest {
    @Test
    fun importedRoutePrimaryActionOpensRouteAndDepartureCheck() {
        assertEquals("查看路线与出发检查", routeWorkspacePrimaryActionLabel(hasRoute = true))
    }

    @Test
    fun emptyRoutePrimaryActionImportsGpx() {
        assertEquals("导入 GPX 文件", routeWorkspacePrimaryActionLabel(hasRoute = false))
    }
}
```

- [x] **Step 2: Run test to verify red**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.feature.route.RouteWorkspacePrimaryActionCopyTest" --no-daemon
```

Expected: compilation fails because `routeWorkspacePrimaryActionLabel` does not exist.

- [x] **Step 3: Implement copy helper and UI usage**

In `RouteWorkspaceScreen.kt`, replace:

```kotlin
Text(if (importedRoute == null) "导入 GPX 文件" else "继续准备")
```

with:

```kotlin
Text(routeWorkspacePrimaryActionLabel(hasRoute = importedRoute != null))
```

Add this function near the bottom of the file:

```kotlin
internal fun routeWorkspacePrimaryActionLabel(hasRoute: Boolean): String =
    if (hasRoute) "查看路线与出发检查" else "导入 GPX 文件"
```

- [x] **Step 4: Run copy test to verify green**

Run the same focused test command.

Expected: `BUILD SUCCESSFUL`.

---

## Task 3: OpenSpec And Focused Verification

**Files:**
- Modify: `openspec/changes/trailmate-p0-departure-navigation-gate/tasks.md`

- [x] **Step 1: Mark completed OpenSpec tasks**

After Tasks 1 and 2 are green, mark these items complete in `openspec/changes/trailmate-p0-departure-navigation-gate/tasks.md`:

```markdown
- [x] Add OpenSpec delta for P0 departure navigation gating.
- [x] Add failing model tests for required offline base-map blockers.
- [x] Make route cockpit primary action follow the departure repair action before start-hike.
- [x] Add route workspace copy test for imported route entry.
- [x] Update route workspace copy to `查看路线与出发检查`.
```

- [x] **Step 2: Run focused verification**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteCockpitPresentationEngineTest" --tests "com.trailmate.app.feature.route.RouteWorkspacePrimaryActionCopyTest" --no-daemon
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 3: Run full verification**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon
openspec validate --all --strict
```

Expected: Gradle `BUILD SUCCESSFUL` and OpenSpec totals include all changes passing.

---

## Self-Review

- Spec coverage: `Departure Gate Blocks Field Start Until Ready` is covered by Task 1 tests; route workspace copy is covered by Task 2.
- Placeholder scan: no placeholders or vague "add tests" steps remain.
- Type consistency: tests use existing `RouteCockpitPrimaryActionKind`, `RouteCockpitReadinessActionKind`, and existing route sample helpers.
