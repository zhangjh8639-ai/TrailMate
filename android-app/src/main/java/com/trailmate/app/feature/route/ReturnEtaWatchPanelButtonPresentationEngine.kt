package com.trailmate.app.feature.route

enum class ReturnEtaWatchPanelActionKind {
    NONE,
    REQUEST_LOCATION,
    SHARE_LOCATION
}

data class ReturnEtaWatchPanelButtonPresentation(
    val label: String,
    val visible: Boolean,
    val manualGuidanceLabel: String,
    val kind: ReturnEtaWatchPanelActionKind
)

object ReturnEtaWatchPanelButtonPresentationEngine {
    fun present(
        primaryActionLabel: String,
        primaryActionRequiresSafetyShare: Boolean,
        safetyShareTextAvailable: Boolean,
        safetyShareRepairLabel: String
    ): ReturnEtaWatchPanelButtonPresentation {
        if (!primaryActionRequiresSafetyShare) {
            return ReturnEtaWatchPanelButtonPresentation(
                label = primaryActionLabel,
                visible = false,
                manualGuidanceLabel = primaryActionLabel.takeIf { it.isNotBlank() }.orEmpty(),
                kind = ReturnEtaWatchPanelActionKind.NONE
            )
        }

        if (primaryActionLabel.isBlank()) {
            return ReturnEtaWatchPanelButtonPresentation(
                label = primaryActionLabel,
                visible = false,
                manualGuidanceLabel = "",
                kind = ReturnEtaWatchPanelActionKind.NONE
            )
        }

        val kind = if (safetyShareTextAvailable) {
            ReturnEtaWatchPanelActionKind.SHARE_LOCATION
        } else {
            ReturnEtaWatchPanelActionKind.REQUEST_LOCATION
        }

        return ReturnEtaWatchPanelButtonPresentation(
            label = if (kind == ReturnEtaWatchPanelActionKind.SHARE_LOCATION) {
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
