package com.trailmate.app.core.model

import java.time.Duration
import java.time.Instant

data class EmergencyCard(
    val routeName: String,
    val coordinate: GeoCoordinate,
    val batteryLevel: BatteryLevel,
    val gpsAccuracy: GpsAccuracy,
    val nearestExit: RouteExitPoint?,
    val updatedAt: Instant,
    val contactName: String?,
    val helperText: String = SafetyCopy.emergencyHelperText(),
) {
    init {
        require(routeName.isNotBlank()) { "Route name must not be blank." }
    }
}

object SafetyCopy {
    fun emergencyHelperText(): String = "请将以下信息发送给可信联系人或救援人员。"

    fun confirmedOffRouteGuidance(
        direction: CompassDirection,
        distance: Distance,
    ): String =
        "最近路线点在${direction.label}约${distance.meters.toInt()}m。请结合实际地形返回，不要直线穿越未知区域。"
}

data class TrackPoint(
    val sessionId: NavigationSessionId,
    val coordinate: GeoCoordinate,
    val gpsAccuracy: GpsAccuracy,
    val recordedAt: Instant,
    val batteryLevel: BatteryLevel,
    val speedMetersPerSecond: Double? = null,
    val bearingDegrees: Double? = null,
)

data class RouteRecord(
    val id: String,
    val routeId: RouteId,
    val title: String,
    val completedAt: Instant,
    val actualDistance: Distance,
    val actualElevationGain: Elevation,
    val duration: Duration,
    val offRouteCount: Int,
    val stopCount: Int,
    val maxDeviation: Distance,
    val highestElevation: Elevation,
    val visibility: PrivacyVisibility = PrivacyVisibility.Private,
) {
    init {
        require(id.isNotBlank()) { "Record id must not be blank." }
        require(title.isNotBlank()) { "Record title must not be blank." }
        require(offRouteCount >= 0) { "Off-route count must be non-negative." }
        require(stopCount >= 0) { "Stop count must be non-negative." }
    }

    companion object {
        fun create(
            id: String,
            routeId: RouteId,
            title: String,
            completedAt: Instant,
            actualDistance: Distance,
            actualElevationGain: Elevation,
            duration: Duration,
            offRouteCount: Int,
            stopCount: Int,
            maxDeviation: Distance,
            highestElevation: Elevation,
        ): RouteRecord =
            RouteRecord(
                id = id,
                routeId = routeId,
                title = title,
                completedAt = completedAt,
                actualDistance = actualDistance,
                actualElevationGain = actualElevationGain,
                duration = duration,
                offRouteCount = offRouteCount,
                stopCount = stopCount,
                maxDeviation = maxDeviation,
                highestElevation = highestElevation,
                visibility = PrivacyVisibility.Private,
            )
    }
}

data class RouteFeedback(
    val routeId: RouteId,
    val category: FeedbackCategory,
    val coordinate: GeoCoordinate?,
    val note: String,
    val createdAt: Instant,
    val visibility: PrivacyVisibility = PrivacyVisibility.Private,
) {
    init {
        require(note.isNotBlank()) { "Feedback note must not be blank." }
    }
}

enum class FeedbackCategory {
    Closure,
    Muddy,
    Supply,
    Water,
    Danger,
    BeginnerSuitability,
    Other,
}
