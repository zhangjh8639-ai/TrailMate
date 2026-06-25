# TrailMate P0 Off-Route Alert Delivery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver new off-route alert decisions through Android notification/vibration hooks while keeping the policy and delivery decisions deterministic and testable.

**Architecture:** Add a pure delivery model that consumes `RouteDeviationAlertDecision` and runtime capabilities, then a narrow Android adapter for notification/vibration APIs. `RouteDetailScreen` invokes delivery only from the GPS snapshot decision path, never from Compose rendering.

**Tech Stack:** Kotlin, Android framework `NotificationManager`/`Vibrator`, Jetpack Compose integration, JUnit 4, OpenSpec.

---

## File Structure

- Create `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryEngineTest.kt`: pure delivery behavior tests.
- Create `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryEngine.kt`: pure delivery plan model.
- Create `android-app/src/main/java/com/trailmate/app/core/location/RouteDeviationAlertAndroidDelivery.kt`: Android notification/vibration adapter.
- Modify `android-app/src/main/AndroidManifest.xml`: add `android.permission.VIBRATE`.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`: call Android delivery after a new GPS-derived alert decision is evaluated.
- Modify `openspec/changes/trailmate-p0-off-route-alert-delivery/*`: track delivery requirements.

## Task 1: Pure Delivery Model

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryEngine.kt`

- [x] **Step 1: Write failing delivery tests**

```kotlin
package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationAlertDeliveryEngineTest {
    @Test
    fun urgentOffRouteDecisionPostsAndVibratesWhenAllowed() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE,
                shouldNotify = true,
                shouldVibrate = true,
                title = "疑似偏离路线",
                caption = "请先停下，当前位置距计划路线约 112 m。核对地图、路标和现场路径后再继续。"
            ),
            notificationPermissionGranted = true,
            deviceCanVibrate = true
        )

        assertTrue(plan.shouldPostNotification)
        assertTrue(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.URGENT, plan.tone)
        assertEquals("TrailMate 偏航提醒", plan.notificationTitle)
        assertTrue(plan.notificationText.contains("停下"))
        assertFalse(plan.notificationText.contains("救援"))
        assertEquals(null, plan.inAppOnlyReason)
    }

    @Test
    fun silentSameEpisodeDecisionDoesNotInterruptAgain() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE_SILENT,
                shouldNotify = false,
                shouldVibrate = false,
                title = "偏离恢复中",
                caption = "继续核对路线，暂不重复提醒。当前位置距计划路线约 125 m。"
            ),
            notificationPermissionGranted = true,
            deviceCanVibrate = true
        )

        assertFalse(plan.shouldPostNotification)
        assertFalse(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.NONE, plan.tone)
    }

    @Test
    fun missingNotificationPermissionUsesInAppFallbackAndVibration() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE_ESCALATED,
                shouldNotify = true,
                shouldVibrate = true,
                title = "偏离距离增加",
                caption = "你可能正在远离计划路线，当前偏离约 172 m。请停下确认是否需要原路返回。"
            ),
            notificationPermissionGranted = false,
            deviceCanVibrate = true
        )

        assertFalse(plan.shouldPostNotification)
        assertTrue(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.URGENT, plan.tone)
        assertEquals("通知权限未开启，TrailMate 只能在路线页内显示偏航提醒。", plan.inAppOnlyReason)
    }

    @Test
    fun rejoinedDecisionCanNotifyWithoutVibration() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.REJOINED_ROUTE,
                shouldNotify = true,
                shouldVibrate = false,
                title = "已回到路线",
                caption = "当前位置已回到计划路线附近，请确认下一检查点后继续。"
            ),
            notificationPermissionGranted = true,
            deviceCanVibrate = true
        )

        assertTrue(plan.shouldPostNotification)
        assertFalse(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.REJOINED, plan.tone)
        assertEquals("TrailMate 路线提醒", plan.notificationTitle)
    }

    private fun decision(
        kind: RouteDeviationAlertKind,
        shouldNotify: Boolean,
        shouldVibrate: Boolean,
        title: String,
        caption: String
    ) = RouteDeviationAlertDecision(
        kind = kind,
        shouldNotify = shouldNotify,
        shouldVibrate = shouldVibrate,
        title = title,
        caption = caption,
        primaryActionLabel = "查看路线",
        nextState = RouteDeviationAlertState()
    )
}
```

- [x] **Step 2: Run RED**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertDeliveryEngineTest" --no-daemon
```

Expected: failure because `RouteDeviationAlertDeliveryEngine` does not exist.

- [x] **Step 3: Implement delivery model**

```kotlin
package com.trailmate.app.core.model

enum class RouteDeviationAlertDeliveryTone {
    NONE,
    URGENT,
    REJOINED
}

data class RouteDeviationAlertDeliveryPlan(
    val shouldPostNotification: Boolean,
    val shouldVibrate: Boolean,
    val tone: RouteDeviationAlertDeliveryTone,
    val notificationTitle: String,
    val notificationText: String,
    val inAppOnlyReason: String?
)

object RouteDeviationAlertDeliveryEngine {
    fun resolve(
        decision: RouteDeviationAlertDecision,
        notificationPermissionGranted: Boolean,
        deviceCanVibrate: Boolean
    ): RouteDeviationAlertDeliveryPlan {
        if (!decision.shouldNotify && !decision.shouldVibrate) {
            return none()
        }

        val tone = when (decision.kind) {
            RouteDeviationAlertKind.OFF_ROUTE,
            RouteDeviationAlertKind.OFF_ROUTE_ESCALATED -> RouteDeviationAlertDeliveryTone.URGENT
            RouteDeviationAlertKind.REJOINED_ROUTE -> RouteDeviationAlertDeliveryTone.REJOINED
            RouteDeviationAlertKind.NONE,
            RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX,
            RouteDeviationAlertKind.OFF_ROUTE_SILENT -> RouteDeviationAlertDeliveryTone.NONE
        }
        if (tone == RouteDeviationAlertDeliveryTone.NONE) {
            return none()
        }

        return RouteDeviationAlertDeliveryPlan(
            shouldPostNotification = decision.shouldNotify && notificationPermissionGranted,
            shouldVibrate = decision.shouldVibrate && deviceCanVibrate,
            tone = tone,
            notificationTitle = when (tone) {
                RouteDeviationAlertDeliveryTone.URGENT -> "TrailMate 偏航提醒"
                RouteDeviationAlertDeliveryTone.REJOINED -> "TrailMate 路线提醒"
                RouteDeviationAlertDeliveryTone.NONE -> ""
            },
            notificationText = "${decision.title}：${decision.caption}",
            inAppOnlyReason = if (decision.shouldNotify && !notificationPermissionGranted) {
                "通知权限未开启，TrailMate 只能在路线页内显示偏航提醒。"
            } else {
                null
            }
        )
    }

    private fun none(): RouteDeviationAlertDeliveryPlan =
        RouteDeviationAlertDeliveryPlan(
            shouldPostNotification = false,
            shouldVibrate = false,
            tone = RouteDeviationAlertDeliveryTone.NONE,
            notificationTitle = "",
            notificationText = "",
            inAppOnlyReason = null
        )
}
```

- [x] **Step 4: Run GREEN**

Run the same targeted test. Expected: `BUILD SUCCESSFUL`.

## Task 2: Android Delivery Adapter And Route Integration

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/location/RouteDeviationAlertAndroidDelivery.kt`
- Modify: `android-app/src/main/AndroidManifest.xml`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Add Android adapter**

Create an object that resolves the delivery plan, creates a high-importance `trailmate_route_alerts` channel for urgent off-route alerts and a default-importance `trailmate_route_status` channel for rejoined confirmations, posts a notification only when allowed, and vibrates only when requested and available. Use `Notification.Builder`, `NotificationManager`, `VibratorManager` on API 31+, and legacy `Vibrator` below API 31.

- [x] **Step 2: Add manifest permission**

Add:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

- [x] **Step 3: Connect from GPS decision path**

In `RouteDetailScreen.kt`, after `RouteDeviationAlertPolicy.evaluate(...)`, call `RouteDeviationAlertAndroidDelivery.deliver(...)` before storing display state. Do not call delivery from `GpsTrackPanel` or `RouteDeviationAlertBanner`.

- [x] **Step 4: Run focused compile/test**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertDeliveryEngineTest" --no-daemon
```

Expected: `BUILD SUCCESSFUL`.

## Task 3: Verification And Review

**Files:**
- All files above
- `openspec/changes/trailmate-p0-off-route-alert-delivery/*`
- This plan file

- [x] **Step 1: Run full verification**

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon
openspec validate --all --strict
git diff --check
```

- [x] **Step 2: Request reviews**

Ask one subagent to review OpenSpec/spec compliance and another to review code quality. Fix Critical or Important findings before commit.

- [x] **Step 3: Commit**

```powershell
git add android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryEngine.kt android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertDeliveryEngineTest.kt android-app/src/main/java/com/trailmate/app/core/location/RouteDeviationAlertAndroidDelivery.kt android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt android-app/src/main/AndroidManifest.xml docs/superpowers/plans/2026-06-25-trailmate-p0-off-route-alert-delivery.md openspec/changes/trailmate-p0-off-route-alert-delivery
git commit -m "feat: deliver off-route alerts on Android"
```
