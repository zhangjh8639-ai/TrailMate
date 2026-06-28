# TrailMate P0 Field Battery Status Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add live phone battery context to the route cockpit field status so low battery is visible during outdoor navigation and recording.

**Architecture:** Keep the safety decision logic in pure Kotlin by extending `RouteFieldStatusEngine` with a small `RouteBatteryStatus` value. Android UI reads the sticky battery broadcast and passes the value into the existing route field status summary without adding a new screen.

**Tech Stack:** Kotlin, Android Compose, OpenSpec, JUnit.

---

### Task 1: Route Field Battery Status

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteFieldStatusEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteFieldStatusEngine.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Create: `openspec/changes/trailmate-p0-field-battery-status/proposal.md`
- Create: `openspec/changes/trailmate-p0-field-battery-status/specs/light-navigation/spec.md`
- Create: `openspec/changes/trailmate-p0-field-battery-status/tasks.md`

- [x] **Step 1: Write failing battery status tests**

Add tests proving:
- normal battery shows `电量` as `68%`;
- low battery below 30% shows `偏低 24%` and conservative caption;
- critical battery at or below 15% shows `危险 12%` and stronger caption;
- invalid percentages show `未知`;
- unknown battery shows `未知`;
- existing GPS/track/map/notification items still work.

- [x] **Step 2: Run targeted tests to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; $env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteFieldStatusEngineTest" --no-daemon --console=plain
```

Expected: compile failure or assertion failure because `RouteBatteryStatus` is not implemented yet.

- [x] **Step 3: Implement minimal battery field logic**

Add `RouteBatteryStatus` with `fromPercent(percent: Int?)`.

Extend `RouteFieldStatusEngine.build(...)` with:

```kotlin
batteryStatus: RouteBatteryStatus = RouteBatteryStatus.UNKNOWN
```

Append a `电量` item and override the caption only when battery is low or critical.

- [x] **Step 4: Wire Android battery reading**

In `RouteDetailScreen`, read `Intent.ACTION_BATTERY_CHANGED`, convert `BatteryManager.EXTRA_LEVEL` and `BatteryManager.EXTRA_SCALE` into a percent, and pass `RouteBatteryStatus.fromPercent(percent)` to `RouteFieldStatusEngine.build`.

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
