package com.trailmate.app.core.location

import android.Manifest

object TrailMateLocationPermissionResult {
    fun isForegroundLocationGranted(grants: Map<String, Boolean>): Boolean =
        grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    fun isPreciseLocationGranted(grants: Map<String, Boolean>): Boolean =
        grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
}
