package com.trailmate.app.feature.navigation

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingRuntimePermissionsTest {
    @Test
    fun android12RequestIncludesOnlyForegroundLocation() {
        val permissions = TrackingRuntimePermissions.requiredForSdk(32)

        assertEquals(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            permissions,
        )
        assertFalse(permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        assertFalse(permissions.contains(Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun android13PlusRequestIncludesNotificationsButNotBackgroundLocation() {
        val permissions = TrackingRuntimePermissions.requiredForSdk(33)

        assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
        assertTrue(permissions.contains(Manifest.permission.POST_NOTIFICATIONS))
        assertFalse(permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }

    @Test
    fun foregroundLocationGrantedAcceptsFineOrCoarse() {
        assertTrue(
            TrackingRuntimePermissions.hasForegroundLocation(
                mapOf(Manifest.permission.ACCESS_FINE_LOCATION to true),
            ),
        )
        assertTrue(
            TrackingRuntimePermissions.hasForegroundLocation(
                mapOf(Manifest.permission.ACCESS_COARSE_LOCATION to true),
            ),
        )
        assertFalse(
            TrackingRuntimePermissions.hasForegroundLocation(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to false,
                    Manifest.permission.ACCESS_COARSE_LOCATION to false,
                ),
            ),
        )
    }

    @Test
    fun android13GrantResultRequiresNotificationAfterDialog() {
        val result = TrackingRuntimePermissions.grantResult(
            grants = mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to true,
                Manifest.permission.ACCESS_COARSE_LOCATION to true,
                Manifest.permission.POST_NOTIFICATIONS to false,
            ),
            sdkInt = 33,
        )

        assertTrue(result.hasForegroundLocation)
        assertFalse(result.hasNotificationPermission)
        assertFalse(result.canStartTracking)
    }
}
