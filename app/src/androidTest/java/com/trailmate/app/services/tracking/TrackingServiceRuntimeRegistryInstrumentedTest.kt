package com.trailmate.app.services.tracking

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.RouteId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackingServiceRuntimeRegistryInstrumentedTest {
    @Test
    fun registryStoresAndClearsRunningSessionOnDeviceRuntime() {
        val registry = TrackingServiceRuntimeRegistry()

        registry.markRunning(
            TrackingServiceStartRequest(
                sessionId = NavigationSessionId("device-session"),
                routeId = RouteId("longjing"),
                startedAtEpochMillis = 1_788_000_000_000,
                direction = NavigationDirection.Forward,
            ),
        )

        assertEquals(NavigationSessionId("device-session"), registry.snapshot()?.sessionId)
        assertEquals(1L, registry.status.value.sequence)

        registry.clearRunning()

        assertNull(registry.snapshot())
        assertEquals(2L, registry.status.value.sequence)
    }
}
