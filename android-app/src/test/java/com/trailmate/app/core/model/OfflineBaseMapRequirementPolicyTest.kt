package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineBaseMapRequirementPolicyTest {
    @Test
    fun recommendsOfflineBaseMapsForRecommendedRoutesWithoutBlockingDeparture() {
        val assessment = assessment(matchLevel = MatchLevel.RECOMMENDED)

        assertEquals(
            OfflineBaseMapRequirement.RECOMMENDED,
            OfflineBaseMapRequirementPolicy.resolve(assessment)
        )
    }

    @Test
    fun requiresOfflineBaseMapsForCautionOrNotRecommendedRoutes() {
        assertEquals(
            OfflineBaseMapRequirement.REQUIRED,
            OfflineBaseMapRequirementPolicy.resolve(assessment(matchLevel = MatchLevel.CAUTION))
        )
        assertEquals(
            OfflineBaseMapRequirement.REQUIRED,
            OfflineBaseMapRequirementPolicy.resolve(assessment(matchLevel = MatchLevel.NOT_RECOMMENDED))
        )
    }

    private fun assessment(matchLevel: MatchLevel): RouteAssessmentSummary =
        RouteAssessmentSummary(
            routeName = "龙井山脊",
            distanceKm = 15.2,
            ascentMeters = 860,
            matchLevel = matchLevel,
            confidenceLevel = ConfidenceLevel.MEDIUM,
            estimatedDurationRange = "6:40-7:50",
            risks = emptyList()
        )
}
