package com.trailmate.app.core.model

object GearDepartureQaOverridePolicy {
    fun apply(
        recommendations: List<GearRecommendation>,
        debugBypassEnabled: Boolean
    ): List<GearRecommendation> =
        if (!debugBypassEnabled) {
            recommendations
        } else {
            recommendations.map { recommendation ->
                if (recommendation.status == GearStatus.MISSING) {
                    recommendation.copy(status = GearStatus.COVERED)
                } else {
                    recommendation
                }
            }
        }
}
