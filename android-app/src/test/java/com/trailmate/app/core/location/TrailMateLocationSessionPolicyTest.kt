package com.trailmate.app.core.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateLocationSessionPolicyTest {
    @Test
    fun keepsLocationRequestActiveWhileTrackerIsSearchingOrProducingFixes() {
        assertTrue(
            TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(
                TrailMateLocationSnapshot.searching()
            )
        )
        assertTrue(
            TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(
                TrailMateLocationSnapshot(
                    status = TrailMateLocationStatus.LOCATED,
                    latitude = 30.25,
                    longitude = 120.15,
                    elevationMeters = null,
                    horizontalAccuracyMeters = 18.0,
                    timestampEpochMillis = 1_000L
                )
            )
        )
        assertTrue(
            TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(
                TrailMateLocationSnapshot(
                    status = TrailMateLocationStatus.LOW_ACCURACY,
                    latitude = 30.25,
                    longitude = 120.15,
                    elevationMeters = null,
                    horizontalAccuracyMeters = 88.0,
                    timestampEpochMillis = 1_000L
                )
            )
        )
    }

    @Test
    fun stopsLocationRequestWhenTrackerReportsTerminalSetupFailure() {
        assertFalse(
            TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(
                TrailMateLocationSnapshot.permissionRequired()
            )
        )
        assertFalse(
            TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(
                TrailMateLocationSnapshot.providerDisabled()
            )
        )
        assertFalse(
            TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(
                TrailMateLocationSnapshot.unavailable()
            )
        )
        assertFalse(
            TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(
                TrailMateLocationSnapshot.disabled()
            )
        )
    }
}
