package com.trailmate.app.core.model

object TrackRecordingForegroundRecoveryPolicy {
    fun shouldResumeForegroundService(
        current: TrackRecordingState,
        routeName: String,
        routeKey: String? = null,
        alreadyAttempted: Boolean
    ): Boolean =
        !alreadyAttempted &&
            current.status == TrackRecordingStatus.RECORDING &&
            TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
                trackRecording = current,
                routeName = routeName,
                routeKey = routeKey
            )
}
