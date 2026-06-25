package com.trailmate.app.core.model

object TrackRecordingForegroundRecoveryPolicy {
    fun shouldResumeForegroundService(
        current: TrackRecordingState,
        routeName: String,
        alreadyAttempted: Boolean
    ): Boolean =
        !alreadyAttempted &&
            current.status == TrackRecordingStatus.RECORDING &&
            current.routeName == routeName
}
