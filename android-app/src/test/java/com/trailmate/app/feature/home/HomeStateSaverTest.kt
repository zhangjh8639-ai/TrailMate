package com.trailmate.app.feature.home

import androidx.compose.runtime.saveable.SaverScope
import com.trailmate.app.core.gpx.TargetRouteImportQueueState
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HomeStateSaverTest {
    @Test
    fun targetRouteImportQueueSaverPreservesRouteDuration() {
        val route = ImportedRoute(
            routeName = "Timed Ridge",
            fileName = "timed-ridge.gpx",
            distanceKm = 12.4,
            ascentMeters = 780,
            status = RouteImportStatus.PARSED,
            pointCount = 120,
            durationMinutes = 185,
            routePoints = listOf(
                RoutePoint(
                    latitude = 30.0,
                    longitude = 120.0,
                    elevationMeters = 100.0,
                    distanceAlongRouteKm = 0.0
                ),
                RoutePoint(
                    latitude = 30.01,
                    longitude = 120.0,
                    elevationMeters = 180.0,
                    distanceAlongRouteKm = 1.1
                )
            )
        )
        val queue = TargetRouteImportQueueState.fromRoute(route)

        val saved = with(TargetRouteImportQueueStateSaver) {
            AllowAllSaverScope.save(queue)
        }
        val restored = TargetRouteImportQueueStateSaver.restore(saved!!)

        assertNotNull(restored)
        assertEquals(route, restored?.lastImportedRoute)
    }
}

private object AllowAllSaverScope : SaverScope {
    override fun canBeSaved(value: Any): Boolean = true
}
