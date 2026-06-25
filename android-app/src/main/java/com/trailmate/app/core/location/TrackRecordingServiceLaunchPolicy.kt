package com.trailmate.app.core.location

enum class TrackRecordingServiceLaunchMode {
    FOREGROUND_LOCATION_SERVICE,
    SHORT_SERVICE
}

object TrackRecordingServiceLaunchPolicy {
    fun resolve(
        hasPreciseLocationPermission: Boolean,
        gpsProviderEnabled: Boolean
    ): TrackRecordingServiceLaunchMode =
        if (hasPreciseLocationPermission && gpsProviderEnabled) {
            TrackRecordingServiceLaunchMode.FOREGROUND_LOCATION_SERVICE
        } else {
            TrackRecordingServiceLaunchMode.SHORT_SERVICE
        }
}
