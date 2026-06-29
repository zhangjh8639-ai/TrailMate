# PMTiles Style Manifest Runtime Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Resolve bundled MapLibre PMTiles glyph/sprite assets at runtime and pass a safe manifest into the route map.

**Architecture:** Add a pure bundled asset manifest resolver that only returns a style manifest when the required local asset probes exist. Wire `RouteDetailScreen` to use Android assets as the probe source and pass the resolved manifest into `MapLibrePmTilesRouteMap`. Make labeled style layers use the same font stack as the glyph probe.

**Tech Stack:** Android Kotlin, Jetpack Compose, MapLibre, JUnit, OpenSpec.

---

### Task 1: Bundled Style Asset Manifest Runtime

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/map/MapLibrePmTilesBundledStyleAssetManifestResolver.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/map/MapLibrePmTilesBundledStyleAssetManifestResolverTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/MapLibrePmTilesStyleFactory.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/map/MapLibrePmTilesStyleFactoryTest.kt`

- [x] **Step 1: Write failing tests**

Add tests for complete bundled probes, missing bundled probes, and the labeled style font stack.

- [x] **Step 2: Verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.map.MapLibrePmTilesBundledStyleAssetManifestResolverTest" --tests "com.trailmate.app.core.map.MapLibrePmTilesStyleFactoryTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: fail because the resolver and explicit label font stack do not exist yet.

- [x] **Step 3: Implement minimal resolver and runtime wiring**

Create the resolver with fixed TrailMate asset paths and pass its result from `RouteDetailScreen` into `MapLibrePmTilesRouteMap`.

- [x] **Step 4: Verify GREEN and regressions**

Run targeted tests, full Gradle tests, OpenSpec strict validation, and diff check.
