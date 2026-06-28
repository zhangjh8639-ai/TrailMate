package com.trailmate.app.feature.route

import com.trailmate.app.core.model.DepartureReadinessPrimaryAction
import com.trailmate.app.core.model.DepartureReadinessPrimaryActionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureReadinessPanelButtonPresentationEngineTest {
    @Test
    fun enablesButtonWhenPrimaryActionIsEnabled() {
        val presentation = DepartureReadinessPanelButtonPresentationEngine.present(
            action = DepartureReadinessPrimaryAction(
                label = "开始徒步并记录轨迹",
                kind = DepartureReadinessPrimaryActionKind.START_HIKE_AND_RECORD,
                enabled = true
            )
        )

        assertEquals("下一步：开始徒步并记录轨迹", presentation.label)
        assertTrue(presentation.enabled)
    }

    @Test
    fun disablesButtonWhenSharedPrimaryActionIsBlocked() {
        val presentation = DepartureReadinessPanelButtonPresentationEngine.present(
            action = DepartureReadinessPrimaryAction(
                label = "重新导入 GPX",
                kind = DepartureReadinessPrimaryActionKind.BLOCKED,
                enabled = false
            )
        )

        assertEquals("下一步：重新导入 GPX", presentation.label)
        assertFalse(presentation.enabled)
    }
}
