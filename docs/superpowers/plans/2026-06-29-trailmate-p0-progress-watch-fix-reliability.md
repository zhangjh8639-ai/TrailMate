# P0 Progress Watch Fix Reliability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent TrailMate from showing progress safety warnings from stale, future-dated, malformed, or low-accuracy route-progress fixes.

**Architecture:** Keep the change in `ProgressSafetyWatchEngine`, mirroring the reliability boundaries already used by location-backed hike sessions, deviation alerts, and safe-exit guidance. Treat unreliable fixes as missing progress evidence, so the existing hidden presentation path remains the single fallback.

**Tech Stack:** Kotlin, Android unit tests, OpenSpec.

---

### Task 1: Add Reliability Tests

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/ProgressSafetyWatchEngineTest.kt`

- [x] **Step 1: Write the failing tests**

Add tests that pass an otherwise behind-schedule fix with stale, future, low-accuracy, negative, and non-finite values:

```kotlin
@Test
fun unreliableProgressFixDoesNotShowSafetyWatch() {
    val unreliableFixes = listOf(
        fixAt(distanceKm = 3.3, nowEpochMillis = startedAt + 120 * 60_000L).copy(
            timestampEpochMillis = startedAt + 120 * 60_000L - 61_000L
        ),
        fixAt(distanceKm = 3.3, nowEpochMillis = startedAt + 120 * 60_000L).copy(
            timestampEpochMillis = startedAt + 120 * 60_000L + 1L
        ),
        fixAt(distanceKm = 3.3, nowEpochMillis = startedAt + 120 * 60_000L).copy(
            horizontalAccuracyMeters = 51.0
        ),
        fixAt(distanceKm = 3.3, nowEpochMillis = startedAt + 120 * 60_000L).copy(
            distanceAlongRouteKm = -0.1
        ),
        fixAt(distanceKm = 3.3, nowEpochMillis = startedAt + 120 * 60_000L).copy(
            crossTrackErrorMeters = Double.NaN
        )
    )

    unreliableFixes.forEach { fix ->
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = activeRecording(startedAt),
            fix = fix,
            nowEpochMillis = startedAt + 120 * 60_000L
        )

        assertFalse(presentation.visible)
    }
}
```

Also update the helper so existing positive tests supply fresh timestamps:

```kotlin
private fun fixAt(
    distanceKm: Double,
    nowEpochMillis: Long
): HikeLocationFix =
    HikeLocationFix(
        distanceAlongRouteKm = distanceKm,
        crossTrackErrorMeters = 8.0,
        horizontalAccuracyMeters = 6.0,
        timestampEpochMillis = nowEpochMillis - 30_000L
    )
```

- [x] **Step 2: Run test to verify it fails**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests com.trailmate.app.core.model.ProgressSafetyWatchEngineTest --no-daemon
```

Expected: `unreliableProgressFixDoesNotShowSafetyWatch` fails because the current engine uses stale or low-quality fixes as progress evidence.

### Task 2: Guard Progress Fix Reliability

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/ProgressSafetyWatchEngine.kt`

- [x] **Step 1: Write minimal implementation**

Import shared fix-age policy and replace direct progress extraction with a reliability gate:

```kotlin
import com.trailmate.app.core.location.TrailMateLocationFixReliability
```

```kotlin
val reliableFix = fix.takeIf { it.isReliable(nowEpochMillis) } ?: return hidden()
val actualProgressKm = reliableFix.distanceAlongRouteKm
    .coerceAtMost(route.distanceKm.coerceAtLeast(0.0))
```

Add a local helper:

```kotlin
private fun HikeLocationFix.isReliable(nowEpochMillis: Long): Boolean =
    distanceAlongRouteKm.isFinite() &&
        distanceAlongRouteKm >= 0.0 &&
        crossTrackErrorMeters.isFinite() &&
        crossTrackErrorMeters >= 0.0 &&
        horizontalAccuracyMeters.isFinite() &&
        horizontalAccuracyMeters >= 0.0 &&
        horizontalAccuracyMeters <= MAX_PROGRESS_WATCH_ACCURACY_METERS &&
        timestampEpochMillis > 0L &&
        timestampEpochMillis <= nowEpochMillis &&
        nowEpochMillis - timestampEpochMillis <=
        TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS
```

```kotlin
private const val MAX_PROGRESS_WATCH_ACCURACY_METERS = 50.0
```

- [x] **Step 2: Run target tests**

Run the same Gradle command from Task 1. Expected: all `ProgressSafetyWatchEngineTest` tests pass.

### Task 3: Verify and Review

**Files:**
- Validate all changed files.

- [x] **Step 1: Validate OpenSpec**

```powershell
openspec validate trailmate-p0-progress-watch-fix-reliability --strict
```

Expected: strict validation succeeds.

- [x] **Step 2: Run Android tests**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test --no-daemon
```

Expected: full test suite passes.

- [x] **Step 3: Request code review**

Ask a subagent to review the diff from `codex/p0-safe-exit-fix-reliability` to `codex/p0-progress-watch-fix-reliability`, focusing on whether unreliable progress fixes can still trigger a warning.
