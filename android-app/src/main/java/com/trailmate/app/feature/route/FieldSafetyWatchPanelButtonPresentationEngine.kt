package com.trailmate.app.feature.route

data class FieldSafetyWatchPanelButtonPresentation(
    val label: String,
    val visible: Boolean,
    val manualGuidanceLabel: String
)

object FieldSafetyWatchPanelButtonPresentationEngine {
    fun present(
        primaryActionLabel: String,
        primaryActionRequiresSafetyShare: Boolean
    ): FieldSafetyWatchPanelButtonPresentation {
        val visible = primaryActionRequiresSafetyShare && primaryActionLabel.isNotBlank()
        return FieldSafetyWatchPanelButtonPresentation(
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
