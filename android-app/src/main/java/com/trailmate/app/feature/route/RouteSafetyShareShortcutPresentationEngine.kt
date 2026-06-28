package com.trailmate.app.feature.route

import com.trailmate.app.core.model.SafetySharePresentation

enum class RouteSafetyShareShortcutActionKind {
    REQUEST_LOCATION,
    SHARE_LOCATION
}

data class RouteSafetyShareShortcutPresentation(
    val label: String,
    val kind: RouteSafetyShareShortcutActionKind
)

object RouteSafetyShareShortcutPresentationEngine {
    fun present(presentation: SafetySharePresentation): RouteSafetyShareShortcutPresentation {
        if (presentation.shareText != null) {
            return RouteSafetyShareShortcutPresentation(
                label = "安全分享",
                kind = RouteSafetyShareShortcutActionKind.SHARE_LOCATION
            )
        }

        return RouteSafetyShareShortcutPresentation(
            label = presentation.primaryActionLabel.takeIf { it.isNotBlank() } ?: "重新定位",
            kind = RouteSafetyShareShortcutActionKind.REQUEST_LOCATION
        )
    }
}
