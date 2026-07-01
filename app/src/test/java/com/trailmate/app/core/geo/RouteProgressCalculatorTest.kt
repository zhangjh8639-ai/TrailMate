package com.trailmate.app.core.geo

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.ExitPointType
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.RouteExitPoint
import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteWaypoint
import com.trailmate.app.core.model.WaypointType
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteProgressCalculatorTest {
    @Test
    fun calculatesCompletedAndRemainingDistanceFromProjection() {
        val progress = RouteProgressCalculator.calculate(
            geometry = routeWithElevationAndAnchors(),
            projection = projectionAt(Distance.meters(150.0)),
        )

        assertEquals(150.0, progress.completedDistance.meters, 0.1)
        assertEquals(150.0, progress.remainingDistance.meters, 0.1)
    }

    @Test
    fun remainingElevationGainOnlyCountsFutureClimbs() {
        val progress = RouteProgressCalculator.calculate(
            geometry = routeWithElevationAndAnchors(),
            projection = projectionAt(Distance.meters(150.0)),
        )

        assertEquals(50.0, progress.remainingElevation.meters, 0.1)
    }

    @Test
    fun selectsNextWaypointAfterCurrentProgress() {
        val progress = RouteProgressCalculator.calculate(
            geometry = routeWithElevationAndAnchors(),
            projection = projectionAt(Distance.meters(150.0)),
        )

        assertEquals("wp-ridge", progress.nextWaypoint?.id)
    }

    @Test
    fun nearestExitCanBeBehindCurrentProgress() {
        val progress = RouteProgressCalculator.calculate(
            geometry = routeWithElevationAndAnchors(),
            projection = projectionAt(Distance.meters(150.0)),
        )

        assertEquals("exit-behind", progress.nearestExit?.id)
    }

    @Test
    fun reverseDirectionCalculatesRemainingMetricsTowardRouteStart() {
        val progress = RouteProgressCalculator.calculate(
            geometry = routeWithElevationAndAnchors(),
            projection = projectionAt(Distance.meters(150.0)),
            direction = NavigationDirection.Reverse,
        )

        assertEquals(150.0, progress.completedDistance.meters, 0.1)
        assertEquals(150.0, progress.remainingDistance.meters, 0.1)
        assertEquals(5.0, progress.remainingElevation.meters, 0.1)
        assertEquals("wp-done", progress.nextWaypoint?.id)
        assertEquals("exit-behind", progress.nearestExit?.id)
    }

    private fun projectionAt(progress: Distance): RouteProjection =
        RouteProjection(
            coordinate = GeoCoordinate(latitude = 0.0, longitude = 0.0015),
            segmentIndex = 1,
            distanceFromRoute = Distance.ZERO,
            progress = progress,
            segmentBearingDegrees = 90.0,
            bearingToRouteDegrees = 0.0,
        )

    private fun routeWithElevationAndAnchors(): RouteGeometry =
        RouteGeometry(
            coordinates = listOf(
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.0,
                    elevation = Elevation.meters(100.0),
                ),
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.001,
                    elevation = Elevation.meters(130.0),
                ),
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.002,
                    elevation = Elevation.meters(120.0),
                ),
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.003,
                    elevation = Elevation.meters(170.0),
                ),
            ),
            cumulativeDistances = listOf(
                Distance.ZERO,
                Distance.meters(100.0),
                Distance.meters(200.0),
                Distance.meters(300.0),
            ),
            waypoints = listOf(
                RouteWaypoint(
                    id = "wp-done",
                    title = "已通过水源",
                    type = WaypointType.Water,
                    distanceFromStart = Distance.meters(80.0),
                ),
                RouteWaypoint(
                    id = "wp-ridge",
                    title = "山脊检查点",
                    type = WaypointType.Checkpoint,
                    distanceFromStart = Distance.meters(250.0),
                ),
            ),
            exitPoints = listOf(
                RouteExitPoint(
                    id = "exit-behind",
                    title = "身后岔路口",
                    type = ExitPointType.Trailhead,
                    distanceFromStart = Distance.meters(135.0),
                ),
                RouteExitPoint(
                    id = "exit-road",
                    title = "最近公路",
                    type = ExitPointType.RoadAccess,
                    distanceFromStart = Distance.meters(220.0),
                ),
                RouteExitPoint(
                    id = "exit-village",
                    title = "村口",
                    type = ExitPointType.Village,
                    distanceFromStart = Distance.meters(290.0),
                ),
            ),
        )
}
