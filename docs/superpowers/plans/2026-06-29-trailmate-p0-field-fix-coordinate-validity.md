# P0 Field Fix Coordinate Validity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure TrailMate never treats missing or non-finite coordinates as reliable field-location evidence.

**Architecture:** Harden the shared `TrailMateLocationFixReliability.isReliableForFieldUse` predicate so all field-critical callers inherit the coordinate guard. Keep the existing timestamp and accuracy checks intact.

**Tech Stack:** Kotlin, Android unit tests, OpenSpec.

---

### Task 1: Add Coordinate Validity Tests

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/location/TrailMateLocationFixReliabilityTest.kt`

- [x] **Step 1: Write the failing tests**

Add tests for null, NaN, and infinite latitude/longitude:

```kotlin
@Test
fun missingCoordinatesAreNotReliableForFieldUse() {
    listOf(
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(latitude = null),
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(longitude = null)
    ).forEach { snapshot ->
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
    }
}

@Test
fun nonFiniteCoordinatesAreNotReliableForFieldUse() {
    listOf(
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(latitude = Double.NaN),
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(latitude = Double.POSITIVE_INFINITY),
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(latitude = Double.NEGATIVE_INFINITY),
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(longitude = Double.NaN),
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(longitude = Double.POSITIVE_INFINITY),
        reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(longitude = Double.NEGATIVE_INFINITY)
    ).forEach { snapshot ->
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
    }
}
```

- [x] **Step 2: Run target test to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests com.trailmate.app.core.location.TrailMateLocationFixReliabilityTest --no-daemon
```

Expected: the new coordinate tests fail because the current reliability predicate does not inspect latitude/longitude.

### Task 2: Harden Reliability Predicate

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/location/TrailMateLocationFixReliability.kt`

- [x] **Step 1: Write minimal implementation**

Add latitude and longitude checks before returning reliable:

```kotlin
val latitude = snapshot.latitude
val longitude = snapshot.longitude
return snapshot.status == TrailMateLocationStatus.LOCATED &&
    latitude != null &&
    latitude.isFinite() &&
    longitude != null &&
    longitude.isFinite() &&
    accuracyMeters != null &&
    accuracyMeters.isFinite() &&
    accuracyMeters >= 0.0 &&
    maxAccuracyMeters.isFinite() &&
    maxAccuracyMeters >= 0.0 &&
    accuracyMeters <= maxAccuracyMeters &&
    isFresh(snapshot = snapshot, nowEpochMillis = nowEpochMillis)
```

- [x] **Step 2: Run target test to verify GREEN**

Run the same targeted Gradle command. Expected: all `TrailMateLocationFixReliabilityTest` tests pass.

### Task 3: Verify and Review

**Files:**
- Validate all changed files.

- [x] **Step 1: Validate OpenSpec**

```powershell
openspec validate trailmate-p0-field-fix-coordinate-validity --strict
```

Expected: strict validation succeeds.

- [x] **Step 2: Run full tests**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test --no-daemon
```

Expected: full test suite passes.

- [x] **Step 3: Request code review**

Ask subagents to review spec compliance and code quality before opening the PR.
