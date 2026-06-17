package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GearAdvisorRulesTest {
    @Test
    fun gearRecommendationsDoNotChangeRouteAssessment() {
        val assessment = TrailMateSampleData.routeAssessment
        val checklist = TrailMateSampleData.gearRecommendations

        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertEquals(ConfidenceLevel.LOW, assessment.confidenceLevel)
        assertEquals(15.2, assessment.distanceKm, 0.0)
        assertEquals(GearStatus.MISSING, checklist.first { it.category == "Trekking poles" }.status)
    }
}
