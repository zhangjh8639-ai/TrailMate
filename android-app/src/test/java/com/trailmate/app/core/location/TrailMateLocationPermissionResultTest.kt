package com.trailmate.app.core.location

import android.Manifest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateLocationPermissionResultTest {
    @Test
    fun coarseOnlyGrantCountsAsForegroundLocationPermission() {
        val grants = mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to false,
            Manifest.permission.ACCESS_COARSE_LOCATION to true
        )

        assertTrue(TrailMateLocationPermissionResult.isForegroundLocationGranted(grants))
    }

    @Test
    fun coarseOnlyGrantDoesNotCountAsPreciseOutdoorLocationPermission() {
        val grants = mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to false,
            Manifest.permission.ACCESS_COARSE_LOCATION to true
        )

        assertFalse(TrailMateLocationPermissionResult.isPreciseLocationGranted(grants))
    }

    @Test
    fun fineGrantCountsAsPreciseOutdoorLocationPermission() {
        val grants = mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to true,
            Manifest.permission.ACCESS_COARSE_LOCATION to true
        )

        assertTrue(TrailMateLocationPermissionResult.isPreciseLocationGranted(grants))
    }

    @Test
    fun deniedFineAndCoarseLocationBlocksForegroundLocation() {
        val grants = mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to false,
            Manifest.permission.ACCESS_COARSE_LOCATION to false
        )

        assertFalse(TrailMateLocationPermissionResult.isForegroundLocationGranted(grants))
    }
}
