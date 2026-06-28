# TrailMate P0 PMTiles Storage Preflight Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent remote PMTiles downloads from starting when the selected catalog pack's known remaining bytes exceed local usable storage.

**Architecture:** Keep server catalog and downloader APIs unchanged. Add a small Android coordinator preflight that uses catalog `sizeBytes`, existing `.download` partial size, and target directory usable space before calling the downloader; unknown sizes remain compatible and skip this preflight.

**Tech Stack:** Kotlin Android unit tests, OpenSpec.

---

### Task 1: Android Remote PMTiles Storage Preflight

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/network/TrailMatePmTilesBasemapRemoteImportCoordinatorTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMatePmTilesBasemapRemoteImportCoordinator.kt`
- Create: `openspec/changes/trailmate-p0-pmtiles-storage-preflight/proposal.md`
- Create: `openspec/changes/trailmate-p0-pmtiles-storage-preflight/specs/light-navigation/spec.md`
- Create: `openspec/changes/trailmate-p0-pmtiles-storage-preflight/tasks.md`

- [x] **Step 1: Write failing storage preflight tests**

Add tests proving:
- a selected catalog pack with known `sizeBytes` larger than usable local storage returns `OPEN_LOCAL_PICKER` and does not call the downloader;
- an existing `.download` partial file reduces the known remaining bytes, so a resumable download can still be attempted when usable storage covers only the remaining bytes.

- [x] **Step 2: Run targeted tests to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMatePmTilesBasemapRemoteImportCoordinatorTest" --no-daemon --console=plain
```

Expected: compile or assertion failure because the coordinator has no storage preflight yet.

- [x] **Step 3: Implement minimal storage preflight**

Add a constructor dependency with a production default:

```kotlin
private val usableStorageBytes: (File) -> Long = { directory -> directory.usableSpace }
```

After selecting the catalog item and resolving the temporary download file, compute:

```kotlin
val expected = selected.sizeBytes?.takeIf { it > 0L }
val existing = if (temporaryFile.isFile) temporaryFile.length().coerceAtLeast(0L) else 0L
val remaining = (expected - existing).coerceAtLeast(0L)
```

If `expected` is known and `remaining > usableStorageBytes(targetDirectory)`, return local picker with a storage-specific message and do not call the downloader.

- [x] **Step 4: Verify GREEN**

Run the targeted test command again. Expected: pass.

- [x] **Step 5: Full verification**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
