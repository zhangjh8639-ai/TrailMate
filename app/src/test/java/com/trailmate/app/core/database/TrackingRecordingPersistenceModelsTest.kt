package com.trailmate.app.core.database

import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationEvent
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TrackingRecordingPersistenceModelsTest {
    @Test
    fun sessionRecordFromNavigationSessionKeepsPrivateActiveMetadata() {
        val startedAt = Instant.parse("2026-07-01T01:02:03Z")
        val session = NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAt = startedAt,
        ).reduce(NavigationEvent.StartNavigation)

        val record = TrackingSessionRecord.fromSession(session)

        assertEquals(session.id, record.sessionId)
        assertEquals(RouteId("longjing"), record.routeId)
        assertEquals(startedAt.toEpochMilli(), record.startedAtEpochMillis)
        assertNull(record.endedAtEpochMillis)
        assertEquals(NavigationState.Navigating, record.state)
        assertEquals(NavigationDirection.Forward, record.direction)
        assertEquals(PrivacyVisibility.Private, record.visibility)
        assertEquals(0, record.sampleCount)
    }

    @Test
    fun pointRecordFromSampleUsesOrderedRealProviderFields() {
        val sample = LocationSample(
            coordinate = GeoCoordinate(
                latitude = 30.245,
                longitude = 120.116,
                elevation = Elevation.meters(118.5),
            ),
            accuracy = GpsAccuracy(4.5),
            recordedAt = Instant.parse("2026-07-01T01:02:04Z"),
            bearingDegrees = 28.0,
            speedMetersPerSecond = 1.4,
        )

        val record = TrackingTrackPointRecord.fromSample(
            sessionId = NavigationSessionId("session-1"),
            pointIndex = 2,
            sample = sample,
        )

        assertEquals(NavigationSessionId("session-1"), record.sessionId)
        assertEquals(2, record.pointIndex)
        assertEquals(sample.coordinate, record.coordinate)
        assertEquals(sample.accuracy, record.accuracy)
        assertEquals(sample.recordedAt.toEpochMilli(), record.recordedAtEpochMillis)
        assertEquals(28.0, record.bearingDegrees)
        assertEquals(1.4, record.speedMetersPerSecond)
    }

    @Test
    fun negativePointIndexIsRejected() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            TrackingTrackPointRecord(
                sessionId = NavigationSessionId("session-1"),
                pointIndex = -1,
                coordinate = GeoCoordinate(latitude = 30.245, longitude = 120.116),
                accuracy = GpsAccuracy(4.5),
                recordedAtEpochMillis = Instant.parse("2026-07-01T01:02:04Z").toEpochMilli(),
                bearingDegrees = null,
                speedMetersPerSecond = null,
            )
        }

        assertEquals("Track point index must be non-negative.", error.message)
    }
}
