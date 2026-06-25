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

    @Test
    fun rejoinedDecisionStaysVisibleUntilAcknowledged() {
        val rejoined = decision(
            kind = RouteDeviationAlertKind.REJOINED_ROUTE,
            title = "已回到路线",
            caption = "当前位置已回到计划路线附近，请确认下一检查点后继续。",
            primaryActionLabel = "继续导航"
        )

        val displayed = RouteDeviationAlertPresentationEngine.displayDecision(
            previous = rejoined,
            next = decision(kind = RouteDeviationAlertKind.NONE)
        )

        assertEquals(rejoined, displayed)
    }

    @Test
    fun noneDecisionCanHideNonRejoinedAlerts() {
        val displayed = RouteDeviationAlertPresentationEngine.displayDecision(
            previous = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE_SILENT,
                title = "偏离恢复中",
                caption = "继续核对路线，暂不重复提醒。当前位置距计划路线约 125 m。",
                primaryActionLabel = "查看恢复建议"
            ),
            next = decision(kind = RouteDeviationAlertKind.NONE)
        )

        assertEquals(RouteDeviationAlertKind.NONE, displayed?.kind)
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
