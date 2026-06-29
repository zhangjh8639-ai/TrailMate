package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationFixReliability

object TrackRecordingRouteMonitorEngine {
    private const val OFF_ROUTE_METERS = 75.0

    fun evaluate(
        route: ImportedRoute?,
        recordingRouteName: String?,
        recordingRouteKey: String? = null,
        point: RecordedTrackPoint,
        state: RouteDeviationAlertState,
        nowEpochMillis: Long
    ): RouteDeviationAlertDecision {
        if (route == null || !route.matchesRecording(recordingRouteName, recordingRouteKey) || route.routePoints.size < 2) {
            return none()
        }

        if (!point.isFresh(nowEpochMillis)) {
            return RouteDeviationAlertPolicy.evaluate(
                status = LocationBackedHikeStatus.CHECK_ROUTE,
                fix = null,
                state = state,
                nowEpochMillis = nowEpochMillis
            )
        }

        val fix = RouteGeometryEngine.projectToRoute(
            route = route,
            latitude = point.latitude,
            longitude = point.longitude,
            horizontalAccuracyMeters = point.horizontalAccuracyMeters,
            timestampEpochMillis = point.timestampEpochMillis
        )
        val status = if (fix.crossTrackErrorMeters > OFF_ROUTE_METERS) {
            LocationBackedHikeStatus.CHECK_ROUTE
        } else {
            LocationBackedHikeStatus.ON_ROUTE
        }

        return RouteDeviationAlertPolicy.evaluate(
            status = status,
            fix = fix,
            state = state,
            nowEpochMillis = nowEpochMillis
        )
    }

    private fun ImportedRoute.matchesRecording(
        recordingRouteName: String?,
        recordingRouteKey: String?
    ): Boolean {
        val stableRouteKey = recordingRouteKey?.takeIf { it.isNotBlank() }
        return TrackRecordingRouteIdentityPolicy.recordingMatchesRoute(
            recordingRouteName = recordingRouteName,
            recordingRouteKey = stableRouteKey,
            routeName = routeName,
            routeKey = offlineRoutePackKey()
        )
    }

    private fun RecordedTrackPoint.isFresh(nowEpochMillis: Long): Boolean =
        timestampEpochMillis > 0L &&
            timestampEpochMillis <= nowEpochMillis &&
            nowEpochMillis - timestampEpochMillis <=
            TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS

    private fun none(): RouteDeviationAlertDecision =
        RouteDeviationAlertDecision(
            kind = RouteDeviationAlertKind.NONE,
            shouldNotify = false,
            shouldVibrate = false,
            title = "",
            caption = "",
            primaryActionLabel = "",
            nextState = RouteDeviationAlertState()
        )
}
