# TrailMate P0 PMTiles Resumable Download Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make PMTiles route-pack downloads retry-safe by adding Android Range resume behavior and server byte-range regression coverage.

**Architecture:** Keep this feature in the existing PMTiles network boundary. Android still validates downloaded archives locally before readiness, and the server remains a safe static PMTiles file endpoint whose Spring MVC resource response is covered for byte-range support.

**Tech Stack:** Kotlin, JUnit4, Java 17, Spring MVC, JUnit5, MockMvc, OpenSpec.

---

### Task 1: Android Downloader Contract

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMatePmTilesBasemapFileDownloader.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/network/TrailMateHttpPmTilesBasemapFileDownloaderTest.kt`

- [x] **Step 1: Write failing auth and resume tests**

Add tests named:

```kotlin
@Test
fun sendsBearerTokenWhenProvided()

@Test
fun resumesExistingPartialFileWithRangeAndAppend()

@Test
fun overwritesPartialFileWhenServerIgnoresRange()

@Test
fun rejectsMismatchedPartialContentRangeWithoutCorruptingPartialFile()
```

The resume test should create a target file containing bytes `1, 2`, assert the request header is `Range: bytes=2-`, return `206` with bytes `3, 4`, and expect the final file to contain `1, 2, 3, 4`.
Also cover route-import coordination by pre-creating `<routePackKey>.pmtiles.download` and verifying the coordinator passes that partial file through to the downloader instead of deleting it first.

- [x] **Step 2: Verify Android downloader tests fail**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMateHttpPmTilesBasemapFileDownloaderTest" --no-daemon --console=plain
```

Expected: the new tests fail because the downloader does not accept an auth token and does not send `Range`.

- [x] **Step 3: Implement minimal Android downloader behavior**

Update the interface to:

```kotlin
fun downloadToFile(
    downloadUrl: String,
    targetFile: File,
    authorizationBearerToken: String? = null
): TrailMateApiResult<File>
```

Implementation details:
- set `Authorization: Bearer <token>` only when the token is non-blank;
- when `targetFile.length() > 0`, set `Range: bytes=<length>-`;
- append only for `206`;
- overwrite for `200`;
- preserve non-empty interrupted files for retry;
- keep deleting target files for non-retryable permanent client errors such as `404`.

- [x] **Step 4: Verify Android downloader tests pass**

Run the same targeted Android test command. Expected: pass.

### Task 2: Server Byte-Range Regression Coverage

**Files:**
- Modify: `trailmate-server/src/main/java/com/trailmate/server/map/OfflineBasemapDownloadController.java`
- Modify: `trailmate-server/src/test/java/com/trailmate/server/map/OfflineBasemapDownloadControllerTest.java`

- [x] **Step 1: Write server range regression tests**

Add tests named:

```java
@Test
void servesSatisfiablePmTilesByteRange()

@Test
void rejectsUnsatisfiablePmTilesByteRange()
```

The satisfiable test should write bytes `{1, 2, 3, 4}`, request `Range: bytes=2-`, expect status `206`, `Accept-Ranges: bytes`, `Content-Range: bytes 2-3/4`, `Content-Length: 2`, and body `{3, 4}`.

- [x] **Step 2: Run server range tests**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; .\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.map.OfflineBasemapDownloadControllerTest" --no-daemon --console=plain
```

Expected: pass if Spring MVC keeps serving `FileSystemResource` ranges correctly; if this fails, implement or restore byte-range response handling before proceeding.

- [x] **Step 3: Keep the controller implementation unchanged when the regression passes**

Do not replace Spring MVC's resource handling unless the regression proves it is missing. The current path-safety boundary remains `OfflineBasemapFileService`.

- [x] **Step 4: Verify server tests pass**

Run the same targeted server test command. Expected: pass.

### Task 3: Spec And Branch Verification

**Files:**
- Modify: `openspec/changes/trailmate-p0-pmtiles-resumable-download/tasks.md`

- [x] **Step 1: Mark OpenSpec tasks complete**

Set every implemented task checkbox in `openspec/changes/trailmate-p0-pmtiles-resumable-download/tasks.md` to `[x]`.

- [x] **Step 2: Run combined verification**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMateHttpPmTilesBasemapFileDownloaderTest" --tests "com.trailmate.app.core.network.TrailMatePmTilesBasemapRemoteImportCoordinatorTest" :trailmate-server:test --tests "com.trailmate.server.map.OfflineBasemapDownloadControllerTest" --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass with no whitespace errors.
