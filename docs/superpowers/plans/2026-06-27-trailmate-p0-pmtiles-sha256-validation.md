# TrailMate P0 PMTiles SHA-256 Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Verify remote PMTiles downloads against catalog SHA-256 metadata before importing them as offline basemap packs.

**Architecture:** Keep checksum validation inside `TrailMatePmTilesBasemapRemoteImportCoordinator`, after download and before archive-header validation. Use a streaming digest helper and preserve existing local-picker fallback behavior.

**Tech Stack:** Kotlin, JUnit4, Java MessageDigest, OpenSpec.

---

### Task 1: Android Coordinator Checksum Contract

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMatePmTilesBasemapRemoteImportCoordinator.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/network/TrailMatePmTilesBasemapRemoteImportCoordinatorTest.kt`

- [x] **Step 1: Write failing checksum tests**

Add tests named:

```kotlin
@Test
fun importsDownloadedPackWhenCatalogSha256Matches()

@Test
fun rejectsDownloadedPackWhenCatalogSha256DoesNotMatch()

@Test
fun keepsPreviewCompatibilityWhenCatalogSha256IsMissing()
```

Use `PmTilesArchiveHeaderParserTest.validPmTilesFile(...)` for valid PMTiles bytes. Compute the matching hash in the test with `MessageDigest.getInstance("SHA-256")`.

- [x] **Step 2: Verify checksum tests fail**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMatePmTilesBasemapRemoteImportCoordinatorTest" --no-daemon --console=plain
```

Expected: mismatch test fails because the coordinator imports valid PMTiles without checking `sha256`.

- [x] **Step 3: Implement streaming checksum validation**

In `TrailMatePmTilesBasemapRemoteImportCoordinator`:
- add a private `sha256Hex(file: File): String`;
- add a private `matchesExpectedSha256(file: File, expectedSha256: String?): Boolean`;
- before archive validation, if `selected.sha256` is non-blank and does not match, delete `temporaryFile` and return `openLocalPicker("服务端离线地图包完整性校验未通过，可选择本地 PMTiles 文件。")`.

- [x] **Step 4: Verify checksum tests pass**

Run the same targeted Android test command. Expected: pass.

### Task 2: Branch Verification

**Files:**
- Modify: `openspec/changes/trailmate-p0-pmtiles-sha256-validation/tasks.md`

- [x] **Step 1: Mark OpenSpec tasks complete**

Set implemented task checkboxes to `[x]`.

- [x] **Step 2: Run full verification**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
