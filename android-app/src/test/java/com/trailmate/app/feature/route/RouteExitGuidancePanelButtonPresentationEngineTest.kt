package com.trailmate.app.feature.route

import com.trailmate.app.core.model.RouteExitGuidanceOption
import com.trailmate.app.core.model.RouteExitGuidancePresentation
import com.trailmate.app.core.model.RouteExitGuidanceTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteExitGuidancePanelButtonPresentationEngineTest {
    @Test
    fun hidesButtonForReadyExitRecommendation() {
        val button = RouteExitGuidancePanelButtonPresentationEngine.present(
            presentation = routeExitGuidance(
                primaryActionLabel = "原路返回",
                tone = RouteExitGuidanceTone.READY
            )
        )

        assertEquals("原路返回", button.label)
        assertFalse(button.visible)
    }

    @Test
    fun showsButtonForCautionLocationRepair() {
        val button = RouteExitGuidancePanelButtonPresentationEngine.present(
            presentation = routeExitGuidance(
                primaryActionLabel = "重新定位",
                tone = RouteExitGuidanceTone.CAUTION
            )
        )

        assertEquals("重新定位", button.label)
        assertTrue(button.visible)
    }

    @Test
    fun hidesButtonWhenCautionActionLabelIsBlank() {
        val button = RouteExitGuidancePanelButtonPresentationEngine.present(
            presentation = routeExitGuidance(
                primaryActionLabel = " ",
                tone = RouteExitGuidanceTone.CAUTION
            )
        )

        assertEquals(" ", button.label)
        assertFalse(button.visible)
    }

    private fun routeExitGuidance(
        primaryActionLabel: String,
        tone: RouteExitGuidanceTone
    ) = RouteExitGuidancePresentation(
        title = "安全退出",
        statusLabel = "先稳定定位",
        caption = "当前定位不适合判断撤退方向。",
        primaryActionLabel = primaryActionLabel,
        options = listOf(
            RouteExitGuidanceOption(
                label = "重新定位",
                distanceLabel = "优先",
                caption = "等待定位精度稳定后再判断。"
            )
        ),
        tone = tone
    )
}
