package com.trailmate.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackRecordingServiceLaunchPolicyTest {
    @Test
    fun usesForegroundLocationServiceOnlyWhenPrecisePermissionAndGpsAreReady() {
        val mode = TrackRecordingServiceLaunchPolicy.resolve(
            hasPreciseLocationPermission = true,
            gpsProviderEnabled = true
        )

        assertEquals(TrackRecordingServiceLaunchMode.FOREGROUND_LOCATION_SERVICE, mode)
    }

    @Test
    fun usesShortServiceWhenPreciseLocationPermissionIsMissing() {
        val mode = TrackRecordingServiceLaunchPolicy.resolve(
            hasPreciseLocationPermission = false,
            gpsProviderEnabled = true
        )

        assertEquals(TrackRecordingServiceLaunchMode.SHORT_SERVICE, mode)
    }

    @Test
    fun usesShortServiceWhenGpsProviderIsDisabled() {
        val mode = TrackRecordingServiceLaunchPolicy.resolve(
            hasPreciseLocationPermission = true,
            gpsProviderEnabled = false
        )

        assertEquals(TrackRecordingServiceLaunchMode.SHORT_SERVICE, mode)
    }
}
