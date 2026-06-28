package com.trailmate.app.feature.route

import com.trailmate.app.core.model.RouteExitGuidancePresentation
import com.trailmate.app.core.model.RouteExitGuidanceTone

data class RouteExitGuidancePanelButtonPresentation(
    val label: String,
    val visible: Boolean
)

object RouteExitGuidancePanelButtonPresentationEngine {
    fun present(presentation: RouteExitGuidancePresentation): RouteExitGuidancePanelButtonPresentation =
        RouteExitGuidancePanelButtonPresentation(
            label = presentation.primaryActionLabel,
            visible = presentation.tone == RouteExitGuidanceTone.CAUTION &&
                presentation.primaryActionLabel.isNotBlank()
        )
}
