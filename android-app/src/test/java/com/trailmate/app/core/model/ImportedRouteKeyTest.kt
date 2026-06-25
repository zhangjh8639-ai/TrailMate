package com.trailmate.app.core.model

import org.junit.Assert.assertNotEquals
import org.junit.Test

class ImportedRouteKeyTest {
    @Test
    fun offlineRoutePackKeyIncludesRouteGeometryFingerprint() {
        val first = route(
            routePoints = listOf(
                routePoint(latitude = 30.0000, longitude = 120.0000, distanceAlongRouteKm = 0.0),
                routePoint(latitude = 30.0100, longitude = 120.0000, distanceAlongRouteKm = 1.1)
            )
        )
        val replacement = route(
            routePoints = listOf(
                routePoint(latitude = 30.0000, longitude = 120.0000, distanceAlongRouteKm = 0.0),
                routePoint(latitude = 30.0100, longitude = 120.0100, distanceAlongRouteKm = 1.1)
            )
        )

        assertNotEquals(first.offlineRoutePackKey(), replacement.offlineRoutePackKey())
    }

    private fun route(routePoints: List<RoutePoint>): ImportedRoute =
        ImportedRoute(
            routeName = "龙井山脊",
            fileName = "longjing.gpx",
            distanceKm = 15.2,
            ascentMeters = 860,
            status = RouteImportStatus.PARSED,
            pointCount = routePoints.size,
            routePoints = routePoints
        )

    private fun routePoint(
        latitude: Double,
        longitude: Double,
        distanceAlongRouteKm: Double
    ): RoutePoint =
        RoutePoint(
            latitude = latitude,
            longitude = longitude,
            elevationMeters = null,
            distanceAlongRouteKm = distanceAlongRouteKm
        )
}
