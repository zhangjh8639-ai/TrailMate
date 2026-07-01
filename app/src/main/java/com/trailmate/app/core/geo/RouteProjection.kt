package com.trailmate.app.core.geo

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.RouteGeometry
import kotlin.math.abs
import kotlin.math.sqrt

data class RouteProjection(
    val coordinate: GeoCoordinate,
    val segmentIndex: Int,
    val distanceFromRoute: Distance,
    val progress: Distance,
    val segmentBearingDegrees: Double,
    val bearingToRouteDegrees: Double,
)

object RouteProjector {
    private const val SimilarCandidateWindowMeters = 5.0

    fun project(
        geometry: RouteGeometry,
        coordinate: GeoCoordinate,
        previousProgress: Distance? = null,
    ): RouteProjection {
        val candidates = geometry.coordinates
            .zipWithNext()
            .mapIndexed { index, (segmentStart, segmentEnd) ->
                projectOntoSegment(
                    index = index,
                    segmentStart = segmentStart,
                    segmentEnd = segmentEnd,
                    coordinate = coordinate,
                    startDistance = geometry.cumulativeDistances[index],
                    endDistance = geometry.cumulativeDistances[index + 1],
                )
            }

        val nearestDistance = candidates.minOf { it.distanceFromRoute.meters }
        val stableCandidates = if (previousProgress == null) {
            candidates
        } else {
            candidates.filter {
                it.distanceFromRoute.meters <= nearestDistance + SimilarCandidateWindowMeters
            }
        }

        return if (previousProgress == null) {
            stableCandidates.minBy { it.distanceFromRoute.meters }
        } else {
            stableCandidates.minWith(
                compareBy<RouteProjection> { it.distanceFromRoute.meters }
                    .thenBy { abs(it.progress.meters - previousProgress.meters) },
            )
        }
    }

    private fun projectOntoSegment(
        index: Int,
        segmentStart: GeoCoordinate,
        segmentEnd: GeoCoordinate,
        coordinate: GeoCoordinate,
        startDistance: Distance,
        endDistance: Distance,
    ): RouteProjection {
        val segmentEndLocal = GeoMath.toLocalMeters(segmentEnd, segmentStart)
        val pointLocal = GeoMath.toLocalMeters(coordinate, segmentStart)
        val segmentLengthSquared = segmentEndLocal.x * segmentEndLocal.x +
            segmentEndLocal.y * segmentEndLocal.y

        val ratio = if (segmentLengthSquared == 0.0) {
            0.0
        } else {
            ((pointLocal.x * segmentEndLocal.x + pointLocal.y * segmentEndLocal.y) /
                segmentLengthSquared).coerceIn(0.0, 1.0)
        }

        val projectedX = segmentEndLocal.x * ratio
        val projectedY = segmentEndLocal.y * ratio
        val distanceFromRoute = sqrt(
            (pointLocal.x - projectedX) * (pointLocal.x - projectedX) +
                (pointLocal.y - projectedY) * (pointLocal.y - projectedY),
        )
        val segmentDistance = endDistance.meters - startDistance.meters
        val projectedCoordinate = GeoMath.interpolate(segmentStart, segmentEnd, ratio)

        return RouteProjection(
            coordinate = projectedCoordinate,
            segmentIndex = index,
            distanceFromRoute = Distance.meters(distanceFromRoute),
            progress = Distance.meters(startDistance.meters + segmentDistance * ratio),
            segmentBearingDegrees = GeoMath.initialBearingDegrees(segmentStart, segmentEnd),
            bearingToRouteDegrees = GeoMath.initialBearingDegrees(coordinate, projectedCoordinate),
        )
    }
}
