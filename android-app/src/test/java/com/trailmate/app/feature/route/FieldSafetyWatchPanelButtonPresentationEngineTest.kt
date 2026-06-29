package com.trailmate.app.feature.route

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldSafetyWatchPanelButtonPresentationEngineTest {
    @Test
    fun showsButtonForSafetyShareAction() {
        val button = FieldSafetyWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "分享当前位置",
            primaryActionRequiresSafetyShare = true,
            safetyShareTextAvailable = true,
            safetyShareRepairLabel = "重新定位"
        )

        assertEquals("分享当前位置", button.label)
        assertEquals(FieldSafetyWatchPanelActionKind.SHARE_LOCATION, button.kind)
        assertEquals("", button.manualGuidanceLabel)
        assertTrue(button.visible)
    }

    @Test
    fun safetyShareActionRequestsLocationWhenShareTextIsUnavailable() {
        val button = FieldSafetyWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "分享当前位置",
            primaryActionRequiresSafetyShare = true,
            safetyShareTextAvailable = false,
            safetyShareRepairLabel = "重新定位"
        )

        assertEquals("重新定位", button.label)
        assertEquals(FieldSafetyWatchPanelActionKind.REQUEST_LOCATION, button.kind)
        assertEquals("", button.manualGuidanceLabel)
        assertTrue(button.visible)
    }

    @Test
    fun hidesButtonForManualReviewSuggestion() {
        val button = FieldSafetyWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "复核天黑前路线",
            primaryActionRequiresSafetyShare = false,
            safetyShareTextAvailable = false,
            safetyShareRepairLabel = "重新定位"
        )

        assertEquals("复核天黑前路线", button.label)
        assertEquals(FieldSafetyWatchPanelActionKind.NONE, button.kind)
        assertEquals("复核天黑前路线", button.manualGuidanceLabel)
        assertFalse(button.visible)
    }

    @Test
    fun hidesButtonForManualRestSuggestion() {
        val button = FieldSafetyWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = "先休息复核",
            primaryActionRequiresSafetyShare = false,
            safetyShareTextAvailable = false,
            safetyShareRepairLabel = "重新定位"
        )

        assertEquals("先休息复核", button.label)
        assertEquals(FieldSafetyWatchPanelActionKind.NONE, button.kind)
        assertEquals("先休息复核", button.manualGuidanceLabel)
        assertFalse(button.visible)
    }

    @Test
    fun hidesButtonForBlankSafetyShareLabel() {
        val button = FieldSafetyWatchPanelButtonPresentationEngine.present(
            primaryActionLabel = " ",
            primaryActionRequiresSafetyShare = true,
            safetyShareTextAvailable = true,
            safetyShareRepairLabel = "重新定位"
        )

        assertEquals(" ", button.label)
        assertEquals(FieldSafetyWatchPanelActionKind.NONE, button.kind)
        assertEquals("", button.manualGuidanceLabel)
        assertFalse(button.visible)
    }
}
