# Departure Safety Start Gate Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent the Route cockpit main action from starting a hike while required departure safety items still need repair.

**Architecture:** Keep the policy inside `RouteCockpitPresentationEngine.primaryAction(...)`, which already receives `DepartureReadinessSummary`. The change should only adjust pre-start `HikeSessionStatus.READY` behavior; active recording and in-progress session controls remain unchanged.

**Tech Stack:** Kotlin model unit tests, OpenSpec change `trailmate-p1-departure-safety-start-gate`.

---

## File Structure

- Modify `android-app/src/test/java/com/trailmate/app/core/model/RouteCockpitPresentationEngineTest.kt`
  - Convert existing offline-base-map pre-start expectations into failing tests for repair-first behavior.
  - Add coverage for optional base map and active recording controls.
- Modify `android-app/src/main/java/com/trailmate/app/core/model/RouteCockpitPresentationEngine.kt`
  - Update the pre-start primary action policy to honor all non-start departure readiness primary actions.
- Add OpenSpec files under `openspec/changes/trailmate-p1-departure-safety-start-gate/`.

## Task 1: Route Cockpit Start Gate

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteCockpitPresentationEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteCockpitPresentationEngine.kt`

- [x] **Step 1: Write failing tests**

Update these existing tests so required offline map issues expect repair actions instead of start:

```kotlin
assertEquals("导入离线地图包", presentation.primaryAction.label)
assertEquals(RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP, presentation.primaryAction.kind)
```

For tile verification:

```kotlin
assertEquals("飞行模式验证底图", presentation.primaryAction.label)
assertEquals(RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP, presentation.primaryAction.kind)
```

Add a missing critical gear case:

```kotlin
assertEquals("补齐 1 件关键装备", presentation.primaryAction.label)
assertEquals(RouteCockpitPrimaryActionKind.SHOW_GEAR, presentation.primaryAction.kind)
```

Keep an optional offline base map test expecting `START_HIKE`, and keep active recording tests expecting pause/resume.

- [x] **Step 2: Run test to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteCockpitPresentationEngineTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: FAIL because required offline base map cases still produce `START_HIKE`.

- [x] **Step 3: Implement minimal policy change**

Change the first `session.status == HikeSessionStatus.READY` branch in `RouteCockpitPresentationEngine.primaryAction(...)` to return `departureReadiness.primaryRepairAction()` for any non-start departure primary action. Preserve the later active recording cases before applying departure repair actions to already-active sessions.

- [x] **Step 4: Verify GREEN**

Run the targeted test command again. Expected: PASS.

## Task 2: Final Gates

- [x] **Step 1: Mark OpenSpec tasks complete**

Update `openspec/changes/trailmate-p1-departure-safety-start-gate/tasks.md` after implementation and verification.

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
- Required departure readiness repair actions block `START_HIKE`.
- Optional/recommended offline map gaps remain advisory.
- Active recording controls remain primary.

- [ ] **Step 4: Commit, push, and create a draft stacked PR**

Base branch: `codex/p1-backtrack-breadcrumb-guidance`.
