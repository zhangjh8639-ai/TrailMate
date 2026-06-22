package com.trailmate.app.core.map

import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RoutePoint
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AmapCameraRouteKeyFactoryTest {
    @Test
    fun changesKeyWhenImportedRouteGeometryChangesEvenIfSummaryMatches() {
        val original = routeWithGeometry(
            firstLatitude = 30.10,
            firstLongitude = 120.10,
            lastLatitude = 30.20,
            lastLongitude = 120.20
        )
        val revised = routeWithGeometry(
            firstLatitude = 30.40,
            firstLongitude = 120.40,
            lastLatitude = 30.50,
            lastLongitude = 120.50
        )

        assertNotEquals(
            AmapCameraRouteKeyFactory.build(original),
            AmapCameraRouteKeyFactory.build(revised)
        )
    }

    private fun routeWithGeometry(
        firstLatitude: Double,
        firstLongitude: Double,
        lastLatitude: Double,
        lastLongitude: Double
    ): ImportedRoute =
        ImportedRoute(
            routeName = "龙井山脊",
            fileName = "longjing.gpx",
            distanceKm = 15.2,
            ascentMeters = 860,
            status = RouteImportStatus.PARSED,
            pointCount = 2,
            routePoints = listOf(
                RoutePoint(
                    latitude = firstLatitude,
                    longitude = firstLongitude,
                    elevationMeters = 120.0,
                    distanceAlongRouteKm = 0.0
                ),
                RoutePoint(
                    latitude = lastLatitude,
                    longitude = lastLongitude,
                    elevationMeters = 860.0,
                    distanceAlongRouteKm = 15.2
                )
            )
        )
}
