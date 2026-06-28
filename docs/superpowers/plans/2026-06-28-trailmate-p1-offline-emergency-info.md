# Offline Emergency Info Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a manual emergency info share that helps a hiker describe their route, progress, and reliable GPS state during low-signal or offline situations.

**Architecture:** Implement a pure Kotlin presentation engine that degrades safely when GPS is stale, inaccurate, or missing. Wire one compact route safety card to Android share text; no backend, automatic alerts, contact storage, rescue dispatch, or live tracking.

**Tech Stack:** Kotlin unit tests, Jetpack Compose route screen, Android share intent, OpenSpec change `trailmate-p1-offline-emergency-info`.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/OfflineEmergencyInfoEngine.kt`
  - Owns deterministic emergency text, GPS freshness policy, route/progress details, and no-live-tracking boundary.
- Create `android-app/src/test/java/com/trailmate/app/core/model/OfflineEmergencyInfoEngineTest.kt`
  - Proves fresh location, stale location, poor accuracy, and route progress behavior.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Adds a compact `OfflineEmergencyInfoPanel` near route safety sharing.
- Add OpenSpec files under `openspec/changes/trailmate-p1-offline-emergency-info/`.

## Task 1: Pure Model

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/OfflineEmergencyInfoEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/OfflineEmergencyInfoEngine.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
@Test
fun includesFreshCoordinatesAndProgressInEmergencyText() {
    val presentation = OfflineEmergencyInfoEngine.present(
        routeName = "龙井山脊",
        location = OfflineEmergencyLocation(30.25, 120.12, 8.0, NOW - 30_000L),
        routeSummary = OfflineEmergencyRouteSummary(15.2, 860),
        progress = OfflineEmergencyProgress("当前 CP2", "下一站 CP3", 5.1, true),
        nowEpochMillis = NOW,
        zoneId = ZoneId.of("Asia/Shanghai")
    )

    assertEquals("求助信息", presentation.title)
    assertEquals("定位可用", presentation.statusLabel)
    assertTrue(requireNotNull(presentation.shareText).contains("坐标：30.25000,120.12000"))
}
```

- [ ] **Step 2: Run test to verify RED**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.OfflineEmergencyInfoEngineTest" --no-daemon --console=plain
```

Expected: FAIL because `OfflineEmergencyInfoEngine` is not defined.

- [ ] **Step 3: Implement minimal model**

Create data classes for location, route summary, progress, details, and presentation. Treat coordinates as usable only when latitude/longitude finite, accuracy `0..100m`, timestamp positive, not future, and age `<= 2 minutes`.

- [ ] **Step 4: Verify GREEN**

Run the same targeted test. Expected: PASS.

## Task 2: Route UI Entry

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [ ] **Step 1: Build emergency presentation in `GpsTrackPanel`**

Use the existing route, `locationSnapshot`, `hikeSession`, `trackRecording`, and checkpoint helpers to produce current/next checkpoint labels.

- [ ] **Step 2: Render a compact safety card**

Place `OfflineEmergencyInfoPanel` near departure brief and safety share. Its button calls `onShareTrailMateText(text, chooserTitle)` with chooser title `分享求助信息`.

- [ ] **Step 3: Verify compile and targeted tests**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.OfflineEmergencyInfoEngineTest" --no-daemon --console=plain
```

Expected: PASS.

## Task 3: Final Gates

**Files:**
- Modify: `openspec/changes/trailmate-p1-offline-emergency-info/tasks.md`

- [ ] **Step 1: Mark OpenSpec tasks complete after implementation**
- [ ] **Step 2: Run full validation**

```powershell
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

- [ ] **Step 3: Request read-only product/spec review**
- [ ] **Step 4: Commit, push, and create a draft stacked PR**
