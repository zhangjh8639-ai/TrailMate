# WeChat Safety Share Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prefer WeChat for manual safety/departure/emergency text sharing while preserving system share fallback when WeChat is unavailable.

**Architecture:** Add a small pure sharing policy that decides whether WeChat can be the preferred channel, then add an Android WeChat text launcher that uses the already-linked OpenSDK. Route share handlers try WeChat first for text-only safety messages and fall back to the existing Android chooser; no automatic sending, contact storage, or realtime tracking is introduced.

**Tech Stack:** Kotlin unit tests, Android WeChat OpenSDK, Jetpack Compose route screen handlers, OpenSpec change `trailmate-p1-wechat-safety-share`.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/share/TrailMateWechatTextShare.kt`
  - Owns pure readiness policy plus Android launcher result types.
- Create `android-app/src/test/java/com/trailmate/app/core/share/TrailMateWechatTextSharePolicyTest.kt`
  - Proves ready, not configured, not installed, failed send, and blank text fallback behavior.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Route text shares try WeChat first and fall back to existing chooser.
- Add OpenSpec files under `openspec/changes/trailmate-p1-wechat-safety-share/`.

## Task 1: WeChat Text Share Policy

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/share/TrailMateWechatTextSharePolicyTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/share/TrailMateWechatTextShare.kt`

- [x] **Step 1: Write failing tests**

```kotlin
@Test
fun readyWechatUsesWechatAsPreferredChannel() {
    val decision = TrailMateWechatTextSharePolicy.resolve(
        appIdConfigured = true,
        wechatInstalled = true,
        text = "TrailMate 安全分享"
    )

    assertEquals(TrailMateTextShareChannel.WECHAT, decision.preferredChannel)
    assertEquals("发送到微信", decision.primaryActionLabel)
    assertFalse(decision.requiresSystemFallback)
}
```

- [x] **Step 2: Run test to verify RED**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.share.TrailMateWechatTextSharePolicyTest" --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
```

Expected: FAIL because the policy does not exist.

- [x] **Step 3: Implement minimal policy**

Rules:
- Non-blank text + configured AppID + installed WeChat => preferred channel `WECHAT`.
- Blank text, missing AppID, or missing WeChat => preferred channel `SYSTEM`.
- Failed WeChat send => fallback to system chooser.
- Copy must state this is manual sharing only, not automatic contact or realtime tracking.

- [x] **Step 4: Verify GREEN**

Run the targeted test. Expected: PASS.

## Task 2: Android WeChat Launcher and Route Share Hook

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/share/TrailMateWechatTextShare.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Add Android launcher**

Use `WXAPIFactory.createWXAPI()`, `WXTextObject`, `WXMediaMessage`, and `SendMessageToWX.Req` with `WXSceneSession`.

- [x] **Step 2: Hook route share actions**

Update `shareTrailMateText()` so route safety, departure brief, and emergency text tries WeChat first when configured/installed. If WeChat send is unavailable or fails, use the existing `ACTION_SEND` chooser.

- [x] **Step 3: Verify compile**

Run targeted Android unit tests. Expected: PASS and debug Kotlin compiles.

## Task 3: Final Gates

- [x] **Step 1: Mark OpenSpec tasks complete**
- [x] **Step 2: Run validation**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:PATH="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
.\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain --max-workers=1 "-Dkotlin.compiler.execution.strategy=in-process" "-Dkotlin.incremental=false"
openspec validate --all --strict
git diff --check
```

- [x] **Step 3: Request focused read-only code review**
- [x] **Step 4: Commit, push, and create a draft stacked PR**
