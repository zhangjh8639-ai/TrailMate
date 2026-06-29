package com.trailmate.app.feature.route

enum class FieldSafetyWatchPanelActionKind {
    NONE,
    REQUEST_LOCATION,
    SHARE_LOCATION
}

data class FieldSafetyWatchPanelButtonPresentation(
    val label: String,
    val visible: Boolean,
    val manualGuidanceLabel: String,
    val kind: FieldSafetyWatchPanelActionKind
)

object FieldSafetyWatchPanelButtonPresentationEngine {
    fun present(
        primaryActionLabel: String,
        primaryActionRequiresSafetyShare: Boolean,
        safetyShareTextAvailable: Boolean,
        safetyShareRepairLabel: String
    ): FieldSafetyWatchPanelButtonPresentation {
        if (!primaryActionRequiresSafetyShare) {
            return FieldSafetyWatchPanelButtonPresentation(
                label = primaryActionLabel,
                visible = false,
                manualGuidanceLabel = primaryActionLabel.takeIf { it.isNotBlank() }.orEmpty(),
                kind = FieldSafetyWatchPanelActionKind.NONE
            )
        }

        if (primaryActionLabel.isBlank()) {
            return FieldSafetyWatchPanelButtonPresentation(
                label = primaryActionLabel,
                visible = false,
                manualGuidanceLabel = "",
                kind = FieldSafetyWatchPanelActionKind.NONE
            )
        }

        val kind = if (safetyShareTextAvailable) {
            FieldSafetyWatchPanelActionKind.SHARE_LOCATION
        } else {
            FieldSafetyWatchPanelActionKind.REQUEST_LOCATION
        }

        return FieldSafetyWatchPanelButtonPresentation(
            label = if (kind == FieldSafetyWatchPanelActionKind.SHARE_LOCATION) {
                primaryActionLabel
            } else {
                safetyShareRepairLabel.takeIf { it.isNotBlank() } ?: "重新定位"
            },
            visible = true,
            manualGuidanceLabel = "",
            kind = kind
        )
    }
}
