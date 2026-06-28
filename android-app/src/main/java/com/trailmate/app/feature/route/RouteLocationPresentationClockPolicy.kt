package com.trailmate.app.feature.route

import com.trailmate.app.core.location.TrailMateLocationStatus
import com.trailmate.app.core.model.TrackRecordingStatus

object RouteLocationPresentationClockPolicy {
    fun shouldRefresh(
        gpsEnabled: Boolean,
        locationStatus: TrailMateLocationStatus,
        trackRecordingStatus: TrackRecordingStatus
    ): Boolean {
        if (!gpsEnabled) {
            return false
        }

        return locationStatus == TrailMateLocationStatus.SEARCHING ||
            locationStatus == TrailMateLocationStatus.LOCATED ||
            locationStatus == TrailMateLocationStatus.LOW_ACCURACY ||
            trackRecordingStatus == TrackRecordingStatus.RECORDING
    }
}
