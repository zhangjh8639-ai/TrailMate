package com.trailmate.app.core.geo

import com.trailmate.app.core.model.BatteryLevel
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.ExitPointType
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteExitPoint
import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.core.model.RouteWaypoint
import com.trailmate.app.core.model.WaypointType
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NavigationRuntimeReducerTest {
    @Test
    fun startNavigationCreatesActivePrivateRuntimeState() {
        val state = NavigationRuntimeReducer.reduce(
            state = null,
            action = NavigationRuntimeAction.Start(
                session = idleSession(),
                geometry = routeGeometry(),
            ),
        )

        assertNotNull(state)
        requireNotNull(state)
        assertEquals(NavigationState.Navigating, state.session.state)
        assertEquals(PrivacyVisibility.Private, state.session.visibility)
        assertNull(state.latestSnapshot)
        assertNull(state.latestOffRouteEvidence)
        assertNull(state.latestRouteProgress)
    }

    @Test
    fun locationSampleUpdatesSnapshotEvidenceAndProgress() {
        val state = startedState()

        val updated = NavigationRuntimeReducer.reduce(
            state = state,
            action = NavigationRuntimeAction.LocationUpdated(
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.0,
                        longitude = 0.0015,
                    ),
                ),
                batteryLevel = BatteryLevel(64),
            ),
        )

        assertEquals(NavigationState.Navigating, updated?.session?.state)
        assertEquals(NavigationState.Navigating, updated?.latestSnapshot?.state)
        assertEquals(OffRouteStatus.OnRoute, updated?.latestOffRouteEvidence?.status)
        assertEquals(150.0, updated?.latestRouteProgress?.meters ?: -1.0, 1.0)
        assertEquals(150.0, updated?.latestSnapshot?.remainingDistance?.meters ?: -1.0, 1.0)
    }

    @Test
    fun repeatedOffRouteSamplesBecomeConfirmedOffRoute() {
        val startedAt = Instant.parse("2026-07-01T01:00:00Z")
        val thresholds = thresholds()
        val first = NavigationRuntimeReducer.reduce(
            state = startedState(),
            action = offRouteSampleAction(recordedAt = startedAt, thresholds = thresholds),
        )
        val second = NavigationRuntimeReducer.reduce(
            state = first,
            action = offRouteSampleAction(recordedAt = startedAt.plusSeconds(20), thresholds = thresholds),
        )
        val third = NavigationRuntimeReducer.reduce(
            state = second,
            action = offRouteSampleAction(recordedAt = startedAt.plusSeconds(50), thresholds = thresholds),
        )

        assertEquals(NavigationState.ConfirmedOffRoute, third?.session?.state)
        assertEquals(NavigationState.ConfirmedOffRoute, third?.latestSnapshot?.state)
        assertEquals(OffRouteStatus.Confirmed, third?.latestOffRouteEvidence?.status)
    }

    @Test
    fun originalReturnStateSurvivesOnRouteLocationSamples() {
        val returning = NavigationRuntimeReducer.reduce(
            state = startedState(),
            action = NavigationRuntimeAction.EnterReturnMode,
        )

        val updated = NavigationRuntimeReducer.reduce(
            state = returning,
            action = NavigationRuntimeAction.LocationUpdated(
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.0,
                        longitude = 0.0015,
                    ),
                ),
                batteryLevel = BatteryLevel(41),
            ),
        )

        assertEquals(NavigationState.ReturningOnTrack, updated?.session?.state)
        assertEquals(NavigationState.ReturningOnTrack, updated?.latestSnapshot?.state)
        assertNull(updated?.latestSnapshot?.nearestRoutePoint)
    }

    @Test
    fun pauseAndResumePreserveRuntimeNavigationMode() {
        val returning = NavigationRuntimeReducer.reduce(
            state = startedState(),
            action = NavigationRuntimeAction.EnterReturnMode,
        )

        val paused = NavigationRuntimeReducer.reduce(
            state = returning,
            action = NavigationRuntimeAction.Pause,
        )
        val resumed = NavigationRuntimeReducer.reduce(
            state = paused,
            action = NavigationRuntimeAction.Resume,
        )

        assertEquals(NavigationState.Paused, paused?.session?.state)
        assertEquals(NavigationState.ReturningOnTrack, resumed?.session?.state)
    }

    @Test
    fun poorGpsSuppressesNearestRouteGuidance() {
        val startedAt = Instant.parse("2026-07-01T01:00:00Z")
        val confirmed = NavigationRuntimeReducer.reduce(
            state = startedState(),
            action = offRouteSampleAction(recordedAt = startedAt, thresholds = thresholds()),
        )?.let {
            NavigationRuntimeReducer.reduce(
                state = it,
                action = offRouteSampleAction(recordedAt = startedAt.plusSeconds(20), thresholds = thresholds()),
            )
        }?.let {
            NavigationRuntimeReducer.reduce(
                state = it,
                action = offRouteSampleAction(recordedAt = startedAt.plusSeconds(50), thresholds = thresholds()),
            )
        }

        val unreliable = NavigationRuntimeReducer.reduce(
            state = confirmed,
            action = NavigationRuntimeAction.LocationUpdated(
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.001,
                        longitude = 0.0015,
                    ),
                    accuracyMeters = 80.0,
                    recordedAt = startedAt.plusSeconds(70),
                ),
                batteryLevel = BatteryLevel(39),
                thresholds = thresholds(),
            ),
        )

        assertEquals(NavigationState.ConfirmedOffRoute, unreliable?.session?.state)
        assertEquals(OffRouteStatus.GpsUnreliable, unreliable?.latestOffRouteEvidence?.status)
        assertNull(unreliable?.latestSnapshot?.deviationDistance)
        assertNull(unreliable?.latestSnapshot?.nearestRoutePoint)
    }

    @Test
    fun poorGpsDoesNotOverwriteLastReliableRouteProgress() {
        val reliable = NavigationRuntimeReducer.reduce(
            state = startedState(),
            action = NavigationRuntimeAction.LocationUpdated(
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.0,
                        longitude = 0.0015,
                    ),
                    accuracyMeters = 8.0,
                    recordedAt = Instant.parse("2026-07-01T01:00:00Z"),
                ),
                batteryLevel = BatteryLevel(62),
            ),
        )

        val unreliable = NavigationRuntimeReducer.reduce(
            state = reliable,
            action = NavigationRuntimeAction.LocationUpdated(
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.001,
                        longitude = 0.003,
                    ),
                    accuracyMeters = 80.0,
                    recordedAt = Instant.parse("2026-07-01T01:01:00Z"),
                ),
                batteryLevel = BatteryLevel(61),
                thresholds = thresholds(),
            ),
        )

        assertEquals(150.0, reliable?.latestRouteProgress?.meters ?: -1.0, 1.0)
        assertEquals(150.0, unreliable?.latestRouteProgress?.meters ?: -1.0, 1.0)
        assertEquals(OffRouteStatus.GpsUnreliable, unreliable?.latestOffRouteEvidence?.status)
    }

    @Test
    fun endedSessionIgnoresLaterSamplesAndResumeActions() {
        val ended = NavigationRuntimeReducer.reduce(
            state = startedState(),
            action = NavigationRuntimeAction.End,
        )

        val afterSample = NavigationRuntimeReducer.reduce(
            state = ended,
            action = offRouteSampleAction(recordedAt = Instant.parse("2026-07-01T01:05:00Z")),
        )
        val afterResume = NavigationRuntimeReducer.reduce(
            state = afterSample,
            action = NavigationRuntimeAction.Resume,
        )

        assertEquals(NavigationState.Ended, ended?.session?.state)
        assertEquals(NavigationState.Ended, afterSample?.session?.state)
        assertEquals(NavigationState.Ended, afterResume?.session?.state)
        assertNull(afterSample?.latestSnapshot)
    }

    @Test
    fun endClearsActiveSnapshotAndEvidence() {
        val active = NavigationRuntimeReducer.reduce(
            state = startedState(),
            action = NavigationRuntimeAction.LocationUpdated(
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.001,
                        longitude = 0.0015,
                    ),
                ),
                batteryLevel = BatteryLevel(44),
                thresholds = thresholds(),
            ),
        )

        val ended = NavigationRuntimeReducer.reduce(
            state = active,
            action = NavigationRuntimeAction.End,
        )
        val afterSample = NavigationRuntimeReducer.reduce(
            state = ended,
            action = offRouteSampleAction(recordedAt = Instant.parse("2026-07-01T01:10:00Z")),
        )

        assertEquals(NavigationState.SuspectedOffRoute, active?.latestSnapshot?.state)
        assertEquals(NavigationState.Ended, ended?.session?.state)
        assertNull(ended?.latestSnapshot)
        assertNull(ended?.latestOffRouteEvidence)
        assertNull(ended?.lastReliableOffRouteEvidence)
        assertNull(afterSample?.latestSnapshot)
    }

    private fun startedState(): NavigationRuntimeState =
        requireNotNull(
            NavigationRuntimeReducer.reduce(
                state = null,
                action = NavigationRuntimeAction.Start(
                    session = idleSession(),
                    geometry = routeGeometry(),
                ),
            ),
        )

    private fun idleSession(): NavigationSession =
        NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("route-1"),
            startedAt = Instant.parse("2026-07-01T00:00:00Z"),
        )

    private fun offRouteSampleAction(
        recordedAt: Instant,
        thresholds: OffRouteThresholds = thresholds(),
    ): NavigationRuntimeAction.LocationUpdated =
        NavigationRuntimeAction.LocationUpdated(
            sample = sampleAt(
                coordinate = GeoCoordinate(
                    latitude = 0.001,
                    longitude = 0.0015,
                ),
                recordedAt = recordedAt,
            ),
            batteryLevel = BatteryLevel(55),
            thresholds = thresholds,
        )

    private fun sampleAt(
        coordinate: GeoCoordinate,
        accuracyMeters: Double = 8.0,
        recordedAt: Instant = Instant.parse("2026-07-01T01:00:00Z"),
    ): LocationSample =
        LocationSample(
            coordinate = coordinate,
            accuracy = GpsAccuracy(accuracyMeters),
            recordedAt = recordedAt,
        )

    private fun thresholds(): OffRouteThresholds =
        OffRouteThresholds(
            suspectedDistance = Distance.meters(70.0),
            maxAcceptableAccuracy = GpsAccuracy(30.0),
            confirmationDuration = Duration.ofSeconds(45),
            confirmationSamples = 3,
        )

    private fun routeGeometry(): RouteGeometry =
        RouteGeometry(
            coordinates = listOf(
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.0,
                    elevation = Elevation.meters(100.0),
                ),
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.001,
                    elevation = Elevation.meters(130.0),
                ),
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.002,
                    elevation = Elevation.meters(120.0),
                ),
                GeoCoordinate(
                    latitude = 0.0,
                    longitude = 0.003,
                    elevation = Elevation.meters(170.0),
                ),
            ),
            cumulativeDistances = listOf(
                Distance.ZERO,
                Distance.meters(100.0),
                Distance.meters(200.0),
                Distance.meters(300.0),
            ),
            waypoints = listOf(
                RouteWaypoint(
                    id = "wp-done",
                    title = "已通过水源",
                    type = WaypointType.Water,
                    distanceFromStart = Distance.meters(80.0),
                ),
                RouteWaypoint(
                    id = "wp-ridge",
                    title = "山脊检查点",
                    type = WaypointType.Checkpoint,
                    distanceFromStart = Distance.meters(250.0),
                ),
            ),
            exitPoints = listOf(
                RouteExitPoint(
                    id = "exit-behind",
                    title = "身后岔路口",
                    type = ExitPointType.Trailhead,
                    distanceFromStart = Distance.meters(135.0),
                ),
            ),
        )
}
