package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalCapabilityProfileEngineTest {
    @Test
    fun insufficientHistoryDoesNotBuildProductionCapabilityProfile() {
        val profile = HistoricalCapabilityProfileEngine.build(
            listOf(
                HistoricalActivity("Easy A", distanceKm = 4.0, ascentMeters = 120, durationMinutes = 70),
                HistoricalActivity("Easy B", distanceKm = 5.0, ascentMeters = 160, durationMinutes = 80)
            )
        )

        assertNull(profile)
    }

    @Test
    fun buildsStableCapacityFromHistoricalGpxActivities() {
        val profile = HistoricalCapabilityProfileEngine.build(TrailMateSampleData.historicalActivities)

        requireNotNull(profile)
        assertEquals(3, profile.activityCount)
        assertEquals(18.6, profile.stableDistanceKm, 0.0)
        assertEquals(980.0, profile.stableAscentMeters, 0.0)
        assertEquals(13.7, profile.averageDistanceKm, 0.1)
        assertEquals(720, profile.averageAscentMeters)
        assertEquals(21.0, profile.averagePaceMinutesPerKm ?: 0.0, 0.5)
        assertTrue((profile.effectiveSpeedKmh ?: 0.0) > 0.0)
        assertEquals(ConfidenceLevel.MEDIUM, profile.confidenceLevel)
    }

    @Test
    fun clampsMalformedHistoryToFiniteMinimums() {
        val profile = HistoricalCapabilityProfileEngine.build(
            listOf(
                HistoricalActivity("Bad A", distanceKm = 0.0, ascentMeters = 0, durationMinutes = 0),
                HistoricalActivity("Bad B", distanceKm = 0.0, ascentMeters = 0, durationMinutes = 0),
                HistoricalActivity("Bad C", distanceKm = 0.0, ascentMeters = 0, durationMinutes = 0)
            )
        )

        requireNotNull(profile)
        assertEquals(1.0, profile.stableDistanceKm, 0.0)
        assertEquals(100.0, profile.stableAscentMeters, 0.0)
        assertNull(profile.averagePaceMinutesPerKm)
        assertNull(profile.effectiveSpeedKmh)
    }

    @Test
    fun clampsNonFiniteDistancesOutOfProductionProfile() {
        val profile = HistoricalCapabilityProfileEngine.build(
            listOf(
                HistoricalActivity("Bad A", distanceKm = Double.NaN, ascentMeters = 120, durationMinutes = 40),
                HistoricalActivity("Bad B", distanceKm = Double.POSITIVE_INFINITY, ascentMeters = 160, durationMinutes = 50),
                HistoricalActivity("Good C", distanceKm = 4.0, ascentMeters = 200, durationMinutes = 80)
            )
        )

        requireNotNull(profile)
        assertEquals(4.0, profile.stableDistanceKm, 0.0)
        assertEquals(4.0, profile.averageDistanceKm, 0.0)
        assertEquals(20.0, profile.averagePaceMinutesPerKm ?: 0.0, 0.0)
        assertTrue(profile.stableDistanceKm.isFinite())
        assertTrue(profile.averageDistanceKm.isFinite())
    }
}
