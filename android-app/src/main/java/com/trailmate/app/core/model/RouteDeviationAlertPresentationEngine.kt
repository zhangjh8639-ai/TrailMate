package com.trailmate.app.core.model

enum class RouteDeviationAlertTone {
    URGENT,
    CAUTION,
    REJOINED
}

data class RouteDeviationAlertPresentation(
    val visible: Boolean,
    val tone: RouteDeviationAlertTone,
    val title: String,
    val caption: String,
    val primaryActionLabel: String,
    val shouldRequestAttention: Boolean
)

object RouteDeviationAlertPresentationEngine {
    fun displayDecision(
        previous: RouteDeviationAlertDecision?,
        next: RouteDeviationAlertDecision
    ): RouteDeviationAlertDecision? =
        when {
            next.kind != RouteDeviationAlertKind.NONE -> next
            previous?.kind == RouteDeviationAlertKind.REJOINED_ROUTE -> previous
            else -> next
        }

    fun present(decision: RouteDeviationAlertDecision?): RouteDeviationAlertPresentation {
        if (decision == null || decision.kind == RouteDeviationAlertKind.NONE) {
            return hidden()
        }

        val tone = when (decision.kind) {
            RouteDeviationAlertKind.OFF_ROUTE,
            RouteDeviationAlertKind.OFF_ROUTE_ESCALATED -> RouteDeviationAlertTone.URGENT
            RouteDeviationAlertKind.REJOINED_ROUTE -> RouteDeviationAlertTone.REJOINED
            RouteDeviationAlertKind.OFF_ROUTE_SILENT,
            RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX -> RouteDeviationAlertTone.CAUTION
            RouteDeviationAlertKind.NONE -> RouteDeviationAlertTone.CAUTION
        }

        return RouteDeviationAlertPresentation(
            visible = true,
            tone = tone,
            title = decision.title,
            caption = decision.caption,
            primaryActionLabel = decision.primaryActionLabel,
            shouldRequestAttention = tone == RouteDeviationAlertTone.URGENT &&
                (decision.shouldNotify || decision.shouldVibrate)
        )
    }

    private fun hidden(): RouteDeviationAlertPresentation =
        RouteDeviationAlertPresentation(
            visible = false,
            tone = RouteDeviationAlertTone.CAUTION,
            title = "",
            caption = "",
            primaryActionLabel = "",
            shouldRequestAttention = false
        )
}
