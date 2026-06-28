# Backtrack Breadcrumb Guidance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tell hikers whether the recorded walked track is usable as a conservative original-path return reference.

**Architecture:** Add a pure `BacktrackBreadcrumbGuidanceEngine` that evaluates only `TrackRecordingState` and current time. The Route tab renders a compact advisory card near existing exit guidance, keeping it in the safety stack instead of introducing a new navigation mode.

**Tech Stack:** Kotlin model unit tests, Jetpack Compose route page, OpenSpec change `trailmate-p1-backtrack-breadcrumb-guidance`.

---

## File Structure

- Create `android-app/src/test/java/com/trailmate/app/core/model/BacktrackBreadcrumbGuidanceEngineTest.kt`
  - TDD coverage for ready, warming-up, stale, paused, idle, and copy-boundary states.
- Create `android-app/src/main/java/com/trailmate/app/core/model/BacktrackBreadcrumbGuidanceEngine.kt`
  - Pure presentation engine for tone, copy, action label, and details.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Import the engine, build the presentation in `GpsTrackPanel`, and render a compact guidance panel near `RouteExitGuidancePanel`.
- Add OpenSpec files under `openspec/changes/trailmate-p1-backtrack-breadcrumb-guidance/`.

## Task 1: Backtrack Breadcrumb Engine

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/BacktrackBreadcrumbGuidanceEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/BacktrackBreadcrumbGuidanceEngine.kt`

- [x] **Step 1: Write the failing test**

```kotlin
@Test
fun freshRecordingWithMeaningfulTrackIsReadyForBacktrackingReference() {
    val presentation = BacktrackBreadcrumbGuidanceEngine.present(
        trackRecording = recording(
            totalDistanceKm = 0.42,
            points = listOf(pointAt(10_000L), pointAt(70_000L))
        ),
        nowEpochMillis = 100_000L
    )

    assertTrue(presentation.visible)
    assertEquals(BacktrackBreadcrumbGuidanceTone.READY, presentation.tone)
    assertEquals("原路参照可用", presentation.statusLabel)
    assertTrue(presentation.caption.contains("沿已记录轨迹"))
    assertTrue(presentation.caption.contains("不要抄近路"))
    assertEquals("0.4 km", presentation.details.first { it.label == "已记录" }.value)
}
```

- [x] **Step 2: Run test to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.BacktrackBreadcrumbGuidanceEngineTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: FAIL because `BacktrackBreadcrumbGuidanceEngine` does not exist.

- [x] **Step 3: Implement minimal engine**

Rules:
- Return an advisory presentation for all recording states; never claim nearest road, exit, rescue point, or turn-by-turn navigation.
- `READY` when status is `RECORDING`, at least two points exist, latest point age is at most 5 minutes, and `totalDistanceKm >= 0.1`.
- `CAUTION` when status is `RECORDING` but the track is still warming up.
- `ALERT` when status is `RECORDING` and the latest point is older than 5 minutes.
- `CAUTION` when status is `PAUSED` and at least two points exist, with copy saying the breadcrumb only covers recorded movement before pause.
- `UNAVAILABLE` tone when status is `IDLE` or no points exist.
- Details include recorded distance, point count, and latest point age or recording state.

- [x] **Step 4: Verify GREEN**

Run the targeted test command again. Expected: PASS.

## Task 2: Route Tab Hook

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Build presentation in `GpsTrackPanel`**

Create:

```kotlin
val breadcrumbGuidance = BacktrackBreadcrumbGuidanceEngine.present(
    trackRecording = trackRecording,
    nowEpochMillis = locationPresentationNowEpochMillis
)
```

- [x] **Step 2: Render compact guidance**

Render `BacktrackBreadcrumbGuidancePanel(presentation = breadcrumbGuidance)` after `RouteExitGuidancePanel(...)`, so it reads as supporting evidence for exit decisions rather than another main map mode.

- [x] **Step 3: Verify compile**

Run targeted Android tests. Expected: PASS.

## Task 3: Final Gates

- [x] **Step 1: Mark OpenSpec tasks complete**

Update `openspec/changes/trailmate-p1-backtrack-breadcrumb-guidance/tasks.md` after implementation and verification.

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
- `BacktrackBreadcrumbGuidanceEngine` states, thresholds, and boundary copy.
- Route tab placement near exit guidance without creating another navigation mode.
- Safety copy boundaries around rescue, nearest-road, and turn-by-turn claims.

- [ ] **Step 4: Commit, push, and create a draft stacked PR**

Base branch: `codex/p1-gps-signal-loss-watch`.
