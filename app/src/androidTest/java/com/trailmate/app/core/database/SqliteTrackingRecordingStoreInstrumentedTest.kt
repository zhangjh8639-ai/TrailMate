package com.trailmate.app.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationEvent
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteConfidence
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.core.model.RouteOfflineStatus
import com.trailmate.app.core.model.RouteSourceType
import java.time.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SqliteTrackingRecordingStoreInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase("trailmate.db")
    }

    @After
    fun tearDown() {
        context.deleteDatabase("trailmate.db")
    }

    @Test
    fun createsActiveSessionAppendsOrderedPointsAndMarksEnded() {
        val store = SqliteTrackingRecordingStore(context)
        val session = navigatingSession()

        store.upsertSession(TrackingSessionRecord.fromSession(session))
        store.appendSample(
            sessionId = session.id,
            sample = sampleAt("2026-07-01T01:02:04Z", 30.245),
        )
        store.appendSample(
            sessionId = session.id,
            sample = sampleAt("2026-07-01T01:02:05Z", 30.246),
        )

        val active = store.findActiveSession()
        val points = store.loadPoints(session.id)

        assertEquals(session.id, active?.sessionId)
        assertEquals(RouteId("longjing"), active?.routeId)
        assertEquals(PrivacyVisibility.Private, active?.visibility)
        assertEquals(NavigationState.Navigating, active?.state)
        assertEquals(2, active?.sampleCount)
        assertEquals(listOf(0, 1), points.map { it.pointIndex })
        assertEquals(30.245, points.first().coordinate.latitude, 0.0)

        store.markSessionEnded(
            sessionId = session.id,
            endedAt = Instant.parse("2026-07-01T02:02:03Z"),
        )

        assertNull(store.findActiveSession())
    }

    @Test
    fun upsertingExistingSessionDoesNotDeleteRecordedPoints() {
        val store = SqliteTrackingRecordingStore(context)
        val session = navigatingSession()

        store.upsertSession(TrackingSessionRecord.fromSession(session))
        store.appendSample(
            sessionId = session.id,
            sample = sampleAt("2026-07-01T01:02:04Z", 30.245),
        )
        store.upsertSession(
            TrackingSessionRecord.fromSession(session).copy(state = NavigationState.Paused),
        )

        assertEquals(listOf(0), store.loadPoints(session.id).map { it.pointIndex })
        assertEquals(1, store.findActiveSession()?.sampleCount)
        assertEquals(NavigationState.Paused, store.findActiveSession()?.state)
    }

    @Test
    fun resumedSessionAppendsAfterExistingPointsInsteadOfReplacingPointZero() {
        val store = SqliteTrackingRecordingStore(context)
        val session = navigatingSession()

        store.upsertSession(TrackingSessionRecord.fromSession(session))
        store.appendSample(
            sessionId = session.id,
            sample = sampleAt("2026-07-01T01:02:04Z", 30.245),
        )
        store.upsertSession(TrackingSessionRecord.fromSession(session))
        store.appendSample(
            sessionId = session.id,
            sample = sampleAt("2026-07-01T01:02:05Z", 30.246),
        )

        val points = store.loadPoints(session.id)
        assertEquals(listOf(0, 1), points.map { it.pointIndex })
        assertEquals(listOf(30.245, 30.246), points.map { it.coordinate.latitude })
        assertEquals(2, store.findActiveSession()?.sampleCount)
    }

    @Test
    fun legacyVersionOneImportedRouteDatabaseMigratesTrackingTables() {
        createLegacyVersionOneImportedRouteDatabase()

        val store = SqliteTrackingRecordingStore(context)
        val session = navigatingSession()
        store.upsertSession(TrackingSessionRecord.fromSession(session))
        store.appendSample(
            sessionId = session.id,
            sample = sampleAt("2026-07-01T01:02:04Z", 30.245),
        )

        assertEquals(listOf(0), store.loadPoints(session.id).map { it.pointIndex })
        assertEquals(1, store.findActiveSession()?.sampleCount)
    }

    @Test
    fun trackingStoreCreatesSharedSchemaSoImportedRoutesStillWork() {
        val trackingStore = SqliteTrackingRecordingStore(context)
        trackingStore.upsertSession(TrackingSessionRecord.fromSession(navigatingSession()))

        val importedStore = SqliteImportedRouteStore(context)
        importedStore.upsert(importedRouteRecord())

        assertEquals("imported-route", importedStore.loadAll().single().id)
    }

    private fun navigatingSession(): NavigationSession =
        NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAt = Instant.parse("2026-07-01T01:02:03Z"),
        ).reduce(NavigationEvent.StartNavigation)

    private fun sampleAt(
        instant: String,
        latitude: Double,
    ): LocationSample =
        LocationSample(
            coordinate = GeoCoordinate(latitude = latitude, longitude = 120.116),
            accuracy = GpsAccuracy(4.5),
            recordedAt = Instant.parse(instant),
        )

    private fun importedRouteRecord(): ImportedRouteRecord =
        ImportedRouteRecord(
            id = "imported-route",
            fileName = "longjing.gpx",
            sourceType = RouteSourceType.ImportedGpx,
            routeName = "龙井山脊",
            distanceMeters = 1200.0,
            elevationGainMeters = 80.0,
            waypointCount = 0,
            trackPointCount = 2,
            hasElevation = false,
            importedAtEpochMillis = Instant.parse("2026-07-01T01:00:00Z").toEpochMilli(),
            visibility = PrivacyVisibility.Private,
            offlineStatus = RouteOfflineStatus.TrackOnly,
            confidence = RouteConfidence.Unverified,
            points = listOf(
                ImportedRoutePointRecord(
                    pointIndex = 0,
                    latitude = 30.245,
                    longitude = 120.116,
                    elevationMeters = null,
                    cumulativeDistanceMeters = 0.0,
                ),
                ImportedRoutePointRecord(
                    pointIndex = 1,
                    latitude = 30.246,
                    longitude = 120.117,
                    elevationMeters = null,
                    cumulativeDistanceMeters = 1200.0,
                ),
            ),
        )

    private fun createLegacyVersionOneImportedRouteDatabase() {
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("trailmate.db"), null).use { db ->
            db.version = 1
            db.execSQL(
                """
                CREATE TABLE imported_routes (
                    id TEXT PRIMARY KEY NOT NULL,
                    file_name TEXT NOT NULL,
                    source_type TEXT NOT NULL,
                    route_name TEXT NOT NULL,
                    distance_meters REAL NOT NULL,
                    elevation_gain_meters REAL NOT NULL,
                    waypoint_count INTEGER NOT NULL,
                    track_point_count INTEGER NOT NULL,
                    has_elevation INTEGER NOT NULL,
                    imported_at_epoch_millis INTEGER NOT NULL,
                    visibility TEXT NOT NULL,
                    offline_status TEXT NOT NULL,
                    confidence TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE imported_route_points (
                    route_id TEXT NOT NULL,
                    point_index INTEGER NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    elevation_meters REAL,
                    cumulative_distance_meters REAL NOT NULL,
                    PRIMARY KEY(route_id, point_index),
                    FOREIGN KEY(route_id) REFERENCES imported_routes(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
        }
    }
}
