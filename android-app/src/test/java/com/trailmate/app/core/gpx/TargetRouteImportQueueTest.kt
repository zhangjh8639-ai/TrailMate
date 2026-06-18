package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetRouteImportQueueTest {
    @Test
    fun failedImportKeepsCurrentRouteAndOffersRetry() {
        val currentRoute = TrailMateSampleData.importedTargetRoute
        val queue = TargetRouteImportQueueState
            .fromRoute(currentRoute)
            .start(fileName = "bad-route.gpx")
            .complete(
                TargetRouteImportState.Failed(
                    fileName = "bad-route.gpx",
                    message = "GPX route must contain at least two track or route points."
                )
            )

        val summary = queue.summary()

        assertEquals(currentRoute, queue.lastImportedRoute)
        assertTrue(queue.canRetry)
        assertEquals("Retry available", summary.value)
        assertTrue(summary.caption.contains("bad-route.gpx"))
        assertTrue(summary.caption.contains("Keeping Longjing Ridge"))
    }

    @Test
    fun successfulImportClearsFailureAndUpdatesLastImportedRoute() {
        val failedQueue = TargetRouteImportQueueState
            .fromRoute(TrailMateSampleData.importedTargetRoute)
            .complete(TargetRouteImportState.Failed(fileName = "bad-route.gpx", message = "Empty route."))

        val replacedRoute = TrailMateSampleData.importedTargetRoute.copy(
            routeName = "Replacement Loop",
            fileName = "replacement-loop.gpx",
            distanceKm = 8.4,
            ascentMeters = 420,
            pointCount = 24
        )
        val importedQueue = failedQueue.complete(TargetRouteImportState.Imported(replacedRoute))

        val summary = importedQueue.summary()

        assertEquals(replacedRoute, importedQueue.lastImportedRoute)
        assertFalse(importedQueue.canRetry)
        assertEquals("Parsed", summary.value)
        assertEquals("replacement-loop.gpx / 8.4 km / +420 m / 24 points", summary.caption)
    }
}
