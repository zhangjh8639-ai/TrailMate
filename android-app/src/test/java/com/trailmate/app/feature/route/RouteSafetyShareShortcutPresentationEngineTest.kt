package com.trailmate.app.feature.route

import com.trailmate.app.core.model.SafetySharePresentation
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteSafetyShareShortcutPresentationEngineTest {
    @Test
    fun availableShareUsesCompactSafetyShareLabel() {
        val button = RouteSafetyShareShortcutPresentationEngine.present(
            presentation = safetyShare(
                primaryActionLabel = "分享当前记录位置",
                shareText = "TrailMate 安全分享"
            )
        )

        assertEquals("安全分享", button.label)
        assertEquals(RouteSafetyShareShortcutActionKind.SHARE_LOCATION, button.kind)
    }

    @Test
    fun missingLocationUsesLocationRequestLabel() {
        val button = RouteSafetyShareShortcutPresentationEngine.present(
            presentation = safetyShare(
                primaryActionLabel = "授权定位",
                shareText = null
            )
        )

        assertEquals("授权定位", button.label)
        assertEquals(RouteSafetyShareShortcutActionKind.REQUEST_LOCATION, button.kind)
    }

    @Test
    fun staleLocationUsesRefreshLocationLabel() {
        val button = RouteSafetyShareShortcutPresentationEngine.present(
            presentation = safetyShare(
                primaryActionLabel = "重新定位",
                shareText = null
            )
        )

        assertEquals("重新定位", button.label)
        assertEquals(RouteSafetyShareShortcutActionKind.REQUEST_LOCATION, button.kind)
    }

    @Test
    fun blankRepairLabelFallsBackToRefreshLocation() {
        val button = RouteSafetyShareShortcutPresentationEngine.present(
            presentation = safetyShare(
                primaryActionLabel = " ",
                shareText = null
            )
        )

        assertEquals("重新定位", button.label)
        assertEquals(RouteSafetyShareShortcutActionKind.REQUEST_LOCATION, button.kind)
    }

    private fun safetyShare(
        primaryActionLabel: String,
        shareText: String?
    ) = SafetySharePresentation(
        title = "安全分享",
        statusLabel = "位置状态",
        caption = "用于路线页快捷入口",
        primaryActionLabel = primaryActionLabel,
        shareText = shareText
    )
}
