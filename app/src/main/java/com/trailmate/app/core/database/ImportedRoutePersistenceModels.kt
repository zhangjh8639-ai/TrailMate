package com.trailmate.app.core.database

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteConfidence
import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteOfflineStatus
import com.trailmate.app.core.model.RouteSourceType

data class ImportedRouteRecord(
    val id: String,
    val fileName: String,
    val sourceType: RouteSourceType,
    val routeName: String,
    val distanceMeters: Double,
    val elevationGainMeters: Double,
    val waypointCount: Int,
    val trackPointCount: Int,
    val hasElevation: Boolean,
    val importedAtEpochMillis: Long,
    val visibility: PrivacyVisibility = PrivacyVisibility.Private,
    val offlineStatus: RouteOfflineStatus = RouteOfflineStatus.TrackOnly,
    val confidence: RouteConfidence = RouteConfidence.Unverified,
    val points: List<ImportedRoutePointRecord>,
) {
    init {
        require(id.isNotBlank()) { "Imported route id must not be blank." }
        require(fileName.isNotBlank()) { "Imported route file name must not be blank." }
        require(routeName.isNotBlank()) { "Imported route name must not be blank." }
        require(distanceMeters >= 0.0) { "Imported route distance must be non-negative." }
        require(waypointCount >= 0) { "Waypoint count must be non-negative." }
        require(trackPointCount >= 0) { "Track point count must be non-negative." }
        require(points.size >= 2) { "Imported route must persist at least two geometry points." }
    }
}

data class ImportedRoutePointRecord(
    val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val cumulativeDistanceMeters: Double,
) {
    init {
        require(pointIndex >= 0) { "Point index must be non-negative." }
        require(cumulativeDistanceMeters >= 0.0) { "Cumulative distance must be non-negative." }
    }
}

object ImportedRouteGeometryRecords {
    fun fromGeometry(geometry: RouteGeometry): List<ImportedRoutePointRecord> =
        geometry.coordinates.mapIndexed { index, coordinate ->
            ImportedRoutePointRecord(
                pointIndex = index,
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                elevationMeters = coordinate.elevation?.meters,
                cumulativeDistanceMeters = geometry.cumulativeDistances[index].meters,
            )
        }

    fun toGeometry(points: List<ImportedRoutePointRecord>): RouteGeometry {
        val orderedPoints = points.sortedBy { it.pointIndex }
        return RouteGeometry(
            coordinates = orderedPoints.map { point ->
                GeoCoordinate(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    elevation = point.elevationMeters?.let(Elevation::meters),
                )
            },
            cumulativeDistances = orderedPoints.map { point ->
                Distance.meters(point.cumulativeDistanceMeters)
            },
        )
    }
}
