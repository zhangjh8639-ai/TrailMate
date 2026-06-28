package com.trailmate.app.feature.route

import com.trailmate.app.core.model.DepartureBriefSharePresentation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureBriefSharePanelButtonPresentationEngineTest {
    @Test
    fun showsSendButtonWhenShareTextIsAvailable() {
        val button = DepartureBriefSharePanelButtonPresentationEngine.present(
            presentation = DepartureBriefSharePresentation(
                title = "出发报备",
                statusLabel = "可发送",
                caption = "手动发送路线计划和预计返回时间。",
                primaryActionLabel = "发送出发报备",
                shareText = "TrailMate 出发报备"
            )
        )

        assertEquals("发送出发报备", button.label)
        assertTrue(button.visible)
    }

    @Test
    fun hidesButtonWhenDepartureBriefCannotBeSent() {
        val button = DepartureBriefSharePanelButtonPresentationEngine.present(
            presentation = DepartureBriefSharePresentation(
                title = "出发报备",
                statusLabel = "缺少预计用时",
                caption = "当前路线缺少预计用时，无法生成可靠的出发报备。",
                primaryActionLabel = "查看路线评估",
                shareText = null
            )
        )

        assertEquals("查看路线评估", button.label)
        assertFalse(button.visible)
    }

    @Test
    fun hidesButtonWhenSendableBriefHasBlankActionLabel() {
        val button = DepartureBriefSharePanelButtonPresentationEngine.present(
            presentation = DepartureBriefSharePresentation(
                title = "出发报备",
                statusLabel = "可发送",
                caption = "手动发送路线计划和预计返回时间。",
                primaryActionLabel = " ",
                shareText = "TrailMate 出发报备"
            )
        )

        assertEquals(" ", button.label)
        assertFalse(button.visible)
    }
}
