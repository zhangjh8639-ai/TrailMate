package com.trailmate.app.feature.route

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReturnEtaWatchPanelButtonPresentationEngineTest {
    @Test
    fun showsButtonForSafetyShareAction() {
        val button = ReturnEtaWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "分享当前位置",
            primaryActionRequiresSafetyShare = true
        )

        assertEquals("分享当前位置", button.label)
        assertEquals("", button.manualGuidanceLabel)
        assertTrue(button.visible)
    }

    @Test
    fun hidesButtonButKeepsMissingDurationGuidance() {
        val button = ReturnEtaWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "查看路线评估",
            primaryActionRequiresSafetyShare = false
        )

        assertEquals("查看路线评估", button.label)
        assertEquals("查看路线评估", button.manualGuidanceLabel)
        assertFalse(button.visible)
    }

    @Test
    fun hidesButtonButKeepsNotStartedGuidance() {
        val button = ReturnEtaWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "开始徒步后计算",
            primaryActionRequiresSafetyShare = false
        )

        assertEquals("开始徒步后计算", button.label)
        assertEquals("开始徒步后计算", button.manualGuidanceLabel)
        assertFalse(button.visible)
    }

    @Test
    fun hidesButtonButKeepsReadyObservationGuidance() {
        val button = ReturnEtaWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "继续观察",
            primaryActionRequiresSafetyShare = false
        )

        assertEquals("继续观察", button.label)
        assertEquals("继续观察", button.manualGuidanceLabel)
        assertFalse(button.visible)
    }

    @Test
    fun hidesBlankSafetyShareButton() {
        val button = ReturnEtaWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = " ",
            primaryActionRequiresSafetyShare = true
        )

        assertEquals(" ", button.label)
        assertEquals("", button.manualGuidanceLabel)
        assertFalse(button.visible)
    }
}
