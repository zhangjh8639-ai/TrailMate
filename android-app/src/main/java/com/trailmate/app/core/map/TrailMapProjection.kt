package com.trailmate.app.core.map

import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.RecordedTrackPoint

data class MapScreenPoint(
    val x: Float,
    val y: Float,
    val distanceAlongRouteKm: Double
)

data class TrailMapProjectedLayers(
    val route: List<MapScreenPoint>,
    val recordedTrack: List<MapScreenPoint>
)

object TrailMapProjection {
    fun project(
        routePoints: List<RoutePoint>,
        width: Float,
        height: Float,
        paddingFraction: Float = DEFAULT_PADDING_FRACTION
    ): List<MapScreenPoint> {
        if (routePoints.isEmpty() || width <= 0f || height <= 0f) {
            return emptyList()
        }

        val minLat = routePoints.minOf { it.latitude }
        val maxLat = routePoints.maxOf { it.latitude }
        val minLon = routePoints.minOf { it.longitude }
        val maxLon = routePoints.maxOf { it.longitude }
        val horizontalPadding = width * paddingFraction.coerceIn(0f, 0.45f)
        val verticalPadding = height * paddingFraction.coerceIn(0f, 0.45f)
        val drawableWidth = (width - horizontalPadding * 2).coerceAtLeast(1f)
        val drawableHeight = (height - verticalPadding * 2).coerceAtLeast(1f)
        val latSpan = (maxLat - minLat).takeIf { it > 0.0 }
        val lonSpan = (maxLon - minLon).takeIf { it > 0.0 }

        return routePoints.map { point ->
            val xRatio = lonSpan?.let { (point.longitude - minLon) / it } ?: 0.5
            val yRatio = latSpan?.let { (maxLat - point.latitude) / it } ?: 0.5
            MapScreenPoint(
                x = horizontalPadding + drawableWidth * xRatio.toFloat(),
                y = verticalPadding + drawableHeight * yRatio.toFloat(),
                distanceAlongRouteKm = point.distanceAlongRouteKm
            )
        }
    }

    fun projectLayers(
        routePoints: List<RoutePoint>,
        recordedTrackPoints: List<RecordedTrackPoint>,
        width: Float,
        height: Float,
        paddingFraction: Float = DEFAULT_PADDING_FRACTION
    ): TrailMapProjectedLayers {
        if (width <= 0f || height <= 0f) {
            return TrailMapProjectedLayers(route = emptyList(), recordedTrack = emptyList())
        }
        val routeGeoPoints = routePoints
            .filter { point -> point.latitude.isFinite() && point.longitude.isFinite() }
            .map { point ->
                GeoPoint(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    distanceAlongRouteKm = point.distanceAlongRouteKm
                )
            }
        val trackGeoPoints = recordedTrackPoints
            .filter { point -> point.latitude.isFinite() && point.longitude.isFinite() }
            .mapIndexed { index, point ->
                GeoPoint(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    distanceAlongRouteKm = index.toDouble()
                )
            }
        val bounds = GeoBounds.from(routeGeoPoints + trackGeoPoints)
            ?: return TrailMapProjectedLayers(route = emptyList(), recordedTrack = emptyList())

        return TrailMapProjectedLayers(
            route = projectGeoPoints(
                points = routeGeoPoints,
                bounds = bounds,
                width = width,
                height = height,
                paddingFraction = paddingFraction
            ),
            recordedTrack = projectGeoPoints(
                points = trackGeoPoints,
                bounds = bounds,
                width = width,
                height = height,
                paddingFraction = paddingFraction
            )
        )
    }

    private fun projectGeoPoints(
        points: List<GeoPoint>,
        bounds: GeoBounds,
        width: Float,
        height: Float,
        paddingFraction: Float
    ): List<MapScreenPoint> {
        val horizontalPadding = width * paddingFraction.coerceIn(0f, 0.45f)
        val verticalPadding = height * paddingFraction.coerceIn(0f, 0.45f)
        val drawableWidth = (width - horizontalPadding * 2).coerceAtLeast(1f)
        val drawableHeight = (height - verticalPadding * 2).coerceAtLeast(1f)

        return points.map { point ->
            val xRatio = bounds.lonSpan?.let { (point.longitude - bounds.minLon) / it } ?: 0.5
            val yRatio = bounds.latSpan?.let { (bounds.maxLat - point.latitude) / it } ?: 0.5
            MapScreenPoint(
                x = horizontalPadding + drawableWidth * xRatio.toFloat(),
                y = verticalPadding + drawableHeight * yRatio.toFloat(),
                distanceAlongRouteKm = point.distanceAlongRouteKm
            )
        }
    }

    private const val DEFAULT_PADDING_FRACTION = 0.14f
}

private data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val distanceAlongRouteKm: Double
)

private data class GeoBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {
    val latSpan: Double? = (maxLat - minLat).takeIf { it > 0.0 }
    val lonSpan: Double? = (maxLon - minLon).takeIf { it > 0.0 }

    companion object {
        fun from(points: List<GeoPoint>): GeoBounds? {
            if (points.isEmpty()) {
                return null
            }

            return GeoBounds(
                minLat = points.minOf { it.latitude },
                maxLat = points.maxOf { it.latitude },
                minLon = points.minOf { it.longitude },
                maxLon = points.maxOf { it.longitude }
            )
        }
    }
}
