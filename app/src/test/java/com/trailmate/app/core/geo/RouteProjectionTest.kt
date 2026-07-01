package com.trailmate.app.core.geo

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.RouteGeometry
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteProjectionTest {
    @Test
    fun projectsCoordinateOntoNearestRouteSegment() {
        val projection = RouteProjector.project(
            geometry = straightGeometry(),
            coordinate = GeoCoordinate(latitude = 0.0001, longitude = 0.0005),
        )

        assertEquals(0, projection.segmentIndex)
        assertEquals(50.0, projection.progress.meters, 1.0)
        assertEquals(11.1, projection.distanceFromRoute.meters, 0.6)
        assertEquals(0.0, projection.coordinate.latitude, 0.00001)
        assertEquals(0.0005, projection.coordinate.longitude, 0.00001)
        assertEquals(90.0, projection.segmentBearingDegrees, 1.0)
        assertEquals(180.0, projection.bearingToRouteDegrees, 1.0)
    }

    @Test
    fun previousProgressPreventsJumpOnOverlappingRoute() {
        val projection = RouteProjector.project(
            geometry = outAndBackGeometry(),
            coordinate = GeoCoordinate(latitude = 0.0, longitude = 0.0005),
            previousProgress = Distance.meters(145.0),
        )

        assertEquals(1, projection.segmentIndex)
        assertEquals(150.0, projection.progress.meters, 1.0)
        assertEquals(0.0, projection.distanceFromRoute.meters, 0.1)
    }

    @Test
    fun physicalDistanceWinsWhenPreviousProgressCandidateIsClearlyFarther() {
        val projection = RouteProjector.project(
            geometry = parallelGeometry(),
            coordinate = GeoCoordinate(latitude = 0.0, longitude = 0.0005),
            previousProgress = Distance.meters(150.0),
        )

        assertEquals(0, projection.segmentIndex)
        assertEquals(50.0, projection.progress.meters, 1.0)
        assertEquals(0.0, projection.distanceFromRoute.meters, 0.1)
    }

    private fun straightGeometry(): RouteGeometry =
        RouteGeometry(
            coordinates = listOf(
                GeoCoordinate(latitude = 0.0, longitude = 0.0),
                GeoCoordinate(latitude = 0.0, longitude = 0.001),
                GeoCoordinate(latitude = 0.0, longitude = 0.002),
            ),
            cumulativeDistances = listOf(
                Distance.ZERO,
                Distance.meters(100.0),
                Distance.meters(200.0),
            ),
        )

    private fun outAndBackGeometry(): RouteGeometry =
        RouteGeometry(
            coordinates = listOf(
                GeoCoordinate(latitude = 0.0, longitude = 0.0),
                GeoCoordinate(latitude = 0.0, longitude = 0.001),
                GeoCoordinate(latitude = 0.0, longitude = 0.0),
            ),
            cumulativeDistances = listOf(
                Distance.ZERO,
                Distance.meters(100.0),
                Distance.meters(200.0),
            ),
        )

    private fun parallelGeometry(): RouteGeometry =
        RouteGeometry(
            coordinates = listOf(
                GeoCoordinate(latitude = 0.0, longitude = 0.0),
                GeoCoordinate(latitude = 0.0, longitude = 0.001),
                GeoCoordinate(latitude = 0.00015, longitude = 0.001),
                GeoCoordinate(latitude = 0.00015, longitude = 0.0),
            ),
            cumulativeDistances = listOf(
                Distance.ZERO,
                Distance.meters(100.0),
                Distance.meters(117.0),
                Distance.meters(217.0),
            ),
        )
}
