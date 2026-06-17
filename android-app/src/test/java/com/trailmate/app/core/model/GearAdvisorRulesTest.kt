package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GearAdvisorRulesTest {
    @Test
    fun gearRecommendationsDoNotChangeRouteAssessment() {
        val assessment = TrailMateSampleData.routeAssessment
        val checklist = TrailMateSampleData.gearRecommendations

        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertEquals("6:40-7:50", assessment.estimatedDurationRange)
        assertEquals(GearStatus.MISSING, checklist.first { it.category == "Trekking poles" }.status)
    }
}
