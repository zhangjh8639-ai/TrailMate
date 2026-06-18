package com.trailmate.app.feature.home

import androidx.compose.runtime.saveable.SaverScope
import com.trailmate.app.core.gpx.TargetRouteImportQueueState
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
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
            durationMinutes = 185
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
