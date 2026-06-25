package com.trailmate.app.core.location

import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus

enum class TrackRecordingServiceStartAction {
    PUBLISH_AND_START_UPDATES,
    PUBLISH_ONLY,
    STOP_SELF
}

data class TrackRecordingServiceStartDecision(
    val action: TrackRecordingServiceStartAction,
    val trackRecording: TrackRecordingState,
    val notificationCaption: String,
    val shouldStopSelf: Boolean
)

object TrackRecordingServiceStartPolicy {
    fun resolve(
        requestedRouteName: String,
        current: TrackRecordingState,
        hasPreciseLocationPermission: Boolean,
        hasEnabledProvider: Boolean,
        nowEpochMillis: Long,
        requestedRouteKey: String? = null
    ): TrackRecordingServiceStartDecision {
        val hasRequestedRouteName = requestedRouteName.isNotBlank()
        val activeRouteName = requestedRouteName.ifBlank { current.routeName.orEmpty() }
        val activeRouteKey = requestedRouteKey?.takeIf { it.isNotBlank() }
            ?: current.routeKey.takeIf { !hasRequestedRouteName || current.routeName == activeRouteName }
        if (activeRouteName.isBlank()) {
            return TrackRecordingServiceStartDecision(
                action = TrackRecordingServiceStartAction.STOP_SELF,
                trackRecording = current,
                notificationCaption = "",
                shouldStopSelf = true
            )
        }

        if (!hasPreciseLocationPermission) {
            return blockedDecision(
                current = current,
                caption = "需要精确定位权限后才能记录轨迹",
                nowEpochMillis = nowEpochMillis
            )
        }

        if (!hasEnabledProvider) {
            return blockedDecision(
                current = current,
                caption = "系统定位未开启，无法记录轨迹",
                nowEpochMillis = nowEpochMillis
            )
        }

        val updated = when {
            current.status == TrackRecordingStatus.RECORDING &&
                current.routeName == activeRouteName &&
                current.routeKey == activeRouteKey -> current
            current.status == TrackRecordingStatus.PAUSED &&
                current.routeName == activeRouteName &&
                current.routeKey == activeRouteKey ->
                TrackRecordingEngine.resume(current, nowEpochMillis = nowEpochMillis)
            else -> TrackRecordingEngine.start(
                routeName = activeRouteName,
                nowEpochMillis = nowEpochMillis,
                routeKey = activeRouteKey
            )
        }

        return TrackRecordingServiceStartDecision(
            action = TrackRecordingServiceStartAction.PUBLISH_AND_START_UPDATES,
            trackRecording = updated,
            notificationCaption = "正在获取定位",
            shouldStopSelf = false
        )
    }

    fun resolveRuntimeBlock(
        currentBeforeStart: TrackRecordingState,
        notificationCaption: String,
        nowEpochMillis: Long
    ): TrackRecordingServiceStartDecision =
        blockedDecision(
            current = currentBeforeStart,
            caption = notificationCaption,
            nowEpochMillis = nowEpochMillis
        )

    private fun blockedDecision(
        current: TrackRecordingState,
        caption: String,
        nowEpochMillis: Long
    ): TrackRecordingServiceStartDecision {
        val updated = if (current.status == TrackRecordingStatus.RECORDING) {
            TrackRecordingEngine.pause(current, nowEpochMillis = nowEpochMillis)
        } else {
            current
        }

        return TrackRecordingServiceStartDecision(
            action = TrackRecordingServiceStartAction.PUBLISH_ONLY,
            trackRecording = updated,
            notificationCaption = caption,
            shouldStopSelf = true
        )
    }
}
