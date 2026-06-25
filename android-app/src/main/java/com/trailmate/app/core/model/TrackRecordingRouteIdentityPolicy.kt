package com.trailmate.app.core.model

object TrackRecordingRouteIdentityPolicy {
    fun recordingBelongsToRoute(
        trackRecording: TrackRecordingState,
        routeName: String,
        routeKey: String?
    ): Boolean =
        recordingMatchesRoute(
            recordingRouteName = trackRecording.routeName,
            recordingRouteKey = trackRecording.routeKey,
            routeName = routeName,
            routeKey = routeKey
        )

    fun recordingMatchesRoute(
        recordingRouteName: String?,
        recordingRouteKey: String?,
        routeName: String,
        routeKey: String?
    ): Boolean {
        val stableRecordingRouteKey = recordingRouteKey?.takeIf { it.isNotBlank() }
        val stableRouteKey = routeKey?.takeIf { it.isNotBlank() }

        return if (stableRecordingRouteKey != null && stableRouteKey != null) {
            stableRecordingRouteKey == stableRouteKey
        } else {
            recordingRouteName == routeName
        }
    }
}
