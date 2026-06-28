# Low Power Guidance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add actionable low-power hiking guidance so users know what to do when phone battery becomes a navigation risk.

**Architecture:** Build a deterministic Kotlin presentation engine from existing `RouteBatteryStatus`, route recording state, and offline readiness flags. Wire one compact route safety card only when battery is low or critical; do not toggle Android power settings, disable GPS, or promise battery life.

**Tech Stack:** Kotlin unit tests, Jetpack Compose route screen, OpenSpec change `trailmate-p1-low-power-guidance`.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/LowPowerGuidanceEngine.kt`
  - Owns low/critical battery guidance, ordered manual actions, and safety boundaries.
- Create `android-app/src/test/java/com/trailmate/app/core/model/LowPowerGuidanceEngineTest.kt`
  - Proves hidden normal/unknown states, low battery guidance, and critical battery guidance.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Adds a compact low-power guidance panel in route diagnostics.
- Add OpenSpec files under `openspec/changes/trailmate-p1-low-power-guidance/`.

## Task 1: Pure Model

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/LowPowerGuidanceEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/LowPowerGuidanceEngine.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
@Test
fun criticalBatteryPrioritizesExitAndPowerBank() {
    val presentation = LowPowerGuidanceEngine.present(
        batteryStatus = RouteBatteryStatus.fromPercent(12),
        trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
        offlineRouteReady = true,
        offlineBaseMapReady = true
    )

    assertEquals("电量危险", presentation.statusLabel)
    assertTrue(presentation.primaryActionLabel.contains("撤退"))
}
```

- [ ] **Step 2: Run test to verify RED**

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.LowPowerGuidanceEngineTest" --no-daemon --console=plain
```

Expected: FAIL because `LowPowerGuidanceEngine` is not defined.

- [ ] **Step 3: Implement minimal engine**

Create `LowPowerGuidancePresentation`, `LowPowerGuidanceAction`, `LowPowerGuidanceTone`, and `LowPowerGuidanceEngine.present(...)`.

- [ ] **Step 4: Verify GREEN**

Run the same targeted test. Expected: PASS.

## Task 2: Route UI Entry

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [ ] **Step 1: Build low-power presentation**

Use `routeBatteryStatus`, `trackRecording`, `offlineRoutePackReady`, and map readiness to build `LowPowerGuidanceEngine.present(...)`.

- [ ] **Step 2: Render compact card**

Show `LowPowerGuidancePanel` only when `presentation.visible`. The primary action should request location only when guidance asks for a final reliable fix; otherwise it is informational.

- [ ] **Step 3: Verify compile and targeted tests**

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.LowPowerGuidanceEngineTest" --no-daemon --console=plain
```

Expected: PASS.

## Task 3: Final Gates

- [ ] **Step 1: Mark OpenSpec tasks complete**
- [ ] **Step 2: Run full validation**

```powershell
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

- [ ] **Step 3: Request read-only product/spec review**
- [ ] **Step 4: Commit, push, and create a draft stacked PR**
