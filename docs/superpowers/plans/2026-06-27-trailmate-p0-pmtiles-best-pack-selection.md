# TrailMate P0 PMTiles Best Pack Selection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prefer the smallest valid PMTiles catalog pack when multiple remote packs fully cover the imported route bounds.

**Architecture:** Keep server catalog filtering unchanged: the server may return any PMTiles packs that fully contain the route bounds. Narrow Android selection to filter eligible MVT packs first, then choose the eligible item with the smallest positive `sizeBytes`; unknown or non-positive sizes stay usable but rank after known positive sizes.

**Tech Stack:** Kotlin Android unit tests, OpenSpec.

---

### Task 1: Android PMTiles Catalog Best Pack Selection

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/network/TrailMateOfflineBasemapCatalogSelectionPolicyTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMateOfflineBasemapCatalogSelectionPolicy.kt`
- Create: `openspec/changes/trailmate-p0-pmtiles-best-pack-selection/proposal.md`
- Create: `openspec/changes/trailmate-p0-pmtiles-best-pack-selection/specs/light-navigation/spec.md`

- [x] **Step 1: Write the failing test**

Add this test to `TrailMateOfflineBasemapCatalogSelectionPolicyTest`:

```kotlin
@Test
fun selectsSmallestKnownSizeVectorPackCoveringRouteBounds() {
    val selection = TrailMateOfflineBasemapCatalogSelectionPolicy.selectForRoute(
        routeBounds = PmTilesLatLngBounds(
            minLongitude = 120.05,
            minLatitude = 30.10,
            maxLongitude = 120.25,
            maxLatitude = 30.35
        ),
        catalog = listOf(
            catalogItem(
                packId = "pmtiles_hangzhou_large",
                sizeBytes = 240_000_000L
            ),
            catalogItem(
                packId = "pmtiles_hangzhou_compact",
                sizeBytes = 80_000_000L
            )
        )
    )

    assertEquals("pmtiles_hangzhou_compact", selection?.packId)
}
```

Update the `catalogItem` test helper with a `sizeBytes: Long? = 120_000_000L` parameter and pass it into `TrailMatePmTilesBasemapCatalogItemDto`.

- [x] **Step 2: Run the targeted test to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMateOfflineBasemapCatalogSelectionPolicyTest" --no-daemon --console=plain
```

Expected: the new test fails because the current policy selects the first eligible pack.

- [x] **Step 3: Implement minimal policy change**

Change `TrailMateOfflineBasemapCatalogSelectionPolicy.selectForRoute` to filter eligible catalog items and select the one with the smallest positive `sizeBytes`. Use this helper:

```kotlin
private fun TrailMatePmTilesBasemapCatalogItemDto.knownPositiveSizeRank(): Long =
    sizeBytes?.takeIf { it > 0L } ?: Long.MAX_VALUE
```

- [x] **Step 4: Verify GREEN**

Run the same targeted Android unit test command. Expected: pass.

- [x] **Step 5: Full verification**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
