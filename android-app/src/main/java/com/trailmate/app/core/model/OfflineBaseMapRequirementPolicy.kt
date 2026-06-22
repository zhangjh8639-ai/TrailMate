package com.trailmate.app.core.model

enum class OfflineBaseMapRequirement {
    REQUIRED,
    RECOMMENDED
}

object OfflineBaseMapRequirementPolicy {
    fun resolve(assessment: RouteAssessmentSummary): OfflineBaseMapRequirement =
        when (assessment.matchLevel) {
            MatchLevel.RECOMMENDED -> OfflineBaseMapRequirement.RECOMMENDED
            MatchLevel.CAUTION,
            MatchLevel.NOT_RECOMMENDED -> OfflineBaseMapRequirement.REQUIRED
        }
}
