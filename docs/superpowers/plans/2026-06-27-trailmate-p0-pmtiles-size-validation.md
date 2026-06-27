# TrailMate P0 PMTiles Size Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Android reject remote PMTiles downloads whose length does not match positive catalog `sizeBytes` metadata.

**Architecture:** Keep validation inside `TrailMatePmTilesBasemapRemoteImportCoordinator`, immediately after download and before SHA-256/archive validation. Treat missing `sizeBytes` as preview-compatible, but require positive values to match the temporary file length.

**Tech Stack:** Kotlin, Android JVM unit tests, OpenSpec.

---

### Task 1: Coordinator Size Validation

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/network/TrailMatePmTilesBasemapRemoteImportCoordinatorTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMatePmTilesBasemapRemoteImportCoordinator.kt`

- [x] **Step 1: Write failing tests**

Add tests that:

- import when `catalogItem(sizeBytes = sourceFile.length())`;
- fall back and delete `longjing-ridge.pmtiles.download` when `catalogItem(sizeBytes = sourceFile.length() + 1)`;
- preserve existing import behavior when `catalogItem(sizeBytes = null)`.

- [x] **Step 2: Run targeted test to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMatePmTilesBasemapRemoteImportCoordinatorTest" --no-daemon --console=plain
```

Expected: the mismatch test fails because downloaded size is not yet checked.

- [x] **Step 3: Implement minimal size validation**

Add a private helper near SHA-256 validation:

```kotlin
private fun File.matchesExpectedSize(expectedSizeBytes: Long?): Boolean {
    val expected = expectedSizeBytes?.takeIf { it > 0L } ?: return true
    return length() == expected
}
```

Call it after download succeeds and before SHA-256 validation. On mismatch, delete the temporary file and return the local-picker fallback message.

- [x] **Step 4: Verify GREEN**

Run the targeted coordinator test again. Expected: pass.

- [x] **Step 5: Full verification**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
