# TrailMate P0 Off-Route Alert Policy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a deterministic mobile policy that turns existing route-check states into throttled, episode-based off-route alert decisions.

**Architecture:** Add a pure Kotlin model under `android-app/src/main/java/com/trailmate/app/core/model/` so Android notification, vibration, and UI surfaces can consume alert decisions without duplicating safety logic. The policy reuses `LocationBackedHikeStatus` and `HikeLocationFix`, stores only small episode state, and keeps all field copy Chinese and action-oriented.

**Tech Stack:** Kotlin, Android local unit tests with JUnit 4, OpenSpec.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertPolicy.kt`: pure policy, state, decision, and enums.
- Create `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertPolicyTest.kt`: focused tests for the policy contract.
- Create `openspec/changes/trailmate-p0-off-route-alert-policy/`: proposal, design, tasks, and light-navigation spec delta.

## Task 1: Write The Failing Policy Tests

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertPolicyTest.kt`

- [x] **Step 1: Add failing tests**

```kotlin
package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationAlertPolicyTest {
    @Test
    fun firstReliableOffRouteFixTriggersUrgentAlert() {
        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 112.0),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE, decision.kind)
        assertTrue(decision.shouldNotify)
        assertTrue(decision.shouldVibrate)
        assertEquals("疑似偏离路线", decision.title)
        assertEquals("请先停下，当前位置距计划路线约 112 m。核对地图、路标和现场路径后再继续。", decision.caption)
        assertEquals("停下核对路线", decision.primaryActionLabel)
        assertTrue(decision.nextState.activeEpisode)
        assertEquals(10_000L, decision.nextState.lastAlertEpochMillis)
        assertEquals(112.0, decision.nextState.lastAlertCrossTrackErrorMeters, 0.0)
    }

    @Test
    fun sameEpisodeInsideCooldownSuppressesRepeatedNotification() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 125.0),
            state = state,
            nowEpochMillis = 40_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE_SILENT, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals("偏离恢复中", decision.title)
        assertEquals("继续核对路线，暂不重复提醒。当前位置距计划路线约 125 m。", decision.caption)
        assertTrue(decision.nextState.activeEpisode)
        assertEquals(10_000L, decision.nextState.lastAlertEpochMillis)
        assertEquals(112.0, decision.nextState.lastAlertCrossTrackErrorMeters, 0.0)
    }

    @Test
    fun worseningDeviationInsideCooldownEscalatesAlert() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 172.0),
            state = state,
            nowEpochMillis = 40_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE_ESCALATED, decision.kind)
        assertTrue(decision.shouldNotify)
        assertTrue(decision.shouldVibrate)
        assertEquals("偏离距离增加", decision.title)
        assertEquals("你可能正在远离计划路线，当前偏离约 172 m。请停下确认是否需要原路返回。", decision.caption)
        assertEquals(40_000L, decision.nextState.lastAlertEpochMillis)
        assertEquals(172.0, decision.nextState.lastAlertCrossTrackErrorMeters, 0.0)
    }

    @Test
    fun rejoiningRouteClearsEpisodeOnce() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 18.0),
            state = state,
            nowEpochMillis = 90_000L
        )

        assertEquals(RouteDeviationAlertKind.REJOINED_ROUTE, decision.kind)
        assertTrue(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals("已回到路线", decision.title)
        assertEquals("当前位置已回到计划路线附近，请确认下一检查点后继续。", decision.caption)
        assertFalse(decision.nextState.activeEpisode)
        assertTrue(decision.nextState.rejoinNoticeEmitted)

        val repeated = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 12.0),
            state = decision.nextState,
            nowEpochMillis = 95_000L
        )
        assertEquals(RouteDeviationAlertKind.NONE, repeated.kind)
        assertFalse(repeated.shouldNotify)
    }

    @Test
    fun unreliableFixDoesNotTriggerOffRouteAlert() {
        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 112.0).copy(horizontalAccuracyMeters = 120.0),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals("等待定位稳定", decision.title)
        assertEquals("当前定位精度约 120 m，先到开阔处等待可靠定位，再判断是否偏离路线。", decision.caption)
        assertFalse(decision.nextState.activeEpisode)
    }

    private fun reliableFix(crossTrackErrorMeters: Double): HikeLocationFix =
        HikeLocationFix(
            distanceAlongRouteKm = 5.12,
            crossTrackErrorMeters = crossTrackErrorMeters,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = 1_000L
        )
}
```

- [x] **Step 2: Run the targeted test and verify RED**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertPolicyTest" --no-daemon
```

Expected: build fails because `RouteDeviationAlertPolicy`, `RouteDeviationAlertState`, and `RouteDeviationAlertKind` do not exist.

## Task 2: Implement The Minimal Policy

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertPolicy.kt`

- [x] **Step 1: Add the implementation**

```kotlin
package com.trailmate.app.core.model

enum class RouteDeviationAlertKind {
    NONE,
    WAIT_FOR_RELIABLE_FIX,
    OFF_ROUTE,
    OFF_ROUTE_SILENT,
    OFF_ROUTE_ESCALATED,
    REJOINED_ROUTE
}

data class RouteDeviationAlertState(
    val activeEpisode: Boolean = false,
    val lastAlertEpochMillis: Long? = null,
    val lastAlertCrossTrackErrorMeters: Double? = null,
    val rejoinNoticeEmitted: Boolean = true
)

data class RouteDeviationAlertDecision(
    val kind: RouteDeviationAlertKind,
    val shouldNotify: Boolean,
    val shouldVibrate: Boolean,
    val title: String,
    val caption: String,
    val primaryActionLabel: String,
    val nextState: RouteDeviationAlertState
)

object RouteDeviationAlertPolicy {
    private const val MAX_ALERT_ACCURACY_METERS = 50.0
    private const val COOLDOWN_MILLIS = 120_000L
    private const val ESCALATION_DELTA_METERS = 50.0

    fun evaluate(
        status: LocationBackedHikeStatus,
        fix: HikeLocationFix?,
        state: RouteDeviationAlertState,
        nowEpochMillis: Long
    ): RouteDeviationAlertDecision {
        if (status == LocationBackedHikeStatus.ON_ROUTE && state.activeEpisode) {
            return RouteDeviationAlertDecision(
                kind = RouteDeviationAlertKind.REJOINED_ROUTE,
                shouldNotify = true,
                shouldVibrate = false,
                title = "已回到路线",
                caption = "当前位置已回到计划路线附近，请确认下一检查点后继续。",
                primaryActionLabel = "继续导航",
                nextState = RouteDeviationAlertState(rejoinNoticeEmitted = true)
            )
        }

        if (status != LocationBackedHikeStatus.CHECK_ROUTE) {
            return none(state)
        }

        if (fix == null || fix.horizontalAccuracyMeters > MAX_ALERT_ACCURACY_METERS) {
            return RouteDeviationAlertDecision(
                kind = RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX,
                shouldNotify = false,
                shouldVibrate = false,
                title = "等待定位稳定",
                caption = fix?.let { "当前定位精度约 ${it.horizontalAccuracyMeters.toInt()} m，先到开阔处等待可靠定位，再判断是否偏离路线。" }
                    ?: "尚未获得可靠定位，先到开阔处等待 GPS 稳定，再判断是否偏离路线。",
                primaryActionLabel = "重新定位",
                nextState = state.copy(activeEpisode = false)
            )
        }

        val lastAlertAt = state.lastAlertEpochMillis
        val lastAlertDistance = state.lastAlertCrossTrackErrorMeters
        val inCooldown = lastAlertAt != null && nowEpochMillis - lastAlertAt < COOLDOWN_MILLIS
        val worsened = lastAlertDistance != null &&
            fix.crossTrackErrorMeters - lastAlertDistance >= ESCALATION_DELTA_METERS

        if (state.activeEpisode && inCooldown && !worsened) {
            return RouteDeviationAlertDecision(
                kind = RouteDeviationAlertKind.OFF_ROUTE_SILENT,
                shouldNotify = false,
                shouldVibrate = false,
                title = "偏离恢复中",
                caption = "继续核对路线，暂不重复提醒。当前位置距计划路线约 ${fix.crossTrackErrorMeters.toInt()} m。",
                primaryActionLabel = "查看恢复建议",
                nextState = state.copy(activeEpisode = true, rejoinNoticeEmitted = false)
            )
        }

        val escalated = state.activeEpisode && worsened
        return RouteDeviationAlertDecision(
            kind = if (escalated) RouteDeviationAlertKind.OFF_ROUTE_ESCALATED else RouteDeviationAlertKind.OFF_ROUTE,
            shouldNotify = true,
            shouldVibrate = true,
            title = if (escalated) "偏离距离增加" else "疑似偏离路线",
            caption = if (escalated) {
                "你可能正在远离计划路线，当前偏离约 ${fix.crossTrackErrorMeters.toInt()} m。请停下确认是否需要原路返回。"
            } else {
                "请先停下，当前位置距计划路线约 ${fix.crossTrackErrorMeters.toInt()} m。核对地图、路标和现场路径后再继续。"
            },
            primaryActionLabel = "停下核对路线",
            nextState = RouteDeviationAlertState(
                activeEpisode = true,
                lastAlertEpochMillis = nowEpochMillis,
                lastAlertCrossTrackErrorMeters = fix.crossTrackErrorMeters,
                rejoinNoticeEmitted = false
            )
        )
    }

    private fun none(state: RouteDeviationAlertState): RouteDeviationAlertDecision =
        RouteDeviationAlertDecision(
            kind = RouteDeviationAlertKind.NONE,
            shouldNotify = false,
            shouldVibrate = false,
            title = "",
            caption = "",
            primaryActionLabel = "",
            nextState = state
        )
}
```

- [x] **Step 2: Run the targeted test and verify GREEN**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertPolicyTest" --no-daemon
```

Expected: `BUILD SUCCESSFUL`.

## Task 3: Validate And Commit

**Files:**
- Modify: `docs/superpowers/plans/2026-06-25-trailmate-p0-off-route-alert-policy.md`
- Modify: `openspec/changes/trailmate-p0-off-route-alert-policy/*`
- Create: policy and test files from Tasks 1 and 2

- [x] **Step 1: Run full verification**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.

- [x] **Step 2: Commit**

```powershell
git add android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertPolicy.kt android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertPolicyTest.kt docs/superpowers/plans/2026-06-25-trailmate-p0-off-route-alert-policy.md openspec/changes/trailmate-p0-off-route-alert-policy
git commit -m "feat: add off-route alert policy"
```
