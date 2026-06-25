# TrailMate P0 Background Off-Route Alerts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the foreground track-recording service monitor the active imported route and deliver off-route alerts while the app is backgrounded or the screen is locked.

**Architecture:** Add a pure route-monitor engine that turns saved route geometry plus accepted recording points into `RouteDeviationAlertDecision`s. Wire the foreground service to evaluate that engine after accepted location appends, then use the existing Android delivery adapter; add a small delivery-ownership policy so the visible route screen does not duplicate notification/vibration during active recording.

**Tech Stack:** Kotlin, Android foreground service/location APIs, existing TrailMate route geometry and off-route policy engines, JUnit 4, OpenSpec.

---

## File Structure

- Create `android-app/src/test/java/com/trailmate/app/core/model/TrackRecordingRouteMonitorEngineTest.kt`: pure service-side route deviation tests.
- Create `android-app/src/main/java/com/trailmate/app/core/model/TrackRecordingRouteMonitorEngine.kt`: pure route monitor engine.
- Create `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryOwnerPolicyTest.kt`: duplicate-delivery ownership tests.
- Create `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryOwnerPolicy.kt`: route-screen vs service delivery owner policy.
- Modify `android-app/src/main/java/com/trailmate/app/core/location/TrackRecordingForegroundService.kt`: evaluate route monitor and deliver alerts after accepted recording fixes.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`: skip Android notification/vibration delivery while foreground recording is active.
- Add `openspec/changes/trailmate-p0-background-off-route-alerts/*`: product and spec delta.

## Task 1: Pure Service Route Monitor

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/TrackRecordingRouteMonitorEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/TrackRecordingRouteMonitorEngine.kt`

- [x] **Step 1: Write failing route monitor tests**

Cover these behaviors:

```kotlin
// reliable accepted point far from matching route -> OFF_ROUTE and shouldNotify=true
// repeated same-episode off-route point inside cooldown -> OFF_ROUTE_SILENT
// worsened same-episode point by at least 50 m -> OFF_ROUTE_ESCALATED
// active episode then point near route -> REJOINED_ROUTE without vibration
// missing or mismatched route geometry -> NONE and reset state
// low-accuracy point for matching route -> WAIT_FOR_RELIABLE_FIX and no notification
```

- [x] **Step 2: Run RED**

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.TrackRecordingRouteMonitorEngineTest" --no-daemon --console=plain
```

Expected: compile failure because `TrackRecordingRouteMonitorEngine` does not exist.

- [x] **Step 3: Implement minimal pure engine**

Create a `TrackRecordingRouteMonitorEngine.evaluate(...)` function that validates matching route geometry, projects accepted recorded points with `RouteGeometryEngine.projectToRoute`, derives `CHECK_ROUTE` when cross-track error is greater than 75 m and `ON_ROUTE` otherwise, then delegates to `RouteDeviationAlertPolicy.evaluate(...)`.

- [x] **Step 4: Run GREEN**

Run the same targeted test. Expected: `BUILD SUCCESSFUL`.

## Task 2: Delivery Ownership Policy

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryOwnerPolicyTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryOwnerPolicy.kt`

- [x] **Step 1: Write failing ownership tests**

Cover:

```kotlin
// route screen may deliver when recording is IDLE, PAUSED, or FINISHED
// route screen must not deliver when foreground recording status is RECORDING
```

- [x] **Step 2: Run RED**

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertDeliveryOwnerPolicyTest" --no-daemon --console=plain
```

Expected: compile failure because the policy does not exist.

- [x] **Step 3: Implement minimal ownership policy**

Create a pure object:

```kotlin
object RouteDeviationAlertDeliveryOwnerPolicy {
    fun routeScreenMayDeliver(trackRecordingStatus: TrackRecordingStatus): Boolean =
        trackRecordingStatus != TrackRecordingStatus.RECORDING
}
```

- [x] **Step 4: Run GREEN**

Run the same targeted test. Expected: `BUILD SUCCESSFUL`.

## Task 3: Foreground Service And Route Screen Integration

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/location/TrackRecordingForegroundService.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Wire service monitor**

In `appendLocation`, load the snapshot once, append the location point as before, and only if the point is accepted evaluate the saved `snapshot.importedRoute` with `TrackRecordingRouteMonitorEngine`. Store the next `RouteDeviationAlertState` in a private service field and call `RouteDeviationAlertAndroidDelivery.deliver(...)` with current notification permission.

- [x] **Step 2: Reset monitor state**

Reset the service route-monitor state when recording pauses, finishes, or the active route key changes.

- [x] **Step 3: Guard route-screen delivery**

Wrap the existing route-screen `RouteDeviationAlertAndroidDelivery.deliver(...)` call with `RouteDeviationAlertDeliveryOwnerPolicy.routeScreenMayDeliver(trackRecording.status)`. Keep all in-app state updates unchanged.

- [x] **Step 4: Run focused tests**

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.TrackRecordingRouteMonitorEngineTest" --tests "com.trailmate.app.core.model.RouteDeviationAlertDeliveryOwnerPolicyTest" --no-daemon --console=plain
```

Expected: `BUILD SUCCESSFUL`.

## Task 4: Verification, Review, And PR

**Files:**
- All files above
- `openspec/changes/trailmate-p0-background-off-route-alerts/*`
- `docs/superpowers/plans/2026-06-25-trailmate-p0-background-off-route-alerts.md`

- [x] **Step 1: Run full verification**

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

- [x] **Step 2: Request reviews**

Use subagents for spec compliance and code quality. Fix Critical or Important findings before committing.

- [ ] **Step 3: Commit and push**

Commit message:

```powershell
git commit -m "feat: monitor off-route alerts while recording"
```

- [ ] **Step 4: Create draft PR**

Open a draft PR from `codex/p0-background-off-route-alerts` into `codex/p0-off-route-alert-delivery`.
