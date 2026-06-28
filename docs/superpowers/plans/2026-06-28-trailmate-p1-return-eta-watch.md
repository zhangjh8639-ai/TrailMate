# TrailMate P1 Return ETA Watch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a deterministic return-ETA watch to the route safety area so TrailMate can tell hikers whether their current recording is still within the planned return window or overdue.

**Architecture:** Keep time-window policy in a pure Kotlin `ReturnEtaWatchEngine`. The UI consumes a presentation object and reuses the existing safety-share action when the hike is overdue. This slice does not add background alarms, contact storage, SMS, or server sync.

**Tech Stack:** Kotlin, Android Compose, OpenSpec, JUnit.

---

### Task 1: Return ETA Watch

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/model/ReturnEtaWatchEngine.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/model/ReturnEtaWatchEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Create: `openspec/changes/trailmate-p1-return-eta-watch/proposal.md`
- Create: `openspec/changes/trailmate-p1-return-eta-watch/specs/light-navigation/spec.md`
- Create: `openspec/changes/trailmate-p1-return-eta-watch/tasks.md`

- [x] **Step 1: Write failing model tests**

Cover:
- route has not started, so no return countdown is active;
- active recording before planned finish shows remaining planned time;
- active recording after planned finish but before grace window warns the hiker;
- active recording after grace window escalates to share-location guidance;
- missing duration refuses to invent an ETA;
- finished recording shifts to wrap-up copy.

- [x] **Step 2: Run targeted test to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.ReturnEtaWatchEngineTest" --no-daemon --console=plain
```

Expected: compile failure because `ReturnEtaWatchEngine` does not exist yet.

- [x] **Step 3: Implement deterministic ETA watch engine**

Add `ReturnEtaWatchEngine.present(...)` with:
- a 60-minute confirmation grace window after the planned finish;
- no ETA when route duration is missing or invalid;
- Chinese copy only;
- no emergency dispatch or automatic contact claims.

- [x] **Step 4: Wire route page safety card**

Show the return ETA card inside `GpsTrackPanel` near safety sharing. When overdue, its primary action should attempt a fresh `SafetyShareActionEngine.resolveShareAction(...)`; if location is not shareable, request location refresh.

- [x] **Step 5: Verify GREEN**

Run the targeted test command again. Expected: pass.

- [x] **Step 5b: Preserve overdue safety-share timing**

Add a failing `SafetyShareEngineTest` that shares after the confirmation window and verifies TrailMate keeps the original expected finish time instead of recalculating a new future finish from the click time.

- [x] **Step 6: Full verification and review**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:clean :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
