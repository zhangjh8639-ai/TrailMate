# TrailMate P1 Safety Share Fix Age Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent TrailMate safety sharing from sending stale or timestamp-less GPS positions as current field locations.

**Architecture:** Keep the validation in `SafetyShareEngine` so UI callers cannot bypass it. Add a nullable timestamp to `SafetyShareLocation`, defaulting to `null` for compatibility, and pass `TrailMateLocationSnapshot.timestampEpochMillis` from route UI call sites.

**Tech Stack:** Kotlin, Android Compose call sites, OpenSpec, JUnit.

---

### Task 1: Safety Share Location Freshness

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/SafetyShareEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/SafetyShareEngine.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Create: `openspec/changes/trailmate-p1-safety-share-fix-age/proposal.md`
- Create: `openspec/changes/trailmate-p1-safety-share-fix-age/specs/light-navigation/spec.md`
- Create: `openspec/changes/trailmate-p1-safety-share-fix-age/tasks.md`

- [x] **Step 1: Write failing timestamp freshness tests**

Add tests proving:
- a location timestamp within 2 minutes generates share text;
- a timestamp older than 2 minutes blocks sharing and asks for refreshed GPS;
- missing timestamp blocks sharing;
- zero/negative/future timestamps block sharing.
- a location that was fresh when displayed but stale at click time does not send the precomputed share text.

- [x] **Step 2: Run targeted tests to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.SafetyShareEngineTest" --no-daemon --console=plain
```

Expected: compile failure or assertion failure because safety-share timestamps are not enforced yet.

- [x] **Step 3: Implement minimal freshness check**

Extend `SafetyShareLocation`:

```kotlin
val timestampEpochMillis: Long? = null
```

Before generating share text, reject missing, non-positive, future, or older-than-2-minute timestamps with no `shareText`.

- [x] **Step 4: Wire route UI call sites**

Pass `locationSnapshot.timestampEpochMillis` into both safety-share call paths in `RouteDetailScreen.kt`, and make share button actions re-evaluate safety-share freshness with the current click time before sending text.

- [x] **Step 5: Verify GREEN**

Run the targeted test command again. Expected: pass.

- [x] **Step 6: Full verification**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
