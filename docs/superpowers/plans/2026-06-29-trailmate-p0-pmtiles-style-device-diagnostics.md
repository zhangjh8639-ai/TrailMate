# PMTiles Style Asset Device Diagnostics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Include PMTiles offline label asset readiness in the copied physical-device diagnostics report.

**Architecture:** Extend `TrailMateDeviceDiagnosticsReportFormatter.format` with an optional `MapLibrePmTilesStyleAssetReadiness`. When provided, append compact PMTiles style asset lines that identify status, label readiness, and safe local asset URLs. Wire `RouteDetailScreen` to pass the already-resolved runtime readiness into diagnostics copy.

**Tech Stack:** Android Kotlin, JUnit, OpenSpec.

---

### Task 1: PMTiles Style Asset Device Diagnostics

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/TrailMateDeviceDiagnosticsReportFormatter.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/map/TrailMateDeviceDiagnosticsReportFormatterTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Write failing formatter tests**

Add tests proving missing style assets are reported as not ready and complete local assets expose glyph/sprite URLs without leaking network URLs.

- [x] **Step 2: Verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.map.TrailMateDeviceDiagnosticsReportFormatterTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: fail because the formatter does not accept or print PMTiles style asset readiness yet.

- [x] **Step 3: Implement minimal formatter and route wiring**

Add optional report lines and pass `mapLibrePmTilesStyleAssetReadiness` from `RouteDetailScreen`.

- [x] **Step 4: Verify GREEN and regressions**

Run targeted tests, full Gradle tests, OpenSpec strict validation, and diff check.
