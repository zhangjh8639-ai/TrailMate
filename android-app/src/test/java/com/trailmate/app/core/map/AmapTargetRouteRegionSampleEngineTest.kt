package com.trailmate.app.core.map

import com.trailmate.app.core.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmapTargetRouteRegionSampleEngineTest {
    @Test
    fun choosesMiddleUsableRoutePointForReverseGeocoding() {
        val sample = AmapTargetRouteRegionSampleEngine.representativePoint(
            listOf(
                routePoint(latitude = 30.10, longitude = 120.10),
                routePoint(latitude = 30.20, longitude = 120.20),
                routePoint(latitude = 30.30, longitude = 120.30),
                routePoint(latitude = 30.40, longitude = 120.40)
            )
        )

        assertEquals(30.30, sample?.latitude ?: 0.0, 0.0001)
        assertEquals(120.30, sample?.longitude ?: 0.0, 0.0001)
    }

    @Test
    fun skipsInvalidCoordinatesBeforeChoosingRepresentativePoint() {
        val sample = AmapTargetRouteRegionSampleEngine.representativePoint(
            listOf(
                routePoint(latitude = 999.0, longitude = 120.10),
                routePoint(latitude = 30.20, longitude = 120.20),
                routePoint(latitude = 30.30, longitude = 120.30)
            )
        )

        assertEquals(30.30, sample?.latitude ?: 0.0, 0.0001)
        assertEquals(120.30, sample?.longitude ?: 0.0, 0.0001)
    }

    @Test
    fun returnsNullWhenRouteHasNoUsableCoordinates() {
        val sample = AmapTargetRouteRegionSampleEngine.representativePoint(
            listOf(
                routePoint(latitude = 999.0, longitude = 120.10),
                routePoint(latitude = 30.20, longitude = 999.0)
            )
        )

        assertNull(sample)
    }

    private fun routePoint(latitude: Double, longitude: Double): RoutePoint =
        RoutePoint(
            latitude = latitude,
            longitude = longitude,
            elevationMeters = null,
            distanceAlongRouteKm = 0.0
        )
}
