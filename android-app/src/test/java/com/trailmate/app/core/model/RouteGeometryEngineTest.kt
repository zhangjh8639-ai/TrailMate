package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGeometryEngineTest {
    @Test
    fun projectsGpsFixOntoNearestRouteSegment() {
        val route = ImportedRoute(
            routeName = "Test Ridge",
            fileName = "test-ridge.gpx",
            distanceKm = 2.2,
            ascentMeters = 0,
            status = RouteImportStatus.PARSED,
            pointCount = 3,
            routePoints = listOf(
                RoutePoint(
                    latitude = 30.0000,
                    longitude = 120.0000,
                    elevationMeters = null,
                    distanceAlongRouteKm = 0.0
                ),
                RoutePoint(
                    latitude = 30.0100,
                    longitude = 120.0000,
                    elevationMeters = null,
                    distanceAlongRouteKm = 1.1
                ),
                RoutePoint(
                    latitude = 30.0200,
                    longitude = 120.0000,
                    elevationMeters = null,
                    distanceAlongRouteKm = 2.2
                )
            )
        )

        val fix = RouteGeometryEngine.projectToRoute(
            route = route,
            latitude = 30.0050,
            longitude = 120.0001,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = 10_000L
        )

        assertEquals(0.6, fix.distanceAlongRouteKm, 0.1)
        assertTrue(fix.crossTrackErrorMeters < 20.0)
        assertEquals(8.0, fix.horizontalAccuracyMeters, 0.001)
        assertEquals(10_000L, fix.timestampEpochMillis)
    }

    @Test
    fun returnsFallbackWhenRouteHasNoGeometry() {
        val route = ImportedRoute(
            routeName = "Summary Only",
            fileName = "summary.gpx",
            distanceKm = 2.2,
            ascentMeters = 0,
            status = RouteImportStatus.PARSED,
            pointCount = 0
        )

        val fix = RouteGeometryEngine.projectToRoute(
            route = route,
            latitude = 30.0050,
            longitude = 120.0001,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = 10_000L
        )

        assertEquals(0.0, fix.distanceAlongRouteKm, 0.001)
        assertTrue(fix.crossTrackErrorMeters > 1_000_000.0)
    }
}
