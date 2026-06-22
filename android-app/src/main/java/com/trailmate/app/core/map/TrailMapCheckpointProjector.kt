package com.trailmate.app.core.map

import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanCheckpointType
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RoutePoint

data class TrailMapCheckpointMarker(
    val type: HikePlanCheckpointType,
    val title: String,
    val distanceKm: Double,
    val note: String,
    val latitude: Double,
    val longitude: Double
)

object TrailMapCheckpointProjector {
    fun project(route: ImportedRoute, plan: HikePlanSummary): List<TrailMapCheckpointMarker> {
        val routePoints = route.routePoints
            .filter { point -> point.latitude.isFinite() && point.longitude.isFinite() }
            .sortedBy(RoutePoint::distanceAlongRouteKm)
        if (routePoints.size < 2) {
            return emptyList()
        }

        return plan.checkpoints.mapNotNull { checkpoint ->
            val projected = routePoints.interpolateAt(checkpoint.distanceKm) ?: return@mapNotNull null
            TrailMapCheckpointMarker(
                type = checkpoint.type,
                title = checkpoint.title,
                distanceKm = checkpoint.distanceKm,
                note = checkpoint.note,
                latitude = projected.latitude,
                longitude = projected.longitude
            )
        }
    }

    private fun List<RoutePoint>.interpolateAt(distanceKm: Double): RoutePoint? {
        val clampedDistance = distanceKm.coerceIn(first().distanceAlongRouteKm, last().distanceAlongRouteKm)
        val nextIndex = indexOfFirst { point -> point.distanceAlongRouteKm >= clampedDistance }
        if (nextIndex <= 0) {
            return first()
        }

        val from = this[nextIndex - 1]
        val to = this[nextIndex]
        val segmentDistance = to.distanceAlongRouteKm - from.distanceAlongRouteKm
        if (segmentDistance <= 0.0) {
            return to
        }

        val progress = (clampedDistance - from.distanceAlongRouteKm) / segmentDistance
        return RoutePoint(
            latitude = from.latitude + (to.latitude - from.latitude) * progress,
            longitude = from.longitude + (to.longitude - from.longitude) * progress,
            elevationMeters = null,
            distanceAlongRouteKm = clampedDistance
        )
    }
}
