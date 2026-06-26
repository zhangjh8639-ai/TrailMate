# TrailMate P0 Off-Route Recovery Actions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn TrailMate's off-route recovery card into a concise field action panel that tells the user what to do next after a reliable deviation alert.

**Architecture:** Extend the existing pure `RouteDeviationRecoveryEngine` presentation model with deterministic recovery actions. Keep the action copy grounded in data the app actually has today: route projection, deviation distance, location accuracy, recent rejoin state, and whether safety sharing has a usable location. Do not claim nearest roads, exits, rerouting, or rescue because those data sources are not available yet.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4, existing TrailMate route-deviation and safety-share models, OpenSpec.

---

## File Structure

- Modify `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationRecoveryEngineTest.kt`: add failing assertions for recovery actions and off-route distance details.
- Modify `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationRecoveryEngine.kt`: add `RouteDeviationRecoveryAction` model, action kinds, and deterministic action lists.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`: render recovery actions as compact field action rows inside the existing recovery panel.
- Add `openspec/changes/trailmate-p0-off-route-recovery-actions/*`: OpenSpec proposal, design, task checklist, and light-navigation requirement delta.

## Task 1: Recovery Action Model

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationRecoveryEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationRecoveryEngine.kt`

- [x] **Step 1: Write failing tests**

Add assertions that an off-route recovery presentation includes these actions in order:

```kotlin
listOf(
    RouteDeviationRecoveryAction(
        kind = RouteDeviationRecoveryActionKind.STOP_AND_CONFIRM,
        label = "停下核对",
        value = "暂停前进，确认地图、路标和现场路径。",
        emphasized = true
    ),
    RouteDeviationRecoveryAction(
        kind = RouteDeviationRecoveryActionKind.RETURN_TO_ROUTE,
        label = "回到最近路线",
        value = "计划路线在约 112 m 外，沿安全可见路径返回。",
        emphasized = false
    ),
    RouteDeviationRecoveryAction(
        kind = RouteDeviationRecoveryActionKind.SHARE_LOCATION,
        label = "分享当前位置",
        value = "发送坐标、路线与预计完成时间。",
        emphasized = false
    )
)
```

Also assert that off-route details include `偏离距离` before route progress and accuracy.

- [x] **Step 2: Run RED**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationRecoveryEngineTest" --no-daemon --console=plain
```

Expected: compile failure because `RouteDeviationRecoveryAction` and `RouteDeviationRecoveryActionKind` do not exist.

- [x] **Step 3: Implement minimal action model**

Add `RouteDeviationRecoveryActionKind`, `RouteDeviationRecoveryAction`, and an `actions` property on `RouteDeviationRecoveryPresentation`. Build off-route, low-accuracy, rejoined, and hidden action lists directly in `RouteDeviationRecoveryEngine.present(...)`.

- [x] **Step 4: Run GREEN**

Run the same targeted test. Expected: `BUILD SUCCESSFUL`.

## Task 2: Recovery Panel UI

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Render action rows**

Import `RouteDeviationRecoveryAction` and add a `RouteDeviationRecoveryActionList` composable below the details row. Each action row shows a small icon, label, and value; emphasized action uses the recovery tone color while secondary actions remain neutral.

- [x] **Step 2: Preserve the existing primary action**

Keep the existing bottom `OutlinedButton` wired to `onPrimaryAction`; this preserves share-location or continue-navigation behavior without adding unimplemented reroute/exit buttons.

- [x] **Step 3: Compile with focused tests**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationRecoveryEngineTest" --no-daemon --console=plain
```

Expected: `BUILD SUCCESSFUL`.

## Task 3: OpenSpec And Device Verification

**Files:**
- `openspec/changes/trailmate-p0-off-route-recovery-actions/*`
- Modified Android files above

- [x] **Step 1: Validate OpenSpec**

Run:

```powershell
openspec validate --all --strict
```

Expected: all changes pass strict validation.

- [x] **Step 2: Run Android verification**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --no-daemon --console=plain
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 3: Install on connected SM_S9260**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:installDebug --no-daemon --console=plain
```

Expected: debug build installs on the connected phone. Launch the app and confirm route screen opens without a crash.

- [x] **Step 4: Request review before PR**

Use subagents for code-quality and product/spec review. Fix Critical or Important findings before commit and PR.
