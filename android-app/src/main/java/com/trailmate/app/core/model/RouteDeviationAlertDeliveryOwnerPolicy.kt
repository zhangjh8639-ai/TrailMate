package com.trailmate.app.core.model

object RouteDeviationAlertDeliveryOwnerPolicy {
    fun routeScreenMayDeliver(trackRecordingStatus: TrackRecordingStatus): Boolean =
        trackRecordingStatus != TrackRecordingStatus.RECORDING
}
