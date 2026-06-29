# PMTiles Style Asset Diagnostics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Surface PMTiles offline label asset readiness in route map setup diagnostics without blocking geometry-only offline map use.

**Architecture:** Extend the pure `TrailMapReadinessEngine` with an optional `MapLibrePmTilesStyleAssetReadiness` input. When the MapLibre PMTiles basemap is ready, append a focused "地图标注" setup step that reports whether local glyph/sprite assets are ready. Wire `RouteDetailScreen` to resolve and pass the same runtime style asset readiness used by the route map.

**Tech Stack:** Android Kotlin, JUnit, OpenSpec.

---

### Task 1: PMTiles Style Asset Setup Step

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/TrailMapReadiness.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/map/TrailMapReadinessEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Write failing tests**

Add tests proving a ready PMTiles map reports `地图标注=待补齐` when style assets are missing and `地图标注=已就绪` when local glyph/sprite assets are complete.

- [x] **Step 2: Verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.map.TrailMapReadinessEngineTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: fail because `TrailMapReadinessEngine` does not expose a map-label setup step yet.

- [x] **Step 3: Implement minimal readiness step and wiring**

Add the optional readiness parameter, append the step only when PMTiles is the selected production basemap, and pass the runtime style asset readiness from `RouteDetailScreen`.

- [x] **Step 4: Verify GREEN and regressions**

Run targeted tests, full Gradle tests, OpenSpec strict validation, and diff check.
