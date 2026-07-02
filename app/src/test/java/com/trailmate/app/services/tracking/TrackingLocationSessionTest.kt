package com.trailmate.app.services.tracking

import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.location.LocationProviderObserver
import com.trailmate.app.core.location.LocationProviderRequest
import com.trailmate.app.core.location.LocationProviderStatus
import com.trailmate.app.core.location.LocationSubscription
import com.trailmate.app.core.location.TrailLocationProvider
import com.trailmate.app.core.database.TrackingRecordingStore
import com.trailmate.app.core.database.TrackingSessionRecord
import com.trailmate.app.core.database.TrackingTrackPointRecord
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationEvent
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteId
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

    @Test
    fun recordingContextPersistsSessionButNoPointBeforeFirstFix() {
        val provider = FakeTrailLocationProvider()
        val store = FakeTrackingRecordingStore()
        val model = navigationSession()
        val session = TrackingLocationSession(
            locationProvider = provider,
            recordingContext = TrackingRecordingContext(
                session = model,
                store = store,
                clock = { Instant.parse("2026-07-01T02:02:03Z") },
            ),
        )

        session.start()
        provider.emitStatus(LocationProviderStatus.Ready)

        val persisted = store.sessions.single()
        assertEquals(model.id, persisted.sessionId)
        assertEquals(RouteId("longjing"), persisted.routeId)
        assertEquals(PrivacyVisibility.Private, persisted.visibility)
        assertEquals(0, persisted.sampleCount)
        assertTrue(store.points.isEmpty())
    }

    @Test
    fun recordingContextAppendsOnlyRealProviderSamplesWithStableIndexes() {
        val provider = FakeTrailLocationProvider()
        val store = FakeTrackingRecordingStore()
        val session = TrackingLocationSession(
            locationProvider = provider,
            recordingContext = TrackingRecordingContext(
                session = navigationSession(),
                store = store,
                clock = { Instant.parse("2026-07-01T02:02:03Z") },
            ),
        )

        session.start()
        provider.emitStatus(LocationProviderStatus.Ready)
        provider.emitSample(sampleAt("2026-07-01T01:02:04Z"))
        provider.emitSample(sampleAt("2026-07-01T01:02:05Z"))

        assertEquals(listOf(0, 1), store.points.map { it.pointIndex })
        assertEquals(2, session.state.sampleCount)
        assertEquals(2, store.sessions.last().sampleCount)
    }

    @Test
    fun recordingContextUsesStoreAssignedIndexesWhenResumingExistingSession() {
        val provider = FakeTrailLocationProvider()
        val store = FakeTrackingRecordingStore(nextIndex = 3)
        val session = TrackingLocationSession(
            locationProvider = provider,
            recordingContext = TrackingRecordingContext(
                session = navigationSession(),
                store = store,
                clock = { Instant.parse("2026-07-01T02:02:03Z") },
            ),
        )

        session.start()
        provider.emitSample(sampleAt("2026-07-01T01:02:04Z"))

        assertEquals(listOf(3), store.points.map { it.pointIndex })
        assertEquals(4, session.state.sampleCount)
    }

    @Test
    fun stopMarksRecordingSessionEnded() {
        val provider = FakeTrailLocationProvider()
        val store = FakeTrackingRecordingStore()
        val endedAt = Instant.parse("2026-07-01T02:02:03Z")
        val model = navigationSession()
        val session = TrackingLocationSession(
            locationProvider = provider,
            recordingContext = TrackingRecordingContext(
                session = model,
                store = store,
                clock = { endedAt },
            ),
        )

        session.start()
        provider.emitStatus(LocationProviderStatus.Ready)
        session.stop()

        assertEquals(endedAt.toEpochMilli(), store.endedSessions[model.id])
    }

    @Test
    fun stopCanDisposeProviderWithoutMarkingRecordingEnded() {
        val provider = FakeTrailLocationProvider()
        val store = FakeTrackingRecordingStore()
        val model = navigationSession()
        val session = TrackingLocationSession(
            locationProvider = provider,
            recordingContext = TrackingRecordingContext(
                session = model,
                store = store,
                clock = { Instant.parse("2026-07-01T02:02:03Z") },
            ),
        )

        session.start()
        provider.emitStatus(LocationProviderStatus.Ready)
        session.stop(markRecordingEnded = false)

        assertEquals(model.id, store.sessions.single().sessionId)
        assertTrue(store.endedSessions.isEmpty())
    }

    @Test
    fun disabledStartupDoesNotCreateOrEndRecordingSession() {
        val provider = FakeTrailLocationProvider()
        val store = FakeTrackingRecordingStore()
        val session = TrackingLocationSession(
            locationProvider = provider,
            recordingContext = TrackingRecordingContext(
                session = navigationSession(),
                store = store,
                clock = { Instant.parse("2026-07-01T02:02:03Z") },
            ),
        )

        session.start()
        provider.emitStatus(LocationProviderStatus.Disabled)
        session.stop()

        assertTrue(store.sessions.isEmpty())
        assertTrue(store.points.isEmpty())
        assertTrue(store.endedSessions.isEmpty())
    }

    private fun navigationSession(): NavigationSession =
        NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAt = Instant.parse("2026-07-01T01:02:03Z"),
        ).reduce(NavigationEvent.StartNavigation)

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

private class FakeTrackingRecordingStore(
    private var nextIndex: Int = 0,
) : TrackingRecordingStore {
    val sessions = mutableListOf<TrackingSessionRecord>()
    val points = mutableListOf<TrackingTrackPointRecord>()
    val endedSessions = mutableMapOf<NavigationSessionId, Long>()

    override fun upsertSession(record: TrackingSessionRecord) {
        sessions += record
    }

    override fun appendSample(
        sessionId: NavigationSessionId,
        sample: LocationSample,
    ): TrackingTrackPointRecord {
        val record = TrackingTrackPointRecord.fromSample(
            sessionId = sessionId,
            pointIndex = nextIndex,
            sample = sample,
        )
        nextIndex += 1
        points += record
        val previous = sessions.last()
        sessions += previous.copy(sampleCount = nextIndex)
        return record
    }

    override fun findActiveSession(): TrackingSessionRecord? =
        sessions.lastOrNull { it.endedAtEpochMillis == null }

    override fun loadPoints(sessionId: NavigationSessionId): List<TrackingTrackPointRecord> =
        points.filter { it.sessionId == sessionId }.sortedBy { it.pointIndex }

    override fun markSessionEnded(
        sessionId: NavigationSessionId,
        endedAt: Instant,
    ) {
        endedSessions[sessionId] = endedAt.toEpochMilli()
        sessions += sessions.last().copy(endedAtEpochMillis = endedAt.toEpochMilli())
    }
}
