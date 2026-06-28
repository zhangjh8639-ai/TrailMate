package com.trailmate.app.feature.route

data class ReturnEtaWatchPanelButtonPresentation(
    val label: String,
    val visible: Boolean,
    val manualGuidanceLabel: String
)

object ReturnEtaWatchPanelButtonPresentationEngine {
    fun present(
        primaryActionLabel: String,
        primaryActionRequiresSafetyShare: Boolean
    ): ReturnEtaWatchPanelButtonPresentation {
        val visible = primaryActionRequiresSafetyShare && primaryActionLabel.isNotBlank()
        return ReturnEtaWatchPanelButtonPresentation(
            label = primaryActionLabel,
            visible = visible,
            manualGuidanceLabel = if (!visible && !primaryActionRequiresSafetyShare) {
                primaryActionLabel.takeIf { it.isNotBlank() }.orEmpty()
            } else {
                ""
            }
        )
    }
}
