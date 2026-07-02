package com.trailmate.app.core.database

import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteId

data class TrackingSessionRecord(
    val sessionId: NavigationSessionId,
    val routeId: RouteId,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long? = null,
    val state: NavigationState,
    val direction: NavigationDirection,
    val visibility: PrivacyVisibility = PrivacyVisibility.Private,
    val sampleCount: Int = 0,
) {
    init {
        require(sampleCount >= 0) { "Tracking session sample count must be non-negative." }
    }

    companion object {
        fun fromSession(session: NavigationSession): TrackingSessionRecord =
            TrackingSessionRecord(
                sessionId = session.id,
                routeId = session.routeId,
                startedAtEpochMillis = session.startedAt.toEpochMilli(),
                state = session.state,
                direction = session.direction,
                visibility = session.visibility,
                sampleCount = 0,
            )
    }
}

data class TrackingTrackPointRecord(
    val sessionId: NavigationSessionId,
    val pointIndex: Int,
    val coordinate: GeoCoordinate,
    val accuracy: GpsAccuracy,
    val recordedAtEpochMillis: Long,
    val bearingDegrees: Double?,
    val speedMetersPerSecond: Double?,
) {
    init {
        require(pointIndex >= 0) { "Track point index must be non-negative." }
    }

    companion object {
        fun fromSample(
            sessionId: NavigationSessionId,
            pointIndex: Int,
            sample: LocationSample,
        ): TrackingTrackPointRecord =
            TrackingTrackPointRecord(
                sessionId = sessionId,
                pointIndex = pointIndex,
                coordinate = sample.coordinate,
                accuracy = sample.accuracy,
                recordedAtEpochMillis = sample.recordedAt.toEpochMilli(),
                bearingDegrees = sample.bearingDegrees,
                speedMetersPerSecond = sample.speedMetersPerSecond,
            )
    }
}
