package com.trailmate.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateLocationTrackingRequestPolicyTest {
    @Test
    fun requestsUpdatesWithoutDistanceGateSoStationaryCalibrationCanRefresh() {
        assertEquals(3_000L, TrailMateLocationTrackingRequestPolicy.MIN_TIME_MILLIS)
        assertEquals(0f, TrailMateLocationTrackingRequestPolicy.MIN_DISTANCE_METERS)
    }
}
