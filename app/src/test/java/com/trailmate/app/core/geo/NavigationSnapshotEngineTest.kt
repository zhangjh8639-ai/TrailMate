package com.trailmate.app.core.geo

import com.trailmate.app.core.model.BatteryLevel
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.ExitPointType
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.RouteExitPoint
import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.core.model.RouteWaypoint
import com.trailmate.app.core.model.WaypointType
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationSnapshotEngineTest {
    @Test
    fun onRouteSampleProducesProgressSnapshot() {
        val result = NavigationSnapshotEngine.calculate(
            NavigationSnapshotInput(
                session = session(),
                geometry = routeGeometry(),
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.0,
                        longitude = 0.0015,
                    ),
                ),
                batteryLevel = BatteryLevel(64),
            ),
        )

        val snapshot = result.snapshot

        assertEquals(NavigationState.Navigating, snapshot.state)
        assertEquals(64, snapshot.batteryLevel.percent)
        assertEquals(8.0, snapshot.gpsAccuracy.meters, 0.1)
        assertEquals(150.0, result.projection.progress.meters, 1.0)
        assertEquals(150.0, snapshot.remainingDistance.meters, 1.0)
        assertEquals(50.0, snapshot.remainingElevation.meters, 1.0)
        assertEquals("wp-ridge", snapshot.nextWaypoint?.id)
        assertEquals("exit-behind", snapshot.nearestExit?.id)
        assertNull(snapshot.deviationDistance)
        assertNull(snapshot.nearestRoutePoint)
        assertEquals(OffRouteStatus.OnRoute, result.offRouteEvidence.status)
    }

    @Test
    fun poorGpsAccuracyPreservesSessionStateWithoutGuidance() {
        val result = NavigationSnapshotEngine.calculate(
            NavigationSnapshotInput(
                session = session(state = NavigationState.Navigating),
                geometry = routeGeometry(),
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.001,
                        longitude = 0.0015,
                    ),
                    accuracyMeters = 80.0,
                ),
                batteryLevel = BatteryLevel(42),
                thresholds = thresholds(),
            ),
        )

        assertEquals(NavigationState.Navigating, result.snapshot.state)
        assertEquals(OffRouteStatus.GpsUnreliable, result.offRouteEvidence.status)
        assertEquals(80.0, result.snapshot.gpsAccuracy.meters, 0.1)
        assertNull(result.snapshot.deviationDistance)
        assertNull(result.snapshot.nearestRoutePoint)
    }

    @Test
    fun sustainedDeviationProducesConfirmedOffRouteGuidance() {
        val startedAt = Instant.parse("2026-07-01T01:00:00Z")
        val previousEvidence = OffRouteEvidence(
            status = OffRouteStatus.Suspected,
            distanceFromRoute = Distance.meters(90.0),
            offRouteStartedAt = startedAt,
            consecutiveOffRouteSamples = 2,
        )

        val result = NavigationSnapshotEngine.calculate(
            NavigationSnapshotInput(
                session = session(state = NavigationState.Navigating),
                geometry = routeGeometry(),
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.001,
                        longitude = 0.0015,
                    ),
                    recordedAt = startedAt.plusSeconds(50),
                ),
                batteryLevel = BatteryLevel(55),
                thresholds = thresholds(),
                previousEvidence = previousEvidence,
            ),
        )

        val guidance = requireNotNull(result.snapshot.nearestRoutePoint)

        assertEquals(NavigationState.ConfirmedOffRoute, result.snapshot.state)
        assertEquals(OffRouteStatus.Confirmed, result.offRouteEvidence.status)
        assertTrue(requireNotNull(result.snapshot.deviationDistance).meters > 70.0)
        assertTrue(guidance.directionText.endsWith("方向"))
        assertTrue(guidance.distance.meters > 70.0)
    }

    @Test
    fun guidanceDoesNotExposeSafeStraightLineCommands() {
        val result = NavigationSnapshotEngine.calculate(
            NavigationSnapshotInput(
                session = session(state = NavigationState.Navigating),
                geometry = routeGeometry(),
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.001,
                        longitude = 0.0015,
                    ),
                ),
                batteryLevel = BatteryLevel(55),
                thresholds = thresholds(),
            ),
        )

        val guidanceText = requireNotNull(result.snapshot.nearestRoutePoint).directionText

        assertEquals(NavigationState.SuspectedOffRoute, result.snapshot.state)
        assertEquals(OffRouteStatus.Suspected, result.offRouteEvidence.status)
        listOf("直行", "安全", "穿越", "路线已规划").forEach { unsafeClaim ->
            assertFalse(guidanceText.contains(unsafeClaim))
        }
    }

    @Test
    fun reverseDirectionUsesReverseProgress() {
        val result = NavigationSnapshotEngine.calculate(
            NavigationSnapshotInput(
                session = session(direction = NavigationDirection.Reverse),
                geometry = routeGeometry(),
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.0,
                        longitude = 0.0015,
                    ),
                ),
                batteryLevel = BatteryLevel(88),
            ),
        )

        val snapshot = result.snapshot

        assertEquals(150.0, snapshot.remainingDistance.meters, 1.0)
        assertEquals(5.0, snapshot.remainingElevation.meters, 1.0)
        assertEquals("wp-done", snapshot.nextWaypoint?.id)
    }

    @Test
    fun returningOnTrackSessionIsNotOverwrittenByOnRouteEvidence() {
        val result = NavigationSnapshotEngine.calculate(
            NavigationSnapshotInput(
                session = session(state = NavigationState.ReturningOnTrack),
                geometry = routeGeometry(),
                sample = sampleAt(
                    coordinate = GeoCoordinate(
                        latitude = 0.0,
                        longitude = 0.0015,
                    ),
                ),
                batteryLevel = BatteryLevel(31),
            ),
        )

        assertEquals(NavigationState.ReturningOnTrack, result.snapshot.state)
        assertEquals(OffRouteStatus.OnRoute, result.offRouteEvidence.status)
        assertNull(result.snapshot.nearestRoutePoint)
    }

    @Test
    fun manualAndTerminalSessionStatesArePreserved() {
        listOf(
            NavigationState.Idle,
            NavigationState.Paused,
            NavigationState.Ended,
        ).forEach { preservedState ->
            val result = NavigationSnapshotEngine.calculate(
                NavigationSnapshotInput(
                    session = session(state = preservedState),
                    geometry = routeGeometry(),
                    sample = sampleAt(
                        coordinate = GeoCoordinate(
                            latitude = 0.0,
                            longitude = 0.0015,
                        ),
                    ),
                    batteryLevel = BatteryLevel(72),
                ),
            )

            assertEquals(preservedState, result.snapshot.state)
            assertEquals(OffRouteStatus.OnRoute, result.offRouteEvidence.status)
        }
    }

    private fun session(
        state: NavigationState = NavigationState.Navigating,
        direction: NavigationDirection = NavigationDirection.Forward,
    ): NavigationSession =
        NavigationSession(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("route-1"),
            startedAt = Instant.parse("2026-07-01T00:00:00Z"),
            state = state,
            direction = direction,
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
                RouteExitPoint(
                    id = "exit-road",
                    title = "最近公路",
                    type = ExitPointType.RoadAccess,
                    distanceFromStart = Distance.meters(220.0),
                ),
            ),
        )
}
