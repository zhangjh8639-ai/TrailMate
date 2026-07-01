package com.trailmate.app.core.routeimport

import com.trailmate.app.core.geo.GeoDistance
import com.trailmate.app.core.geo.RouteProjector
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteWaypoint
import com.trailmate.app.core.model.WaypointType

data class RouteImportOptions(
    val minReliableTrackPoints: Int = 10,
    val maxStableSegmentGap: Distance = Distance.meters(500.0),
) {
    init {
        require(minReliableTrackPoints >= 2) {
            "Minimum reliable track points must be at least 2."
        }
    }
}

internal data class ImportedWaypointCandidate(
    val title: String,
    val coordinate: GeoCoordinate,
)

internal data class GeometryBuildResult(
    val geometry: RouteGeometry?,
    val warnings: Set<RouteImportWarning>,
)

internal object RouteGeometryBuilder {
    fun build(
        coordinates: List<GeoCoordinate>,
        waypointCandidates: List<ImportedWaypointCandidate>,
        options: RouteImportOptions,
    ): GeometryBuildResult {
        if (coordinates.size < 2) {
            return GeometryBuildResult(
                geometry = null,
                warnings = setOf(RouteImportWarning.MissingTrackGeometry),
            )
        }

        val warnings = mutableSetOf<RouteImportWarning>()
        if (coordinates.size < options.minReliableTrackPoints) {
            warnings += RouteImportWarning.SparseTrack
        }
        if (coordinates.none { it.elevation != null }) {
            warnings += RouteImportWarning.MissingElevation
        }

        val cumulativeDistances = mutableListOf(Distance.ZERO)
        var totalMeters = 0.0
        coordinates.zipWithNext().forEach { (from, to) ->
            val segmentDistance = GeoDistance.between(from, to)
            if (segmentDistance.meters > options.maxStableSegmentGap.meters) {
                warnings += RouteImportWarning.LargePointGap
            }
            totalMeters += segmentDistance.meters
            cumulativeDistances += Distance.meters(totalMeters)
        }

        val baseGeometry = RouteGeometry(
            coordinates = coordinates,
            cumulativeDistances = cumulativeDistances,
        )
        val waypoints = waypointCandidates.mapIndexed { index, waypoint ->
            RouteWaypoint(
                id = "import-waypoint-${index + 1}",
                title = waypoint.title.ifBlank { "航点 ${index + 1}" },
                type = WaypointType.Checkpoint,
                distanceFromStart = RouteProjector
                    .project(baseGeometry, waypoint.coordinate)
                    .progress,
            )
        }

        return GeometryBuildResult(
            geometry = baseGeometry.copy(waypoints = waypoints),
            warnings = warnings,
        )
    }
}

internal fun coordinateOf(
    latitudeText: String?,
    longitudeText: String?,
    elevationText: String?,
): GeoCoordinate? {
    val latitude = latitudeText?.toDoubleOrNull() ?: return null
    val longitude = longitudeText?.toDoubleOrNull() ?: return null
    val elevation = elevationText
        ?.toDoubleOrNull()
        ?.takeIf { it.isFinite() }
        ?.let { Elevation.meters(it) }

    return runCatching {
        GeoCoordinate(
            latitude = latitude,
            longitude = longitude,
            elevation = elevation,
        )
    }.getOrNull()
}
