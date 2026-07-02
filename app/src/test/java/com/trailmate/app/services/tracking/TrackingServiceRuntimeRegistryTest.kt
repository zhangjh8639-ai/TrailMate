package com.trailmate.app.services.tracking

import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.RouteId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackingServiceRuntimeRegistryTest {
    @Test
    fun markRunningStoresCurrentForegroundSessionSnapshot() {
        val registry = TrackingServiceRuntimeRegistry()
        val request = trackingRequest("session-1", "longjing")

        registry.markRunning(request)

        val snapshot = requireNotNull(registry.snapshot())
        assertEquals(NavigationSessionId("session-1"), snapshot.sessionId)
        assertEquals(RouteId("longjing"), snapshot.routeId)
        assertEquals(1_788_000_000_000, snapshot.startedAtEpochMillis)
        assertEquals(NavigationDirection.Forward, snapshot.direction)
        assertEquals(snapshot, registry.status.value.snapshot)
        assertEquals(1L, registry.status.value.sequence)
    }

    @Test
    fun clearRunningRemovesCurrentForegroundSessionSnapshot() {
        val registry = TrackingServiceRuntimeRegistry()
        registry.markRunning(trackingRequest("session-1", "longjing"))

        registry.clearRunning()

        assertNull(registry.snapshot())
        assertNull(registry.status.value.snapshot)
        assertEquals(2L, registry.status.value.sequence)
    }

    private fun trackingRequest(
        sessionId: String,
        routeId: String,
    ): TrackingServiceStartRequest =
        TrackingServiceStartRequest(
            sessionId = NavigationSessionId(sessionId),
            routeId = RouteId(routeId),
            startedAtEpochMillis = 1_788_000_000_000,
            direction = NavigationDirection.Forward,
        )
}
