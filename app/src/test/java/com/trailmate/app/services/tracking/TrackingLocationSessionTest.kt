package com.trailmate.app.services.tracking

import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.location.LocationProviderObserver
import com.trailmate.app.core.location.LocationProviderRequest
import com.trailmate.app.core.location.LocationProviderStatus
import com.trailmate.app.core.location.LocationSubscription
import com.trailmate.app.core.location.TrailLocationProvider
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingLocationSessionTest {
    @Test
    fun startSubscribesToProviderWithActiveTrackingRequest() {
        val provider = FakeTrailLocationProvider()
        val session = TrackingLocationSession(provider)

        session.start()

        val request = provider.requests.single()
        assertEquals(Duration.ofSeconds(1), request.minTimeInterval)
        assertEquals(Distance.ZERO, request.minDistance)
        assertTrue(request.preferGps)
        assertFalse(provider.subscription.isStopped)
    }

    @Test
    fun duplicateStartWhileActiveDoesNotSubscribeTwice() {
        val provider = FakeTrailLocationProvider()
        val session = TrackingLocationSession(provider)

        session.start()
        session.start()

        assertEquals(1, provider.requests.size)
    }

    @Test
    fun readyStatusCreatesListeningStateWithoutFakeSample() {
        val provider = FakeTrailLocationProvider()
        val session = TrackingLocationSession(provider)

        session.start()
        provider.emitStatus(LocationProviderStatus.Ready)

        assertEquals(TrackingLocationSessionStatus.Listening, session.state.status)
        assertNull(session.state.latestSample)
        assertEquals(0, session.state.sampleCount)
    }

    @Test
    fun realLocationSampleBecomesLatestLocatedState() {
        val provider = FakeTrailLocationProvider()
        val session = TrackingLocationSession(provider)
        val sample = sampleAt("2026-07-01T01:02:03Z")

        session.start()
        provider.emitSample(sample)

        assertEquals(TrackingLocationSessionStatus.Located, session.state.status)
        assertSame(sample, session.state.latestSample)
        assertEquals(1, session.state.sampleCount)
    }

    @Test
    fun unavailableProviderStatusesDoNotCreateTrackPoints() {
        listOf(
            LocationProviderStatus.PermissionDenied to TrackingLocationSessionStatus.PermissionDenied,
            LocationProviderStatus.Disabled to TrackingLocationSessionStatus.Disabled,
            LocationProviderStatus.InvalidReading to TrackingLocationSessionStatus.InvalidReading,
        ).forEach { (providerStatus, expectedStatus) ->
            val provider = FakeTrailLocationProvider()
            val session = TrackingLocationSession(provider)

            session.start()
            provider.emitStatus(providerStatus)

            assertEquals(expectedStatus, session.state.status)
            assertNull(session.state.latestSample)
            assertEquals(0, session.state.sampleCount)
        }
    }

    @Test
    fun disabledOrPermissionDeniedStartupRequiresForegroundShutdown() {
        listOf(
            TrackingLocationSessionStatus.PermissionDenied,
            TrackingLocationSessionStatus.Disabled,
        ).forEach { status ->
            val state = TrackingLocationSessionState(status = status)

            assertTrue(state.requiresTrackingServiceShutdownAfterStart())
        }

        listOf(
            TrackingLocationSessionStatus.Idle,
            TrackingLocationSessionStatus.Listening,
            TrackingLocationSessionStatus.Located,
            TrackingLocationSessionStatus.InvalidReading,
        ).forEach { status ->
            val state = TrackingLocationSessionState(status = status)

            assertFalse(state.requiresTrackingServiceShutdownAfterStart())
        }
    }

    @Test
    fun stopReleasesSubscriptionOnceAndReturnsToIdle() {
        val provider = FakeTrailLocationProvider()
        val session = TrackingLocationSession(provider)

        session.start()
        session.stop()
        session.stop()

        assertEquals(1, provider.subscription.stopCount)
        assertEquals(TrackingLocationSessionStatus.Idle, session.state.status)
        assertNull(session.state.latestSample)
    }

    @Test
    fun callbacksAfterStopAreIgnored() {
        val provider = FakeTrailLocationProvider()
        val session = TrackingLocationSession(provider)

        session.start()
        session.stop()
        provider.emitSample(sampleAt("2026-07-01T01:02:03Z"))
        provider.emitStatus(LocationProviderStatus.Ready)

        assertEquals(TrackingLocationSessionStatus.Idle, session.state.status)
        assertNull(session.state.latestSample)
        assertEquals(0, session.state.sampleCount)
    }

    @Test
    fun startDoesNotFabricateLocationBeforeFirstFix() {
        val provider = FakeTrailLocationProvider()
        val session = TrackingLocationSession(provider)

        session.start()

        assertNull(session.state.latestSample)
        assertEquals(0, session.state.sampleCount)
        assertEquals(1, provider.requests.size)
    }

    private fun sampleAt(instant: String): LocationSample =
        LocationSample(
            coordinate = GeoCoordinate(latitude = 30.245, longitude = 120.116),
            accuracy = GpsAccuracy(6.0),
            recordedAt = Instant.parse(instant),
        )
}

private class FakeTrailLocationProvider : TrailLocationProvider {
    val requests = mutableListOf<LocationProviderRequest>()
    val subscription = FakeLocationSubscription()
    private var observer: LocationProviderObserver? = null

    override fun startLocationUpdates(
        request: LocationProviderRequest,
        observer: LocationProviderObserver,
    ): LocationSubscription {
        requests += request
        this.observer = observer
        return subscription
    }

    fun emitStatus(status: LocationProviderStatus) {
        observer?.onProviderStatus(status)
    }

    fun emitSample(sample: LocationSample) {
        observer?.onLocationSample(sample)
    }
}

private class FakeLocationSubscription : LocationSubscription {
    var stopCount = 0

    override val isStopped: Boolean
        get() = stopCount > 0

    override fun stop() {
        stopCount += 1
    }
}
