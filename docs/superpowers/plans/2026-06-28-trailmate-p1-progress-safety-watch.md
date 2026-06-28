# Progress Safety Watch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a conservative route progress safety watch that warns hikers when actual progress falls materially behind the planned route timeline.

**Architecture:** Add a pure Kotlin presentation engine that compares elapsed recording time, checkpoint timeline, route distance, and current route progress. Surface one route-tab card only for caution/alert states; keep copy focused on manual rest, route shortening, safe-exit review, and optional safety sharing.

**Tech Stack:** Kotlin unit tests, Jetpack Compose route screen, OpenSpec change `trailmate-p1-progress-safety-watch`.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/ProgressSafetyWatchEngine.kt`
  - Owns planned progress interpolation, progress pressure thresholds, and copy boundaries.
- Create `android-app/src/test/java/com/trailmate/app/core/model/ProgressSafetyWatchEngineTest.kt`
  - Proves hidden states, on-plan behavior, caution, alert, and safety-copy boundaries.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Adds a compact progress safety card in the route tab when caution or alert is visible.
- Add OpenSpec files under `openspec/changes/trailmate-p1-progress-safety-watch/`.

## Task 1: Pure Progress Safety Engine

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/ProgressSafetyWatchEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/ProgressSafetyWatchEngine.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
@Test
fun slowProgressWarnsBeforeTheUserPushesFurther() {
    val presentation = ProgressSafetyWatchEngine.present(
        route = route,
        plan = plan,
        trackRecording = activeRecording(startedAt = startedAt),
        fix = fixAt(distanceKm = 2.2),
        nowEpochMillis = startedAt + 150 * 60_000L
    )

    assertEquals("进度偏慢", presentation.statusLabel)
    assertEquals(ProgressSafetyWatchTone.CAUTION, presentation.tone)
    assertFalse(presentation.primaryActionRequiresSafetyShare)
}
```

- [ ] **Step 2: Run test to verify RED**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.ProgressSafetyWatchEngineTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: FAIL because `ProgressSafetyWatchEngine` is not defined.

- [ ] **Step 3: Implement minimal engine**

Create:

```kotlin
data class ProgressSafetyWatchDetail(val label: String, val value: String)
enum class ProgressSafetyWatchTone { CAUTION, ALERT }
data class ProgressSafetyWatchPresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val primaryActionRequiresSafetyShare: Boolean,
    val tone: ProgressSafetyWatchTone?,
    val details: List<ProgressSafetyWatchDetail>
)
```

Rules:
- Hide if no active recording start, no reliable fix/progress, missing checkpoint timeline, or route is finished.
- Interpolate planned distance from checkpoint `timeFromStart`.
- Caution when elapsed time is at least 60 minutes, actual progress is at least 1 km behind planned, and progress ratio is below 75%.
- Alert when elapsed time is at least 90 minutes, actual progress is at least 2 km behind planned, progress ratio is below 60%, and more than 3 km remains.
- Never diagnose fatigue or guarantee safety; use "体力/天气/返程复核" wording.

- [ ] **Step 4: Verify GREEN**

Run the targeted test. Expected: PASS.

## Task 2: Route Tab Surface

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [ ] **Step 1: Build progress safety presentation**

In `RouteCockpitTabContent`, call:

```kotlin
val progressSafetyWatch = ProgressSafetyWatchEngine.present(
    route = route,
    plan = plan,
    trackRecording = trackRecording,
    fix = latestLocationFix,
    nowEpochMillis = returnEtaNowEpochMillis
)
```

- [ ] **Step 2: Render compact visible-only card**

Place `ProgressSafetyWatchPanel` near the other route-safety cards on the main route tab, not hidden inside diagnostics. If `primaryActionRequiresSafetyShare` is true, resolve and share current safety text; otherwise no automatic action.

- [ ] **Step 3: Verify compile and targeted tests**

Run the targeted ProgressSafetyWatchEngine test. Expected: PASS and Android debug Kotlin compiles.

## Task 3: Final Gates

- [ ] **Step 1: Mark OpenSpec tasks complete**
- [ ] **Step 2: Run full validation**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

- [ ] **Step 3: Request read-only code review**
- [ ] **Step 4: Commit, push, and create a draft stacked PR**
