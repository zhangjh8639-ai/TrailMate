package com.trailmate.app.core.model

import java.time.Duration
import java.time.Instant

enum class PrivacyVisibility {
    Private,
    Shared,
    Public,
}

enum class RouteSourceType {
    Platform,
    ImportedGpx,
    ImportedKml,
}

enum class RouteOfflineStatus {
    None,
    TrackOnly,
    Verified,
}

enum class RouteDifficulty {
    Easy,
    EasyModerate,
    Moderate,
    Hard,
}

enum class RouteConfidence {
    A,
    B,
    C,
    Unverified,
}

data class TrailRoute(
    val id: RouteId,
    val name: String,
    val region: String,
    val routeType: String,
    val geometry: RouteGeometry,
    val estimatedDuration: Duration,
    val difficulty: RouteDifficulty,
    val confidence: RouteConfidence,
    val routeVersion: String,
    val lastUpdated: Instant,
    val sourceType: RouteSourceType,
    val offlineStatus: RouteOfflineStatus,
    val visibility: PrivacyVisibility = PrivacyVisibility.Private,
    val riskTags: List<String> = emptyList(),
    val recentFeedback: List<String> = emptyList(),
) {
    val distance: Distance = geometry.totalDistance
    val elevationGain: Elevation = geometry.elevationGain

    init {
        require(name.isNotBlank()) { "Route name must not be blank." }
        require(region.isNotBlank()) { "Route region must not be blank." }
        require(routeType.isNotBlank()) { "Route type must not be blank." }
    }

    companion object {
        fun imported(
            id: RouteId,
            name: String,
            region: String,
            geometry: RouteGeometry,
            importedAt: Instant,
            sourceType: RouteSourceType = RouteSourceType.ImportedGpx,
        ): TrailRoute =
            when (sourceType) {
                RouteSourceType.ImportedGpx,
                RouteSourceType.ImportedKml -> TrailRoute(
                    id = id,
                    name = name,
                    region = region,
                    routeType = when (sourceType) {
                        RouteSourceType.ImportedKml -> "KML 导入"
                        else -> "GPX 导入"
                    },
                    geometry = geometry,
                    estimatedDuration = Duration.ZERO,
                    difficulty = RouteDifficulty.Moderate,
                    confidence = RouteConfidence.Unverified,
                    routeVersion = "imported",
                    lastUpdated = importedAt,
                    sourceType = sourceType,
                    offlineStatus = RouteOfflineStatus.TrackOnly,
                    visibility = PrivacyVisibility.Private,
                    riskTags = listOf("未验证"),
                )
                RouteSourceType.Platform -> error("Imported route source must be GPX or KML.")
            }

        fun platform(
            id: RouteId,
            name: String,
            region: String,
            routeType: String,
            geometry: RouteGeometry,
            estimatedDuration: Duration,
            difficulty: RouteDifficulty,
            confidence: RouteConfidence,
            routeVersion: String,
            lastUpdated: Instant,
            riskTags: List<String>,
            recentFeedback: List<String>,
        ): TrailRoute =
            TrailRoute(
                id = id,
                name = name,
                region = region,
                routeType = routeType,
                geometry = geometry,
                estimatedDuration = estimatedDuration,
                difficulty = difficulty,
                confidence = confidence,
                routeVersion = routeVersion,
                lastUpdated = lastUpdated,
                sourceType = RouteSourceType.Platform,
                offlineStatus = RouteOfflineStatus.Verified,
                visibility = PrivacyVisibility.Private,
                riskTags = riskTags,
                recentFeedback = recentFeedback,
            )
    }
}

data class RouteGeometry(
    val coordinates: List<GeoCoordinate>,
    val cumulativeDistances: List<Distance>,
    val waypoints: List<RouteWaypoint> = emptyList(),
    val riskPoints: List<RouteRiskPoint> = emptyList(),
    val exitPoints: List<RouteExitPoint> = emptyList(),
) {
    init {
        require(coordinates.size >= 2) { "Route geometry needs at least two coordinates." }
        require(coordinates.size == cumulativeDistances.size) {
            "Cumulative distances must match coordinate count."
        }
        require(cumulativeDistances.first() == Distance.ZERO) {
            "Cumulative distances must start at zero."
        }
        require(cumulativeDistances.zipWithNext().all { (previous, next) -> next.meters >= previous.meters }) {
            "Cumulative distances must be monotonic."
        }
        val totalDistance = cumulativeDistances.last()
        val annotatedDistances = waypoints.map { it.distanceFromStart } +
            riskPoints.map { it.distanceFromStart } +
            exitPoints.map { it.distanceFromStart }
        require(annotatedDistances.all { it.meters <= totalDistance.meters }) {
            "Route annotations must be within route bounds."
        }
    }

    val totalDistance: Distance = cumulativeDistances.last()
    val hasElevation: Boolean = coordinates.any { it.elevation != null }
    val elevationGain: Elevation = Elevation.meters(calculateElevationGain())

    private fun calculateElevationGain(): Double {
        val elevations = coordinates.mapNotNull { it.elevation?.meters }
        if (elevations.size < 2) return 0.0

        return elevations
            .zipWithNext()
            .sumOf { (previous, next) -> (next - previous).coerceAtLeast(0.0) }
    }
}

data class RouteWaypoint(
    val id: String,
    val title: String,
    val type: WaypointType,
    val distanceFromStart: Distance,
)

enum class WaypointType {
    Start,
    End,
    Checkpoint,
    Water,
    Supply,
    Viewpoint,
}

data class RouteRiskPoint(
    val id: String,
    val title: String,
    val type: RiskPointType,
    val distanceFromStart: Distance,
)

enum class RiskPointType {
    Slippery,
    Fork,
    WeakSignal,
    Exposed,
    Closure,
}

data class RouteExitPoint(
    val id: String,
    val title: String,
    val type: ExitPointType,
    val distanceFromStart: Distance,
)

enum class ExitPointType {
    RoadAccess,
    Village,
    Transit,
    Trailhead,
}
