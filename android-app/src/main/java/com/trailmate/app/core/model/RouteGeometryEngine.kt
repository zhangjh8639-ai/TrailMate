package com.trailmate.app.core.model

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

object RouteGeometryEngine {
    fun projectToRoute(
        route: ImportedRoute,
        latitude: Double,
        longitude: Double,
        horizontalAccuracyMeters: Double,
        timestampEpochMillis: Long
    ): HikeLocationFix {
        val points = route.routePoints
        if (points.size < 2) {
            return HikeLocationFix(
                distanceAlongRouteKm = 0.0,
                crossTrackErrorMeters = NO_ROUTE_GEOMETRY_ERROR_METERS,
                horizontalAccuracyMeters = horizontalAccuracyMeters,
                timestampEpochMillis = timestampEpochMillis
            )
        }

        val best = points.zipWithNext().map { (from, to) ->
            projectToSegment(
                from = from,
                to = to,
                latitude = latitude,
                longitude = longitude
            )
        }.minBy { projection -> projection.crossTrackErrorMeters }

        return HikeLocationFix(
            distanceAlongRouteKm = best.distanceAlongRouteKm.coerceIn(0.0, route.distanceKm),
            crossTrackErrorMeters = best.crossTrackErrorMeters,
            horizontalAccuracyMeters = horizontalAccuracyMeters,
            timestampEpochMillis = timestampEpochMillis
        )
    }

    private fun projectToSegment(
        from: RoutePoint,
        to: RoutePoint,
        latitude: Double,
        longitude: Double
    ): SegmentProjection {
        val referenceLatitude = (from.latitude + to.latitude + latitude) / 3.0
        val segmentX = longitudeMeters(
            longitude = to.longitude,
            originLongitude = from.longitude,
            referenceLatitude = referenceLatitude
        )
        val segmentY = latitudeMeters(latitude = to.latitude, originLatitude = from.latitude)
        val fixX = longitudeMeters(
            longitude = longitude,
            originLongitude = from.longitude,
            referenceLatitude = referenceLatitude
        )
        val fixY = latitudeMeters(latitude = latitude, originLatitude = from.latitude)
        val segmentLengthSquared = segmentX.pow(2) + segmentY.pow(2)
        val projectionFraction = if (segmentLengthSquared == 0.0) {
            0.0
        } else {
            ((fixX * segmentX + fixY * segmentY) / segmentLengthSquared).coerceIn(0.0, 1.0)
        }
        val projectedX = segmentX * projectionFraction
        val projectedY = segmentY * projectionFraction
        val segmentDistanceKm = max(0.0, to.distanceAlongRouteKm - from.distanceAlongRouteKm)

        return SegmentProjection(
            distanceAlongRouteKm = from.distanceAlongRouteKm + segmentDistanceKm * projectionFraction,
            crossTrackErrorMeters = sqrt((fixX - projectedX).pow(2) + (fixY - projectedY).pow(2))
        )
    }

    private fun longitudeMeters(
        longitude: Double,
        originLongitude: Double,
        referenceLatitude: Double
    ): Double =
        Math.toRadians(longitude - originLongitude) *
            EARTH_RADIUS_METERS *
            cos(Math.toRadians(referenceLatitude))

    private fun latitudeMeters(latitude: Double, originLatitude: Double): Double =
        Math.toRadians(latitude - originLatitude) * EARTH_RADIUS_METERS

    private data class SegmentProjection(
        val distanceAlongRouteKm: Double,
        val crossTrackErrorMeters: Double
    )

    private const val EARTH_RADIUS_METERS = 6_371_000.0
    private const val NO_ROUTE_GEOMETRY_ERROR_METERS = 9_999_999.0
}
