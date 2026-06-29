# P0 Field Fix Accuracy Validity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure TrailMate never treats malformed horizontal accuracy as reliable field-location evidence.

**Architecture:** Harden the shared `TrailMateLocationFixReliability.isReliableForFieldUse` predicate so every caller benefits without duplicating checks. Keep freshness and status checks unchanged, and add focused unit tests around the shared policy.

**Tech Stack:** Kotlin, Android unit tests, OpenSpec.

---

### Task 1: Add Accuracy Validity Tests

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/location/TrailMateLocationFixReliabilityTest.kt`

- [x] **Step 1: Write the failing tests**

Add coverage for malformed snapshot accuracy and invalid caller thresholds:

```kotlin
@Test
fun malformedAccuracyIsNotReliableForFieldUse() {
    listOf(null, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { accuracy ->
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(
                    horizontalAccuracyMeters = accuracy
                ),
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
    }
}

@Test
fun overThresholdAccuracyIsNotReliableForFieldUse() {
    assertFalse(
        TrailMateLocationFixReliability.isReliableForFieldUse(
            snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(
                horizontalAccuracyMeters = 50.1
            ),
            nowEpochMillis = NOW_EPOCH_MILLIS,
            maxAccuracyMeters = 50.0
        )
    )
}

@Test
fun invalidMaxAccuracyThresholdIsNotReliableForFieldUse() {
    listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { threshold ->
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS),
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = threshold
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

Expected: the malformed accuracy test fails because negative accuracy is currently treated as reliable.

### Task 2: Harden Reliability Predicate

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/location/TrailMateLocationFixReliability.kt`

- [x] **Step 1: Write minimal implementation**

Replace the direct `<= maxAccuracyMeters` check with explicit finite and non-negative validation:

```kotlin
val accuracyMeters = snapshot.horizontalAccuracyMeters
return snapshot.status == TrailMateLocationStatus.LOCATED &&
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
openspec validate trailmate-p0-field-fix-accuracy-validity --strict
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
