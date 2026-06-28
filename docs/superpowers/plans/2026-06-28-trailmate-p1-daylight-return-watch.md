# Daylight Return Watch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Warn hikers on the route tab when their current expected finish approaches or exceeds the local daylight window.

**Architecture:** Add a pure Kotlin daylight safety engine with a deterministic local sunset estimator based on route coordinates and date. Surface a visible-only route safety card near existing route safety cards; keep all actions manual and conservative.

**Tech Stack:** Kotlin unit tests, Java time APIs, Jetpack Compose route screen, OpenSpec change `trailmate-p1-daylight-return-watch`.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/DaylightReturnWatchEngine.kt`
  - Owns daylight window estimation, finish-window thresholds, Chinese presentation copy, and safety-share escalation.
- Create `android-app/src/test/java/com/trailmate/app/core/model/DaylightReturnWatchEngineTest.kt`
  - Covers hidden states, safe daylight window, caution, alert, coordinate/date validation, and honest safety copy.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Builds and renders the daylight card on the route tab when visible.
- Add OpenSpec files under `openspec/changes/trailmate-p1-daylight-return-watch/`.

## Task 1: Daylight Safety Engine

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/DaylightReturnWatchEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/DaylightReturnWatchEngine.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
@Test
fun expectedFinishNearSunsetShowsCaution() {
    val presentation = DaylightReturnWatchEngine.present(
        route = route,
        trackRecording = activeRecording,
        expectedFinishEpochMillis = Instant.parse("2026-06-19T10:20:00Z").toEpochMilli(),
        nowEpochMillis = Instant.parse("2026-06-19T08:20:00Z").toEpochMilli(),
        zoneId = ZoneId.of("Asia/Shanghai")
    )

    assertTrue(presentation.visible)
    assertEquals("日照窗口收紧", presentation.statusLabel)
    assertEquals(DaylightReturnWatchTone.CAUTION, presentation.tone)
}
```

- [ ] **Step 2: Run test to verify RED**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.DaylightReturnWatchEngineTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: FAIL because `DaylightReturnWatchEngine` is not defined.

- [ ] **Step 3: Implement minimal engine**

Use:
- Hide when route geometry is missing, recording is idle/finished, no route centroid can be estimated, or expected finish is missing while the current time is still comfortably before sunset.
- Estimate sunset and civil dusk locally from route centroid and date.
- Show caution when expected finish is within 45 minutes before sunset and route remains active.
- Show alert when expected finish is after sunset, current time is after sunset while recording, or expected finish reaches civil dusk.
- Primary action is manual safety sharing only for alert states.
- Copy must not imply weather visibility, rescue dispatch, automatic contact, or guaranteed safety.

- [ ] **Step 4: Verify GREEN**

Run the targeted daylight test. Expected: PASS.

## Task 2: Route Tab Surface

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [ ] **Step 1: Build presentation**

In `RouteCockpitTabContent`, call `DaylightReturnWatchEngine.present()` with route, track recording, active expected finish time, route-tab clock, and `ZoneId.systemDefault()`.

- [ ] **Step 2: Render visible-only card**

Place `DaylightReturnWatchPanel` near `ProgressSafetyWatchPanel` and `LowPowerGuidancePanel`. If the action requires safety share, reuse `handleSafetyShare()`.

- [ ] **Step 3: Verify compile**

Run targeted daylight tests and Android unit tests. Expected: PASS.

## Task 3: Final Gates

- [ ] **Step 1: Mark OpenSpec tasks complete**
- [ ] **Step 2: Run validation**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

- [ ] **Step 3: Request focused code review**
- [ ] **Step 4: Commit, push, and create a draft stacked PR**
