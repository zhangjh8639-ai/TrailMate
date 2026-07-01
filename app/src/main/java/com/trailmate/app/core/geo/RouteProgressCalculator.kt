package com.trailmate.app.core.geo

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.RouteExitPoint
import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteWaypoint

data class RouteProgress(
    val projection: RouteProjection,
    val completedDistance: Distance,
    val remainingDistance: Distance,
    val remainingElevation: Elevation,
    val nextWaypoint: RouteWaypoint?,
    val nearestExit: RouteExitPoint?,
)

object RouteProgressCalculator {
    fun calculate(
        geometry: RouteGeometry,
        projection: RouteProjection,
        direction: NavigationDirection = NavigationDirection.Forward,
    ): RouteProgress {
        val routeProgressMeters = projection.progress.meters
            .coerceIn(0.0, geometry.totalDistance.meters)
        val completedMeters = when (direction) {
            NavigationDirection.Forward -> routeProgressMeters
            NavigationDirection.Reverse -> geometry.totalDistance.meters - routeProgressMeters
        }.coerceIn(0.0, geometry.totalDistance.meters)
        val remainingMeters = when (direction) {
            NavigationDirection.Forward -> geometry.totalDistance.meters - routeProgressMeters
            NavigationDirection.Reverse -> routeProgressMeters
        }.coerceAtLeast(0.0)

        return RouteProgress(
            projection = projection,
            completedDistance = Distance.meters(completedMeters),
            remainingDistance = Distance.meters(remainingMeters),
            remainingElevation = Elevation.meters(
                calculateRemainingElevationGain(
                    geometry = geometry,
                    routeProgressMeters = routeProgressMeters,
                    direction = direction,
                ),
            ),
            nextWaypoint = selectNextWaypoint(
                geometry = geometry,
                routeProgressMeters = routeProgressMeters,
                direction = direction,
            ),
            nearestExit = selectNearestExit(
                geometry = geometry,
                routeProgressMeters = routeProgressMeters,
            ),
        )
    }

    private fun calculateRemainingElevationGain(
        geometry: RouteGeometry,
        routeProgressMeters: Double,
        direction: NavigationDirection,
    ): Double {
        var remainingGain = 0.0

        geometry.coordinates.zipWithNext().forEachIndexed { index, (start, end) ->
            val startElevation = start.elevation?.meters ?: return@forEachIndexed
            val endElevation = end.elevation?.meters ?: return@forEachIndexed
            val startDistance = geometry.cumulativeDistances[index].meters
            val endDistance = geometry.cumulativeDistances[index + 1].meters

            if (endDistance <= startDistance) return@forEachIndexed

            when (direction) {
                NavigationDirection.Forward -> {
                    if (endDistance <= routeProgressMeters) return@forEachIndexed

                    val effectiveStartElevation = if (routeProgressMeters <= startDistance) {
                        startElevation
                    } else {
                        interpolateElevation(
                            startElevation = startElevation,
                            endElevation = endElevation,
                            startDistance = startDistance,
                            endDistance = endDistance,
                            routeProgressMeters = routeProgressMeters,
                        )
                    }

                    remainingGain += (endElevation - effectiveStartElevation).coerceAtLeast(0.0)
                }
                NavigationDirection.Reverse -> {
                    if (startDistance >= routeProgressMeters) return@forEachIndexed

                    val effectiveStartElevation = if (routeProgressMeters >= endDistance) {
                        endElevation
                    } else {
                        interpolateElevation(
                            startElevation = startElevation,
                            endElevation = endElevation,
                            startDistance = startDistance,
                            endDistance = endDistance,
                            routeProgressMeters = routeProgressMeters,
                        )
                    }

                    remainingGain += (startElevation - effectiveStartElevation).coerceAtLeast(0.0)
                }
            }
        }

        return remainingGain
    }

    private fun interpolateElevation(
        startElevation: Double,
        endElevation: Double,
        startDistance: Double,
        endDistance: Double,
        routeProgressMeters: Double,
    ): Double {
        val ratio = ((routeProgressMeters - startDistance) / (endDistance - startDistance))
            .coerceIn(0.0, 1.0)
        return startElevation + (endElevation - startElevation) * ratio
    }

    private fun selectNextWaypoint(
        geometry: RouteGeometry,
        routeProgressMeters: Double,
        direction: NavigationDirection,
    ): RouteWaypoint? =
        when (direction) {
            NavigationDirection.Forward -> geometry.waypoints
                .filter { it.distanceFromStart.meters > routeProgressMeters }
                .minByOrNull { it.distanceFromStart.meters }
            NavigationDirection.Reverse -> geometry.waypoints
                .filter { it.distanceFromStart.meters < routeProgressMeters }
                .maxByOrNull { it.distanceFromStart.meters }
        }

    private fun selectNearestExit(
        geometry: RouteGeometry,
        routeProgressMeters: Double,
    ): RouteExitPoint? =
        geometry.exitPoints.minByOrNull {
            kotlin.math.abs(it.distanceFromStart.meters - routeProgressMeters)
        }
}
