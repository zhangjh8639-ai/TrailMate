package com.trailmate.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrailMateLocationProviderPolicyTest {
    @Test
    fun preciseLocationUsesGpsWhenAvailable() {
        val provider = TrailMateLocationProviderPolicy.resolve(
            hasFineLocationPermission = true,
            hasCoarseLocationPermission = true,
            gpsProviderEnabled = true,
            networkProviderEnabled = true
        )

        assertEquals(TrailMateLocationProvider.GPS, provider)
    }

    @Test
    fun preciseLocationDoesNotFallBackToNetworkWhenGpsIsDisabled() {
        val provider = TrailMateLocationProviderPolicy.resolve(
            hasFineLocationPermission = true,
            hasCoarseLocationPermission = true,
            gpsProviderEnabled = false,
            networkProviderEnabled = true
        )

        assertNull(provider)
    }

    @Test
    fun approximateOnlyLocationDoesNotSatisfyOutdoorGpsProvider() {
        val provider = TrailMateLocationProviderPolicy.resolve(
            hasFineLocationPermission = false,
            hasCoarseLocationPermission = true,
            gpsProviderEnabled = true,
            networkProviderEnabled = true
        )

        assertNull(provider)
    }

    @Test
    fun approximateOnlyLocationHasNoUsableProviderWhenNetworkIsDisabled() {
        val provider = TrailMateLocationProviderPolicy.resolve(
            hasFineLocationPermission = false,
            hasCoarseLocationPermission = true,
            gpsProviderEnabled = true,
            networkProviderEnabled = false
        )

        assertNull(provider)
    }
}
