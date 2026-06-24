package com.trailmate.app.core.map

import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.TrackRecordingState

data class MapLibreRouteGeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class MapLibreCheckpointOverlay(
    val title: String,
    val distanceKm: Double,
    val note: String,
    val latitude: Double,
    val longitude: Double
)

data class MapLibreRouteOverlay(
    val routePoints: List<MapLibreRouteGeoPoint>,
    val trackPoints: List<MapLibreRouteGeoPoint>,
    val checkpoints: List<MapLibreCheckpointOverlay>
) {
    val hasDrawableRoute: Boolean get() = routePoints.size >= 2
}

object MapLibreRouteOverlayProjector {
    fun project(
        route: ImportedRoute,
        plan: HikePlanSummary,
        trackRecording: TrackRecordingState
    ): MapLibreRouteOverlay =
        MapLibreRouteOverlay(
            routePoints = route.routePoints.mapValidRouteGeoPoints(),
            trackPoints = trackRecording.points.mapValidTrackGeoPoints(),
            checkpoints = TrailMapCheckpointProjector.project(route = route, plan = plan)
                .map { marker ->
                    MapLibreCheckpointOverlay(
                        title = marker.title,
                        distanceKm = marker.distanceKm,
                        note = marker.note,
                        latitude = marker.latitude,
                        longitude = marker.longitude
                    )
                }
        )

    private fun List<RoutePoint>.mapValidRouteGeoPoints(): List<MapLibreRouteGeoPoint> =
        filter { point -> point.latitude.isFinite() && point.longitude.isFinite() }
            .map { point ->
                MapLibreRouteGeoPoint(
                    latitude = point.latitude,
                    longitude = point.longitude
                )
            }

    private fun List<RecordedTrackPoint>.mapValidTrackGeoPoints(): List<MapLibreRouteGeoPoint> =
        filter { point -> point.latitude.isFinite() && point.longitude.isFinite() }
            .map { point ->
                MapLibreRouteGeoPoint(
                    latitude = point.latitude,
                    longitude = point.longitude
                )
            }
}
