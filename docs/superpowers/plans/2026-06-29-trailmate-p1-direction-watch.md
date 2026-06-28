# Direction Watch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Warn hikers when reliable on-route GPS samples show they are moving backward along the planned route.

**Architecture:** Add a pure `RouteDirectionWatchEngine` that compares the previous reliable route-aligned fix with the latest fix. The route tab stores the previous fix locally and renders a small safety panel only when the engine reports a visible warning. Low-accuracy, stale, off-route, paused, and finished states are treated as out of scope for this panel so existing location/off-route guidance remains authoritative.

**Tech Stack:** Kotlin model unit tests, Jetpack Compose route page, OpenSpec change `trailmate-p1-direction-watch`.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/RouteDirectionWatchEngine.kt`
  - Pure engine for route direction status, tone, copy, action label, and details.
- Create `android-app/src/test/java/com/trailmate/app/core/model/RouteDirectionWatchEngineTest.kt`
  - TDD coverage for reverse movement, jitter tolerance, low confidence suppression, non-recording suppression, and safety-copy boundaries.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Store the previous route-aligned fix and render `RouteDirectionWatchPanel` on the route tab.
- Add OpenSpec files under `openspec/changes/trailmate-p1-direction-watch/`.

## Task 1: Direction Watch Engine

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/RouteDirectionWatchEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/RouteDirectionWatchEngine.kt`

- [x] **Step 1: Write the failing test**

```kotlin
@Test
fun reliableBackwardMovementShowsDirectionAlert() {
    val presentation = RouteDirectionWatchEngine.present(
        previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L),
        currentFix = fixAt(distanceKm = 5.05, timestamp = 91_000L),
        locationStatus = LocationBackedHikeStatus.ON_ROUTE,
        trackRecording = recording()
    )

    assertTrue(presentation.visible)
    assertEquals("方向异常", presentation.title)
    assertEquals("可能反向行进", presentation.statusLabel)
    assertEquals(RouteDirectionWatchTone.ALERT, presentation.tone)
    assertEquals("停下核对方向", presentation.primaryActionLabel)
    assertTrue(presentation.caption.contains("路线进度倒退约 250 m"))
}
```

- [x] **Step 2: Run test to verify RED**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDirectionWatchEngineTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: FAIL because `RouteDirectionWatchEngine` does not exist.

- [x] **Step 3: Implement minimal engine**

Rules:
- Visible alert when both fixes are reliable, route status is `ON_ROUTE`, recording is active, the samples are at least 60 seconds apart, and route distance decreases by at least 0.15 km.
- Hide for normal forward movement, tiny backward movement under 0.15 km, missing previous fix, low accuracy, off-route, paused, finished, stale sample order, or invalid distances.
- Copy must tell the hiker to stop, check map/markers/route direction, and avoid implying automatic rescue, guaranteed safety, or medical diagnosis.

- [x] **Step 4: Verify GREEN**

Run the targeted test. Expected: PASS.

## Task 2: Route Tab Hook

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Track previous fix**

Add a remembered `previousRouteDirectionFix` near `latestLocationFix`. Update it in the location tracking effect only when a new `latestLocationFix` arrives and the prior fix is not the same timestamp.

- [x] **Step 2: Render route direction watch**

Build `RouteDirectionWatchEngine.present(...)` in `RouteWorkspaceTab` and render `RouteDirectionWatchPanel` after `ProgressSafetyWatchPanel`. The primary action should request a fresh location fix.

- [x] **Step 3: Verify compile**

Run the targeted Android test and full Android unit tests. Expected: PASS.

## Task 3: Final Gates

- [x] **Step 1: Mark OpenSpec tasks complete**
- [x] **Step 2: Run validation**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

- [x] **Step 3: Request focused read-only code review**
- [ ] **Step 4: Commit, push, and create a draft stacked PR**
