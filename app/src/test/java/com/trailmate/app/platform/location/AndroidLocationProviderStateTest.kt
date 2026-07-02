package com.trailmate.app.platform.location

import com.trailmate.app.core.location.LocationProviderStatus
import com.trailmate.app.core.location.RecordingLocationObserver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidLocationProviderStateTest {
    @Test
    fun stopSuppressesFutureLocationCallbacks() {
        val state = AndroidLocationProviderState(
            subscribedProviders = setOf("gps", "network"),
        )

        assertTrue(state.shouldForwardLocation())

        state.stop()
        state.stop()

        assertTrue(state.isStopped)
        assertFalse(state.shouldForwardLocation())
    }

    @Test
    fun disabledStatusIsReportedOnlyAfterAllSubscribedProvidersAreUnavailable() {
        val observer = RecordingLocationObserver()
        val state = AndroidLocationProviderState(
            subscribedProviders = setOf("gps", "network"),
        )

        state.markProviderDisabled("gps", observer)

        assertTrue(observer.statuses.isEmpty())

        state.markProviderDisabled("network", observer)

        assertEquals(listOf(LocationProviderStatus.Disabled), observer.statuses)
        assertFalse(state.shouldForwardLocation())
    }

    @Test
    fun providerEnabledAfterDisabledReportsReadyAgain() {
        val observer = RecordingLocationObserver()
        val state = AndroidLocationProviderState(
            subscribedProviders = setOf("gps"),
        )

        state.markProviderDisabled("gps", observer)
        state.markProviderEnabled("gps", observer)

        assertEquals(
            listOf(
                LocationProviderStatus.Disabled,
                LocationProviderStatus.Ready,
            ),
            observer.statuses,
        )
        assertTrue(state.shouldForwardLocation())
    }
}
