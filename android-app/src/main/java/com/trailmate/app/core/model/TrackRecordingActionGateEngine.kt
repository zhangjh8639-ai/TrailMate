package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationFixReliability

enum class TrackRecordingActionGateStep {
    REQUEST_FOREGROUND_LOCATION,
    REQUEST_NOTIFICATION,
    WAIT_FOR_RELIABLE_LOCATION,
    APPLY_TRACK_ACTION
}

object TrackRecordingActionGateEngine {
    fun resolve(
        status: TrackRecordingStatus,
        hasForegroundLocationPermission: Boolean,
        notificationPermissionGranted: Boolean,
        locationSnapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): TrackRecordingActionGateStep {
        if (status == TrackRecordingStatus.RECORDING) {
            return TrackRecordingActionGateStep.APPLY_TRACK_ACTION
        }
        if (!hasForegroundLocationPermission) {
            return TrackRecordingActionGateStep.REQUEST_FOREGROUND_LOCATION
        }
        if (!notificationPermissionGranted) {
            return TrackRecordingActionGateStep.REQUEST_NOTIFICATION
        }
        if (!locationSnapshot.isReliableForRecordingStart(nowEpochMillis)) {
            return TrackRecordingActionGateStep.WAIT_FOR_RELIABLE_LOCATION
        }

        return TrackRecordingActionGateStep.APPLY_TRACK_ACTION
    }

    fun primaryActionLabel(
        status: TrackRecordingStatus,
        step: TrackRecordingActionGateStep
    ): String =
        when {
            step == TrackRecordingActionGateStep.REQUEST_FOREGROUND_LOCATION -> "授权定位"
            step == TrackRecordingActionGateStep.REQUEST_NOTIFICATION -> "允许通知"
            step == TrackRecordingActionGateStep.WAIT_FOR_RELIABLE_LOCATION -> "等待定位稳定"
            status == TrackRecordingStatus.RECORDING -> "暂停记录"
            status == TrackRecordingStatus.PAUSED -> "继续记录"
            else -> "开始记录"
        }

    private fun TrailMateLocationSnapshot.isReliableForRecordingStart(nowEpochMillis: Long): Boolean =
        TrailMateLocationFixReliability.isReliableForFieldUse(
            snapshot = this,
            nowEpochMillis = nowEpochMillis,
            maxAccuracyMeters = MAX_RECORDING_START_ACCURACY_METERS
        )

    private const val MAX_RECORDING_START_ACCURACY_METERS = 50.0
}
