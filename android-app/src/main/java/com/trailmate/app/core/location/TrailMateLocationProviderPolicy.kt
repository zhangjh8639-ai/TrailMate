package com.trailmate.app.core.location

import android.location.LocationManager

enum class TrailMateLocationProvider {
    GPS
}

object TrailMateLocationProviderPolicy {
    @Suppress("UNUSED_PARAMETER")
    fun resolve(
        hasFineLocationPermission: Boolean,
        hasCoarseLocationPermission: Boolean,
        gpsProviderEnabled: Boolean,
        networkProviderEnabled: Boolean
    ): TrailMateLocationProvider? {
        if (!hasFineLocationPermission) {
            return null
        }
        if (hasFineLocationPermission && gpsProviderEnabled) {
            return TrailMateLocationProvider.GPS
        }

        return null
    }
}

fun TrailMateLocationProvider.toAndroidProviderName(): String =
    when (this) {
        TrailMateLocationProvider.GPS -> LocationManager.GPS_PROVIDER
    }
