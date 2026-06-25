# TrailMate P0 Off-Route Alert Surface Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Surface the latest off-route alert decision in the route GPS panel so field users see a direct alert before detailed recovery guidance.

**Architecture:** Keep the policy pure and add a second pure presentation model that maps `RouteDeviationAlertDecision` into compact UI copy and tone. RouteDetailScreen will store alert episode state and latest decision, update it only when GPS route-check updates arrive, and render a compact alert card before the existing recovery panel.

**Tech Stack:** Kotlin, Jetpack Compose, Android local unit tests with JUnit 4, OpenSpec.

---

## File Structure

- Create `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertPresentationEngine.kt`: maps alert decisions to compact UI presentation.
- Create `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertPresentationEngineTest.kt`: tests visibility, tone, copy, and acknowledgement labels.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`: maintain latest alert state and render a compact route alert card in `GpsTrackPanel`.
- Modify `openspec/changes/trailmate-p0-off-route-alert-policy/*`: document route-panel surface scope.

## Task 1: Presentation Model

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertPresentationEngineTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertPresentationEngine.kt`

- [x] **Step 1: Write failing presentation tests**

```kotlin
package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationAlertPresentationEngineTest {
    @Test
    fun urgentOffRouteDecisionShowsStopAndCheckAlert() {
        val presentation = RouteDeviationAlertPresentationEngine.present(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE,
                shouldNotify = true,
                shouldVibrate = true,
                title = "疑似偏离路线",
                caption = "请先停下，当前位置距计划路线约 112 m。核对地图、路标和现场路径后再继续。",
                primaryActionLabel = "停下核对路线"
            )
        )

        assertTrue(presentation.visible)
        assertEquals(RouteDeviationAlertTone.URGENT, presentation.tone)
        assertEquals("疑似偏离路线", presentation.title)
        assertEquals("停下核对路线", presentation.primaryActionLabel)
        assertTrue(presentation.shouldRequestAttention)
        assertChinese(presentation)
    }

    @Test
    fun silentSameEpisodeDecisionShowsLowerPriorityRecoveryStatus() {
        val presentation = RouteDeviationAlertPresentationEngine.present(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE_SILENT,
                shouldNotify = false,
                shouldVibrate = false,
                title = "偏离恢复中",
                caption = "继续核对路线，暂不重复提醒。当前位置距计划路线约 125 m。",
                primaryActionLabel = "查看恢复建议"
            )
        )

        assertTrue(presentation.visible)
        assertEquals(RouteDeviationAlertTone.CAUTION, presentation.tone)
        assertFalse(presentation.shouldRequestAttention)
        assertEquals("查看恢复建议", presentation.primaryActionLabel)
        assertChinese(presentation)
    }

    @Test
    fun rejoinedDecisionShowsContinueAction() {
        val presentation = RouteDeviationAlertPresentationEngine.present(
            decision = decision(
                kind = RouteDeviationAlertKind.REJOINED_ROUTE,
                shouldNotify = true,
                shouldVibrate = false,
                title = "已回到路线",
                caption = "当前位置已回到计划路线附近，请确认下一检查点后继续。",
                primaryActionLabel = "继续导航"
            )
        )

        assertTrue(presentation.visible)
        assertEquals(RouteDeviationAlertTone.REJOINED, presentation.tone)
        assertEquals("继续导航", presentation.primaryActionLabel)
        assertFalse(presentation.shouldRequestAttention)
        assertChinese(presentation)
    }

    @Test
    fun noneDecisionIsHidden() {
        val presentation = RouteDeviationAlertPresentationEngine.present(
            decision = decision(kind = RouteDeviationAlertKind.NONE)
        )

        assertFalse(presentation.visible)
    }

    private fun decision(
        kind: RouteDeviationAlertKind,
        shouldNotify: Boolean = false,
        shouldVibrate: Boolean = false,
        title: String = "",
        caption: String = "",
        primaryActionLabel: String = ""
    ) = RouteDeviationAlertDecision(
        kind = kind,
        shouldNotify = shouldNotify,
        shouldVibrate = shouldVibrate,
        title = title,
        caption = caption,
        primaryActionLabel = primaryActionLabel,
        nextState = RouteDeviationAlertState()
    )

    private fun assertChinese(presentation: RouteDeviationAlertPresentation) {
        val text = presentation.title + presentation.caption + presentation.primaryActionLabel
        assertTrue(text.any { it in '\u4e00'..'\u9fff' })
        assertFalse(text.contains("reroute", ignoreCase = true))
        assertFalse(text.contains("rescue", ignoreCase = true))
    }
}
```

- [x] **Step 2: Run RED**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertPresentationEngineTest" --no-daemon
```

Expected: failure because `RouteDeviationAlertPresentationEngine` does not exist.

- [x] **Step 3: Implement presentation model**

```kotlin
package com.trailmate.app.core.model

enum class RouteDeviationAlertTone {
    URGENT,
    CAUTION,
    REJOINED
}

data class RouteDeviationAlertPresentation(
    val visible: Boolean,
    val tone: RouteDeviationAlertTone,
    val title: String,
    val caption: String,
    val primaryActionLabel: String,
    val shouldRequestAttention: Boolean
)

object RouteDeviationAlertPresentationEngine {
    fun present(decision: RouteDeviationAlertDecision?): RouteDeviationAlertPresentation {
        if (decision == null || decision.kind == RouteDeviationAlertKind.NONE) {
            return hidden()
        }

        val tone = when (decision.kind) {
            RouteDeviationAlertKind.OFF_ROUTE,
            RouteDeviationAlertKind.OFF_ROUTE_ESCALATED -> RouteDeviationAlertTone.URGENT
            RouteDeviationAlertKind.REJOINED_ROUTE -> RouteDeviationAlertTone.REJOINED
            RouteDeviationAlertKind.OFF_ROUTE_SILENT,
            RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX -> RouteDeviationAlertTone.CAUTION
            RouteDeviationAlertKind.NONE -> RouteDeviationAlertTone.CAUTION
        }

        return RouteDeviationAlertPresentation(
            visible = true,
            tone = tone,
            title = decision.title,
            caption = decision.caption,
            primaryActionLabel = decision.primaryActionLabel,
            shouldRequestAttention = decision.shouldNotify || decision.shouldVibrate
        )
    }

    private fun hidden(): RouteDeviationAlertPresentation =
        RouteDeviationAlertPresentation(
            visible = false,
            tone = RouteDeviationAlertTone.CAUTION,
            title = "",
            caption = "",
            primaryActionLabel = "",
            shouldRequestAttention = false
        )
}
```

- [x] **Step 4: Run GREEN**

Run the same targeted test. Expected: `BUILD SUCCESSFUL`.

## Task 2: Route GPS Panel Integration

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [x] **Step 1: Add alert state and decision updates**

Use `RouteDeviationAlertPolicy.evaluate()` inside the projected GPS fix update path after `LocationBackedHikeSessionEngine.applyLocationFix(...)`. Store `RouteDeviationAlertState` and latest `RouteDeviationAlertDecision` in route-session-scoped Compose state. Reset both when location is disabled, permission is denied, route session resets, or the user acknowledges a rejoined route.

- [x] **Step 2: Render compact alert before recovery panel**

Pass the latest decision into `GpsTrackPanel`, derive `RouteDeviationAlertPresentationEngine.present(decision)`, and render `RouteDeviationAlertBanner` before `RouteDeviationRecoveryPanel`.

- [x] **Step 3: Run focused compile/test**

Run:

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationAlertPresentationEngineTest" --no-daemon
```

Expected: `BUILD SUCCESSFUL`.

## Task 3: Verification And PR Update

**Files:**
- Modify: OpenSpec and plan files
- Create/modify: presentation model, tests, route screen

- [x] **Step 1: Run full verification**

```powershell
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue; $env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'; $env:ANDROID_SDK_ROOT=$env:ANDROID_HOME; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon
openspec validate --all --strict
git diff --check
```

- [x] **Step 2: Commit**

```powershell
git add android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationAlertPresentationEngine.kt android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationAlertPresentationEngineTest.kt android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt docs/superpowers/plans/2026-06-25-trailmate-p0-off-route-alert-surface.md openspec/changes/trailmate-p0-off-route-alert-policy
git commit -m "feat: surface off-route alerts in route panel"
```
