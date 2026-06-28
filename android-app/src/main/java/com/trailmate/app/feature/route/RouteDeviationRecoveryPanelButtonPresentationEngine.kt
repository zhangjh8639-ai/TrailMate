package com.trailmate.app.feature.route

import com.trailmate.app.core.model.RouteDeviationRecoveryActionKind
import com.trailmate.app.core.model.RouteDeviationRecoveryPresentation
import com.trailmate.app.core.model.RouteDeviationRecoveryTone

enum class RouteDeviationRecoveryPanelActionKind {
    NONE,
    ACKNOWLEDGE_REJOIN,
    REQUEST_LOCATION,
    SHARE_LOCATION
}

data class RouteDeviationRecoveryPanelButtonPresentation(
    val label: String,
    val visible: Boolean,
    val kind: RouteDeviationRecoveryPanelActionKind
)

object RouteDeviationRecoveryPanelButtonPresentationEngine {
    fun present(
        presentation: RouteDeviationRecoveryPresentation,
        safetyShareTextAvailable: Boolean
    ): RouteDeviationRecoveryPanelButtonPresentation {
        val label = presentation.primaryActionLabel
        if (!presentation.visible || label.isBlank()) {
            return RouteDeviationRecoveryPanelButtonPresentation(
                label = label,
                visible = false,
                kind = RouteDeviationRecoveryPanelActionKind.NONE
            )
        }

        if (presentation.tone == RouteDeviationRecoveryTone.REJOINED) {
            return RouteDeviationRecoveryPanelButtonPresentation(
                label = label,
                visible = true,
                kind = RouteDeviationRecoveryPanelActionKind.ACKNOWLEDGE_REJOIN
            )
        }

        val actionKinds = presentation.actions.map { action -> action.kind }.toSet()
        val hasShareAction = RouteDeviationRecoveryActionKind.SHARE_LOCATION in actionKinds
        val kind = if (
            safetyShareTextAvailable &&
            hasShareAction
        ) {
            RouteDeviationRecoveryPanelActionKind.SHARE_LOCATION
        } else {
            RouteDeviationRecoveryPanelActionKind.REQUEST_LOCATION
        }

        return RouteDeviationRecoveryPanelButtonPresentation(
            label = if (kind == RouteDeviationRecoveryPanelActionKind.SHARE_LOCATION) {
                label
            } else {
                requestLocationLabel(label = label, hasUnavailableShareAction = hasShareAction)
            },
            visible = true,
            kind = kind
        )
    }

    private fun requestLocationLabel(
        label: String,
        hasUnavailableShareAction: Boolean
    ): String =
        if (hasUnavailableShareAction) "重新定位" else label
}
