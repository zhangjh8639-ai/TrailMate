package com.trailmate.app.feature.route

import com.trailmate.app.core.model.DepartureBriefSharePresentation

data class DepartureBriefSharePanelButtonPresentation(
    val label: String,
    val visible: Boolean
)

object DepartureBriefSharePanelButtonPresentationEngine {
    fun present(presentation: DepartureBriefSharePresentation): DepartureBriefSharePanelButtonPresentation =
        DepartureBriefSharePanelButtonPresentation(
            label = presentation.primaryActionLabel,
            visible = presentation.shareText != null && presentation.primaryActionLabel.isNotBlank()
        )
}
