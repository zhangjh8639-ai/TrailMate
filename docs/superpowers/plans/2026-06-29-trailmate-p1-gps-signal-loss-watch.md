# GPS Signal Loss Watch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Warn hikers when active track recording is relying on stale or blocked GPS evidence.

**Architecture:** Add a pure `GpsSignalLossWatchEngine` that evaluates `TrailMateLocationSnapshot`, `TrackRecordingState`, and current time. Route UI consumes the presentation in the existing Route tab safety stack and fullscreen navigation dock, so the feature stays advisory and does not create another navigation mode or GPS toggle.

**Tech Stack:** Kotlin model unit tests, Jetpack Compose route page, OpenSpec change `trailmate-p1-gps-signal-loss-watch`.

---

## File Structure

- Create `android-app/src/test/java/com/trailmate/app/core/model/GpsSignalLossWatchEngineTest.kt`
  - TDD coverage for fresh, stale, severely stale, blocked, low-accuracy stale, non-recording, and safety-copy boundaries.
- Create `android-app/src/main/java/com/trailmate/app/core/model/GpsSignalLossWatchEngine.kt`
  - Pure engine for visibility, tone, copy, action label, and details.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Import the new engine and render route-tab/fullscreen warning panels.
- Add OpenSpec files under `openspec/changes/trailmate-p1-gps-signal-loss-watch/`.

## Task 1: GPS Signal-Loss Engine

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/GpsSignalLossWatchEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/GpsSignalLossWatchEngine.kt`

- [x] **Step 1: Write the failing test**

```kotlin
@Test
fun staleLocatedFixWhileRecordingShowsCaution() {
    val presentation = GpsSignalLossWatchEngine.present(
        snapshot = locatedAt(timestamp = 1_000L),
        trackRecording = recording(),
        nowEpochMillis = 121_000L
    )

    assertTrue(presentation.visible)
    assertEquals("定位停更", presentation.title)
    assertEquals("等待新定位", presentation.statusLabel)
    assertEquals(GpsSignalLossWatchTone.CAUTION, presentation.tone)
    assertEquals("刷新定位", presentation.primaryActionLabel)
    assertTrue(presentation.caption.contains("超过 1 分钟"))
    assertTrue(presentation.caption.contains("停下"))
}
```

- [x] **Step 2: Run test to verify RED**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.GpsSignalLossWatchEngineTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: FAIL because `GpsSignalLossWatchEngine` does not exist.

- [x] **Step 3: Implement minimal engine**

Rules:
- Hide unless `trackRecording.status == TrackRecordingStatus.RECORDING`.
- Hide when `LOCATED` or `LOW_ACCURACY` snapshot age is at most `TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS`.
- Show caution when a coordinate-bearing snapshot is older than 60 seconds.
- Show alert when a coordinate-bearing snapshot is at least 5 minutes old.
- Show alert when recording is active and status is `DISABLED`, `PERMISSION_REQUIRED`, `PROVIDER_DISABLED`, or `UNAVAILABLE`.
- Show caution/alert for `SEARCHING` based on how long the app has been waiting for a fix.
- Copy must tell the hiker to stop, refresh location, and verify offline map/trail markers/visible path without implying rescue, automatic contact, or guaranteed safety.

- [x] **Step 4: Verify GREEN**

Run the targeted test. Expected: PASS.

## Task 2: Route Tab And Fullscreen Hook

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Build presentation in `RouteCockpitTabContent`**

Create:

```kotlin
val gpsSignalLossWatch = GpsSignalLossWatchEngine.present(
    snapshot = locationSnapshot,
    trackRecording = trackRecording,
    nowEpochMillis = locationPresentationNowEpochMillis
)
```

- [x] **Step 2: Render normal Route tab warning**

Render `GpsSignalLossWatchPanel` immediately after `RouteCockpitSection(...)` and before lower-priority watch panels. The primary action calls `onRequestLocation`.

- [x] **Step 3: Render fullscreen compact warning**

Pass `gpsSignalLossWatch` to `RouteNavigationFullscreen(...)` and `RouteNavigationFullscreenDock(...)`. Render a compact banner near `RouteDirectionWatchCompactBanner` so the user sees stale-location risk before acting on route progress.

- [x] **Step 4: Verify compile**

Run targeted Android tests. Expected: PASS.

## Task 3: Final Gates

- [x] **Step 1: Mark OpenSpec tasks complete**

Update `openspec/changes/trailmate-p1-gps-signal-loss-watch/tasks.md` after implementation and verification.

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

Review scope:
- `GpsSignalLossWatchEngine` correctness and thresholds.
- UI placement in normal Route tab and fullscreen navigation.
- Copy boundaries around safety, rescue, and navigation promises.

- [ ] **Step 4: Commit, push, and create a draft stacked PR**

Base branch: `codex/p1-direction-watch`.
