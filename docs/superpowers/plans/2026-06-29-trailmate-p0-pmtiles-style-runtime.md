# PMTiles Style Runtime Wiring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the PMTiles style asset readiness gate into the real MapLibre route map runtime.

**Architecture:** Add one pure route style policy that resolves the asset manifest before style JSON construction. Keep the route map default geometry-only when no local glyph/sprite assets are available, and allow labels only when the manifest resolves as ready.

**Tech Stack:** Android Kotlin, Jetpack Compose, MapLibre, JUnit, OpenSpec.

---

### Task 1: Runtime Style Policy

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/map/MapLibrePmTilesRouteStylePolicyTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/map/MapLibrePmTilesRouteStylePolicy.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/MapLibrePmTilesStyleAssets.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/MapLibrePmTilesRouteMap.kt`

- [x] **Step 1: Write failing tests**

Add tests proving the route style policy keeps geometry-only style by default, enables labels for complete local assets, and rejects network-backed assets.

- [x] **Step 2: Verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.map.MapLibrePmTilesRouteStylePolicyTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: fail because `MapLibrePmTilesRouteStylePolicy` is missing.

- [x] **Step 3: Implement minimal policy and runtime wiring**

Create a pure policy that calls `MapLibrePmTilesStyleAssetReadinessEngine.resolve(...)`, add an unavailable manifest helper, and replace the route map's direct style factory call with the policy.

- [x] **Step 4: Verify GREEN and regressions**

Run targeted test, full project tests, OpenSpec strict validation, and inspect diff before review.
