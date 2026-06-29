# Route Deviation Fix Reliability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent route-deviation alerts and rejoin notices from using invalid, stale, or future `HikeLocationFix` values as field evidence.

**Architecture:** Centralize the reliability guard inside `RouteDeviationAlertPolicy` so every caller gets the same behavior. The policy will treat missing, low-accuracy, stale, future, non-positive timestamp, or non-finite fixes as `WAIT_FOR_RELIABLE_FIX` while preserving the current deviation episode state.

**Tech Stack:** Android Kotlin, JUnit, OpenSpec.

---

### Task 1: Route Deviation Fix Reliability

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertPolicy.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertPolicyTest.kt`
- Create: `openspec/changes/trailmate-p0-route-deviation-fix-reliability/proposal.md`
- Create: `openspec/changes/trailmate-p0-route-deviation-fix-reliability/tasks.md`
- Create: `openspec/changes/trailmate-p0-route-deviation-fix-reliability/specs/light-navigation/spec.md`

- [x] **Step 1: Write failing policy tests**

Add tests to `RouteDeviationAlertPolicyTest` proving:

```kotlin
@Test
fun staleOffRouteFixWaitsForReliableFixWithoutAlert() {
    val decision = RouteDeviationAlertPolicy.evaluate(
        status = LocationBackedHikeStatus.CHECK_ROUTE,
        fix = reliableFix(crossTrackErrorMeters = 112.0).copy(timestampEpochMillis = 1_000L),
        state = RouteDeviationAlertState(),
        nowEpochMillis = 62_000L
    )

    assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
    assertFalse(decision.shouldNotify)
    assertFalse(decision.shouldVibrate)
    assertEquals("等待定位稳定", decision.title)
}
```

and:

```kotlin
@Test
fun futureRejoinFixPreservesActiveEpisode() {
    val state = RouteDeviationAlertState(
        activeEpisode = true,
        lastAlertEpochMillis = 10_000L,
        lastAlertCrossTrackErrorMeters = 112.0,
        rejoinNoticeEmitted = false
    )

    val decision = RouteDeviationAlertPolicy.evaluate(
        status = LocationBackedHikeStatus.ON_ROUTE,
        fix = reliableFix(crossTrackErrorMeters = 18.0).copy(timestampEpochMillis = NOW_EPOCH_MILLIS + 1L),
        state = state,
        nowEpochMillis = NOW_EPOCH_MILLIS
    )

    assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
    assertFalse(decision.shouldNotify)
    assertFalse(decision.shouldVibrate)
    assertEquals(state, decision.nextState)
}
```

- [x] **Step 2: Verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertPolicyTest"
```

Expected: the new tests fail because `RouteDeviationAlertPolicy` currently accepts fresh-looking decisions based only on horizontal accuracy.

- [x] **Step 3: Implement minimal guard**

Update `RouteDeviationAlertPolicy` with a private reliability helper:

```kotlin
private fun HikeLocationFix?.isReliable(nowEpochMillis: Long): Boolean =
    this != null &&
        distanceAlongRouteKm.isFinite() &&
        distanceAlongRouteKm >= 0.0 &&
        crossTrackErrorMeters.isFinite() &&
        crossTrackErrorMeters >= 0.0 &&
        horizontalAccuracyMeters.isFinite() &&
        horizontalAccuracyMeters <= MAX_ALERT_ACCURACY_METERS &&
        timestampEpochMillis > 0L &&
        timestampEpochMillis <= nowEpochMillis &&
        nowEpochMillis - timestampEpochMillis <= TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS
```

Use the helper before emitting `REJOINED_ROUTE`, `OFF_ROUTE`, `OFF_ROUTE_SILENT`, or `OFF_ROUTE_ESCALATED`.

- [x] **Step 4: Verify GREEN and regressions**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertPolicyTest"
openspec validate trailmate-p0-route-deviation-fix-reliability --strict
git diff --check
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat test
```

Expected: all commands succeed.

- [ ] **Step 5: Review and PR**

Request a read-only code review focused on policy behavior, episode-state preservation, and OpenSpec alignment before staging, committing, pushing, and creating a PR.
