package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityProfileEngineTest {
    @Test
    fun emptyHistoryKeepsQuestionnaireFallback() {
        val profile = CapabilityProfileEngine.build(
            baselineProfile = TrailMateSampleData.baselineProfile,
            historicalActivities = emptyList()
        )

        assertEquals(ConfidenceLevel.LOW, profile.confidenceLevel)
        assertEquals("0/3 GPX", profile.evidenceLabel)
        assertEquals("Questionnaire fallback", profile.title)
        assertTrue(profile.caption.contains("Use sample history"))
    }

    @Test
    fun threeHistoricalActivitiesBuildEvidenceProfile() {
        val profile = CapabilityProfileEngine.build(
            baselineProfile = TrailMateSampleData.baselineProfile,
            historicalActivities = TrailMateSampleData.historicalActivities
        )

        assertEquals(ConfidenceLevel.MEDIUM, profile.confidenceLevel)
        assertEquals("3/3 GPX", profile.evidenceLabel)
        assertEquals("Historical profile", profile.title)
        assertEquals("Longest 18.6 km / +980 m", profile.value)
        assertTrue(profile.caption.contains("Average 13.7 km"))
        assertTrue(profile.caption.contains("+720 m"))
    }
}
