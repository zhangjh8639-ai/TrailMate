# TrailMate P1 Route Exit Guidance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a conservative safety-exit decision card to route diagnostics so hikers can compare backtracking to the start versus continuing to the next planned checkpoint or finish.

**Architecture:** Keep the decision deterministic in `RouteExitGuidanceEngine`. The first version only uses route progress, plan checkpoints, GPS reliability, and track recording distance. It must not claim to know nearest roads, exits, or rescue points until TrailMate has explicit offline POI/road data.

**Tech Stack:** Kotlin, Android Compose, OpenSpec, JUnit.

---

### Task 1: Route Exit Guidance

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/model/RouteExitGuidanceEngine.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/model/RouteExitGuidanceEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Create: `openspec/changes/trailmate-p1-route-exit-guidance/proposal.md`
- Create: `openspec/changes/trailmate-p1-route-exit-guidance/specs/light-navigation/spec.md`
- Create: `openspec/changes/trailmate-p1-route-exit-guidance/tasks.md`

- [x] **Step 1: Write failing model tests**

Cover:
- backtracking when the start is closer than the next planned reference;
- continuing to the next checkpoint when it is closer than backtracking;
- requiring reliable GPS before choosing a direction;
- using finish as the next reference near route end.

- [x] **Step 2: Run targeted tests to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteExitGuidanceEngineTest" --no-daemon --console=plain
```

Expected: compile failure because the engine does not exist yet.

- [x] **Step 3: Implement deterministic guidance engine**

Add `RouteExitGuidanceEngine.present(...)` with Chinese presentation copy and no network dependency.

- [x] **Step 4: Wire route page diagnostics**

Show the safety-exit card in the GPS/track diagnostics area after deviation recovery. Only show a button for unreliable GPS, where the action refreshes location.

- [x] **Step 5: Verify GREEN**

Run the targeted test command again. Expected: pass.

- [x] **Step 6: Full verification and review**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
