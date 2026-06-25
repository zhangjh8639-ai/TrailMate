package com.trailmate.app.core.map

import com.trailmate.app.core.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmapOfflineBaseMapTargetHintEngineTest {
    @Test
    fun usesResolvedCityAsRegionLabelWhenAvailable() {
        val hint = AmapOfflineBaseMapTargetHintEngine.build(
            targetRegion = AmapTargetRouteRegion(
                provinceName = "浙江省",
                cityName = "杭州市",
                cityCode = "0571",
                adcode = "330100"
            ),
            routePoints = listOf(routePoint(30.20, 120.10))
        )

        assertEquals("杭州市", hint.regionLabel)
        assertNull(hint.fallbackHint)
    }

    @Test
    fun fallsBackToRepresentativeCoordinateWhenCityLookupFails() {
        val hint = AmapOfflineBaseMapTargetHintEngine.build(
            targetRegion = null,
            routePoints = listOf(
                routePoint(30.10, 120.10),
                routePoint(30.20, 120.20),
                routePoint(30.30, 120.30)
            )
        )

        assertNull(hint.regionLabel)
        assertEquals("路线中点 30.2000, 120.2000（城市待确认）", hint.fallbackHint)
    }

    @Test
    fun hasNoHintWhenRouteHasNoUsableCoordinate() {
        val hint = AmapOfflineBaseMapTargetHintEngine.build(
            targetRegion = null,
            routePoints = listOf(routePoint(999.0, 120.10))
        )

        assertNull(hint.regionLabel)
        assertNull(hint.fallbackHint)
    }

    private fun routePoint(latitude: Double, longitude: Double): RoutePoint =
        RoutePoint(
            latitude = latitude,
            longitude = longitude,
            elevationMeters = null,
            distanceAlongRouteKm = 0.0
        )
}
