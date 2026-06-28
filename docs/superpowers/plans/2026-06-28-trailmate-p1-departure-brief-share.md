# Departure Brief Share Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a manual pre-departure itinerary share so a safety contact knows the planned route, expected finish, and confirmation rule before the hiker loses signal.

**Architecture:** Keep this as a deterministic local presentation engine plus one compact route safety card. It must not depend on GPS, server state, contacts, SMS, WeChat SDK, or background alarms.

**Tech Stack:** Kotlin unit tests, Jetpack Compose route screen, OpenSpec change `trailmate-p1-departure-brief-share`.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/DepartureBriefShareEngine.kt`
  - Owns pure Kotlin policy and Chinese share text.
- Create `android-app/src/test/java/com/trailmate/app/core/model/DepartureBriefShareEngineTest.kt`
  - Proves route plan share, active-recording start time, missing duration, and finished-route states.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Adds a compact `DepartureBriefSharePanel` near existing safety share actions.
- Add OpenSpec files under `openspec/changes/trailmate-p1-departure-brief-share/`.

## Task 1: Pure Model

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/DepartureBriefShareEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/DepartureBriefShareEngine.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
@Test
fun sharesDepartureBriefWithoutGps() {
    val presentation = DepartureBriefShareEngine.present(
        plan = DepartureBriefPlan("龙井山脊", 15.2, 860, 410),
        trackRecording = TrackRecordingState(),
        nowEpochMillis = NOW,
        zoneId = ZoneId.of("Asia/Shanghai")
    )

    assertEquals("出发报备", presentation.title)
    assertEquals("可发送", presentation.statusLabel)
    assertTrue(requireNotNull(presentation.shareText).contains("预计完成：2026-06-19 15:50"))
}
```

- [ ] **Step 2: Run test to verify RED**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.DepartureBriefShareEngineTest" --no-daemon --console=plain
```

Expected: FAIL because `DepartureBriefShareEngine` is not defined.

- [ ] **Step 3: Implement minimal model**

```kotlin
data class DepartureBriefPlan(
    val routeName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val estimatedDurationMinutes: Int?
)
```

Use `nowEpochMillis` for not-started plan, `startedAtEpochMillis` for active recording, and refuse to invent finish time when duration is missing.

- [ ] **Step 4: Verify GREEN**

Run the same targeted test. Expected: PASS.

## Task 2: Route UI Entry

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [ ] **Step 1: Add route screen wiring**

Create a `DepartureBriefShareEngine.present(...)` presentation in `GpsTrackPanel` using the existing planned duration fallback:

```kotlin
val departureBriefShare = DepartureBriefShareEngine.present(
    plan = DepartureBriefPlan(route.routeName, route.distanceKm, route.ascentMeters, plannedDurationMinutes),
    trackRecording = trackRecording,
    nowEpochMillis = returnEtaNowEpochMillis
)
```

- [ ] **Step 2: Add compact card**

Render `DepartureBriefSharePanel` before current-location `SafetySharePanel`. Button calls `presentation.shareText?.let(onShareSafetyText)`.

- [ ] **Step 3: Verify compile and tests**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.DepartureBriefShareEngineTest" --no-daemon --console=plain
```

Expected: PASS.

## Task 3: Final Gates

**Files:**
- Modify: `openspec/changes/trailmate-p1-departure-brief-share/tasks.md`

- [ ] **Step 1: Mark OpenSpec tasks done after implementation**
- [ ] **Step 2: Run full validation**

```powershell
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

- [ ] **Step 3: Request read-only product/spec review**
- [ ] **Step 4: Commit, push, and create a draft stacked PR**
