# PMTiles Style Assets Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Gate MapLibre PMTiles labeled offline styles on bundled glyph and sprite assets, while preserving geometry-only offline maps when assets are incomplete.

**Architecture:** Add a small pure policy in `core/map` for style asset manifest/readiness. Extend `MapLibrePmTilesStyleFactory` with an asset-aware overload that keeps the current geometry-only JSON by default and emits labeled layers only for ready local assets.

**Tech Stack:** Kotlin, Android unit tests, MapLibre style JSON, OpenSpec.

---

## Task 1: Style Asset Readiness Policy

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/map/MapLibrePmTilesStyleAssets.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/map/MapLibrePmTilesStyleAssetsTest.kt`

- [ ] **Step 1: Write the failing tests**

Add tests that assert incomplete assets are not ready and complete assets are ready with Chinese copy:

```kotlin
@Test
fun missingGlyphsKeepLabeledOfflineStyleUnavailable() {
    val readiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
        MapLibrePmTilesStyleAssetManifest(
            glyphsUrl = null,
            spriteJsonUrl = "asset://trailmate/maplibre/protomaps/sprite",
            spriteImageUrl = "asset://trailmate/maplibre/protomaps/sprite.png"
        )
    )

    assertFalse(readiness.readyForLabels)
    assertEquals(MapLibrePmTilesStyleAssetStatus.MISSING_GLYPHS, readiness.status)
    assertEquals("离线地图标注资源待补齐", readiness.title)
}

@Test
fun completeAssetsEnableLabeledOfflineStyle() {
    val readiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(completeManifest())

    assertTrue(readiness.readyForLabels)
    assertEquals(MapLibrePmTilesStyleAssetStatus.READY, readiness.status)
    assertEquals("离线地图标注资源已就绪", readiness.title)
}
```

- [ ] **Step 2: Run RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.map.MapLibrePmTilesStyleAssetsTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: compile fails because the new policy does not exist.

- [ ] **Step 3: Implement minimal policy**

Create:

```kotlin
enum class MapLibrePmTilesStyleAssetStatus {
    READY,
    MISSING_GLYPHS,
    MISSING_SPRITE_JSON,
    MISSING_SPRITE_IMAGE
}

data class MapLibrePmTilesStyleAssetManifest(
    val glyphsUrl: String?,
    val spriteJsonUrl: String?,
    val spriteImageUrl: String?
)

data class MapLibrePmTilesStyleAssetReadiness(
    val status: MapLibrePmTilesStyleAssetStatus,
    val readyForLabels: Boolean,
    val title: String,
    val caption: String
)
```

Implement `MapLibrePmTilesStyleAssetReadinessEngine.resolve()` with one missing-asset status at a time and Chinese copy that avoids claiming labels are available when an asset is missing.

- [ ] **Step 4: Run GREEN**

Run the same targeted test command and expect pass.

## Task 2: Asset-Aware Style JSON

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/MapLibrePmTilesStyleFactory.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/map/MapLibrePmTilesStyleFactoryTest.kt`
- Modify: `openspec/changes/trailmate-p0-pmtiles-style-assets/tasks.md`

- [ ] **Step 1: Write failing tests**

Add tests:

```kotlin
@Test
fun incompleteAssetsKeepGeometryOnlyStyle() {
    val styleJson = MapLibrePmTilesStyleFactory.buildStyleJson(
        file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles"),
        styleAssets = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = null,
                spriteJsonUrl = "asset://trailmate/maplibre/protomaps/sprite",
                spriteImageUrl = "asset://trailmate/maplibre/protomaps/sprite.png"
            )
        )
    )

    assertFalse(styleJson.contains("\"text-field\""))
    assertFalse(styleJson.contains("\"icon-image\""))
    assertFalse(styleJson.contains("\"glyphs\""))
    assertFalse(styleJson.contains("\"sprite\""))
}

@Test
fun completeAssetsBuildLabeledOfflineStyle() {
    val styleJson = MapLibrePmTilesStyleFactory.buildStyleJson(
        file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles"),
        styleAssets = MapLibrePmTilesStyleAssetReadinessEngine.resolve(completeManifest())
    )

    assertTrue(styleJson.contains("\"glyphs\":\"asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf\""))
    assertTrue(styleJson.contains("\"sprite\":\"asset://trailmate/maplibre/protomaps/sprite\""))
    assertTrue(styleJson.contains("\"text-field\""))
    assertTrue(styleJson.contains("\"icon-image\""))
}
```

- [ ] **Step 2: Run RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.map.MapLibrePmTilesStyleFactoryTest" --tests "com.trailmate.app.core.map.MapLibrePmTilesStyleAssetsTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: compile fails because the overload does not exist.

- [ ] **Step 3: Implement style overload**

Add `buildStyleJson(file: File, styleAssets: MapLibrePmTilesStyleAssetReadiness): String`. It should call the existing geometry-only style when `readyForLabels == false`; when true, include local `glyphs`, local `sprite`, and conservative label/icon layers sourced from existing vector source layers.

- [x] **Step 4: Run GREEN and final checks**

Run targeted tests, then:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

Expected: all pass.
