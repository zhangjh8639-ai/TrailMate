package com.trailmate.app.feature.route

import com.trailmate.app.core.model.RouteDeviationRecoveryAction
import com.trailmate.app.core.model.RouteDeviationRecoveryActionKind
import com.trailmate.app.core.model.RouteDeviationRecoveryPresentation
import com.trailmate.app.core.model.RouteDeviationRecoveryTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationRecoveryPanelButtonPresentationEngineTest {
    @Test
    fun rejoinedRecoveryAcknowledgesRouteRejoin() {
        val button = RouteDeviationRecoveryPanelButtonPresentationEngine.present(
            presentation = recovery(
                primaryActionLabel = "继续导航",
                tone = RouteDeviationRecoveryTone.REJOINED,
                actions = listOf(action(RouteDeviationRecoveryActionKind.CONTINUE_NAVIGATION, "继续导航"))
            ),
            safetyShareTextAvailable = false
        )

        assertEquals("继续导航", button.label)
        assertEquals(RouteDeviationRecoveryPanelActionKind.ACKNOWLEDGE_REJOIN, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun lowAccuracyRecoveryRequestsFreshLocation() {
        val button = RouteDeviationRecoveryPanelButtonPresentationEngine.present(
            presentation = recovery(
                primaryActionLabel = "重新定位",
                tone = RouteDeviationRecoveryTone.OFF_ROUTE,
                actions = listOf(action(RouteDeviationRecoveryActionKind.WAIT_FOR_GPS, "重新定位"))
            ),
            safetyShareTextAvailable = false
        )

        assertEquals("重新定位", button.label)
        assertEquals(RouteDeviationRecoveryPanelActionKind.REQUEST_LOCATION, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun offRouteRecoverySharesLocationOnlyWhenShareTextIsAvailable() {
        val button = RouteDeviationRecoveryPanelButtonPresentationEngine.present(
            presentation = recovery(
                primaryActionLabel = "分享当前位置",
                tone = RouteDeviationRecoveryTone.OFF_ROUTE,
                actions = listOf(action(RouteDeviationRecoveryActionKind.SHARE_LOCATION, "分享当前位置"))
            ),
            safetyShareTextAvailable = true
        )

        assertEquals("分享当前位置", button.label)
        assertEquals(RouteDeviationRecoveryPanelActionKind.SHARE_LOCATION, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun shareRecoveryFallsBackToRelocationWhenShareTextIsUnavailable() {
        val button = RouteDeviationRecoveryPanelButtonPresentationEngine.present(
            presentation = recovery(
                primaryActionLabel = "发送安全位置",
                tone = RouteDeviationRecoveryTone.OFF_ROUTE,
                actions = listOf(action(RouteDeviationRecoveryActionKind.SHARE_LOCATION, "发送安全位置"))
            ),
            safetyShareTextAvailable = false
        )

        assertEquals("重新定位", button.label)
        assertEquals(RouteDeviationRecoveryPanelActionKind.REQUEST_LOCATION, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun hiddenRecoveryHidesButton() {
        val button = RouteDeviationRecoveryPanelButtonPresentationEngine.present(
            presentation = recovery(
                visible = false,
                primaryActionLabel = "继续导航",
                tone = RouteDeviationRecoveryTone.REJOINED,
                actions = listOf(action(RouteDeviationRecoveryActionKind.CONTINUE_NAVIGATION, "继续导航"))
            ),
            safetyShareTextAvailable = true
        )

        assertEquals("继续导航", button.label)
        assertEquals(RouteDeviationRecoveryPanelActionKind.NONE, button.kind)
        assertFalse(button.visible)
    }

    private fun recovery(
        visible: Boolean = true,
        primaryActionLabel: String,
        tone: RouteDeviationRecoveryTone,
        actions: List<RouteDeviationRecoveryAction>
    ) = RouteDeviationRecoveryPresentation(
        visible = visible,
        title = "偏离恢复",
        statusLabel = "停止自动推进",
        caption = "疑似偏离路线，请先停下核对当前位置。",
        primaryActionLabel = primaryActionLabel,
        steps = emptyList(),
        details = emptyList(),
        actions = actions,
        tone = tone
    )

    private fun action(
        kind: RouteDeviationRecoveryActionKind,
        label: String
    ) = RouteDeviationRecoveryAction(
        kind = kind,
        label = label,
        value = "检查当前位置"
    )
}
