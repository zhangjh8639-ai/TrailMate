package com.trailmate.app.feature.route

import com.trailmate.app.core.location.TrailMateLocationStatus
import com.trailmate.app.core.model.TrackRecordingStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteLocationPresentationClockPolicyTest {
    @Test
    fun refreshesLocatedFixesSoSafetyShareCanExpire() {
        val shouldRefresh = RouteLocationPresentationClockPolicy.shouldRefresh(
            gpsEnabled = true,
            locationStatus = TrailMateLocationStatus.LOCATED,
            trackRecordingStatus = TrackRecordingStatus.IDLE
        )

        assertTrue(shouldRefresh)
    }

    @Test
    fun refreshesLowAccuracyFixesSoRepairCopyCanStayCurrent() {
        val shouldRefresh = RouteLocationPresentationClockPolicy.shouldRefresh(
            gpsEnabled = true,
            locationStatus = TrailMateLocationStatus.LOW_ACCURACY,
            trackRecordingStatus = TrackRecordingStatus.IDLE
        )

        assertTrue(shouldRefresh)
    }

    @Test
    fun doesNotRefreshWhenGpsIsDisabled() {
        val shouldRefresh = RouteLocationPresentationClockPolicy.shouldRefresh(
            gpsEnabled = false,
            locationStatus = TrailMateLocationStatus.LOCATED,
            trackRecordingStatus = TrackRecordingStatus.RECORDING
        )

        assertFalse(shouldRefresh)
    }

    @Test
    fun doesNotRefreshTerminalUnavailableStates() {
        val statuses = listOf(
            TrailMateLocationStatus.DISABLED,
            TrailMateLocationStatus.PERMISSION_REQUIRED,
            TrailMateLocationStatus.PROVIDER_DISABLED,
            TrailMateLocationStatus.UNAVAILABLE
        )

        statuses.forEach { status ->
            val shouldRefresh = RouteLocationPresentationClockPolicy.shouldRefresh(
                gpsEnabled = true,
                locationStatus = status,
                trackRecordingStatus = TrackRecordingStatus.IDLE
            )

            assertFalse("status=$status", shouldRefresh)
        }
    }
}
