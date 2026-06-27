# TrailMate P0 PMTiles Route Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Require PMTiles route packs to fully contain route bounds before server catalog listing, Android selection, import, or readiness.

**Architecture:** Add contains helpers to server and Android bounds types, then replace route-pack suitability checks from intersects to contains. Keep existing bounding-box approach; do not add geometry buffering or multi-pack stitching.

**Tech Stack:** Kotlin Android unit tests, Java Spring Boot server tests, OpenSpec.

---

### Task 1: Android Coverage Semantics

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/PmTilesArchiveHeaderParser.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/PmTilesOfflineBasemapImportPolicy.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/PmTilesOfflineBasemapStatus.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMateOfflineBasemapCatalogSelectionPolicy.kt`
- Modify tests under `android-app/src/test/java/com/trailmate/app/core/map` and `android-app/src/test/java/com/trailmate/app/core/network`

- [x] **Step 1: Write failing Android tests**

Add tests proving that a PMTiles/catalog bounds rectangle that intersects but does not contain target route bounds is rejected by:

- `TrailMateOfflineBasemapCatalogSelectionPolicy`;
- `PmTilesOfflineBasemapImportPolicy`;
- `PmTilesOfflineBasemapManifestReader` / readiness.

- [x] **Step 2: Run targeted Android tests to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMateOfflineBasemapCatalogSelectionPolicyTest" --tests "com.trailmate.app.core.map.PmTilesOfflineBasemapImportPolicyTest" --tests "com.trailmate.app.core.map.PmTilesOfflineBasemapManifestReaderTest" --no-daemon --console=plain
```

Expected: partial-overlap tests fail because current checks use `intersects`.

- [x] **Step 3: Implement Android contains checks**

Add `PmTilesLatLngBounds.contains(other)` and use it in catalog selection, import policy, and manifest reader.

- [x] **Step 4: Verify Android GREEN**

Run the same targeted Android command. Expected: pass.

### Task 2: Server Catalog Coverage

**Files:**
- Modify: `trailmate-server/src/main/java/com/trailmate/server/map/OfflineBasemapBounds.java`
- Modify: `trailmate-server/src/main/java/com/trailmate/server/map/OfflineBasemapService.java`
- Modify: `trailmate-server/src/test/java/com/trailmate/server/map/OfflineBasemapServiceTest.java`

- [x] **Step 1: Write failing server test**

Add a test proving `OfflineBasemapService.listPmTilesCatalog(...)` excludes a pack that intersects but does not fully contain the requested route bounds.

- [x] **Step 2: Run targeted server test to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.map.OfflineBasemapServiceTest" --no-daemon --console=plain
```

Expected: partial-overlap server test fails because current filtering uses `intersects`.

- [x] **Step 3: Implement server contains checks**

Add `OfflineBasemapBounds.contains(other)` and filter PMTiles packs with `pack.bounds().contains(routeBounds)` via a public method on `OfflineBasemapCatalogItem`.

- [x] **Step 4: Full verification**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
