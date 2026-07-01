package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class NavigationModelsTest {
    @Test
    fun navigationSessionDefaultsToPrivateIdleState() {
        val session = NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAt = Instant.parse("2026-07-01T01:00:00Z"),
        )

        assertEquals(PrivacyVisibility.Private, session.visibility)
        assertEquals(NavigationState.Idle, session.state)
        assertEquals(NavigationDirection.Forward, session.direction)
    }

    @Test
    fun navigationCanStartFromIdle() {
        val result = NavigationStateReducer.reduce(
            state = NavigationState.Idle,
            event = NavigationEvent.StartNavigation,
        )

        assertEquals(NavigationState.Navigating, result)
    }

    @Test
    fun confirmedOffRouteRequiresSuspectedOffRouteFirst() {
        val result = NavigationStateReducer.reduce(
            state = NavigationState.Navigating,
            event = NavigationEvent.ConfirmOffRoute,
        )

        assertEquals(NavigationState.Navigating, result)
    }

    @Test
    fun returnOnTrackStartsFromSuspectedOrConfirmedOffRoute() {
        assertEquals(
            NavigationState.ReturningOnTrack,
            NavigationStateReducer.reduce(
                state = NavigationState.SuspectedOffRoute,
                event = NavigationEvent.ReturnOnTrack,
            ),
        )
        assertEquals(
            NavigationState.ReturningOnTrack,
            NavigationStateReducer.reduce(
                state = NavigationState.ConfirmedOffRoute,
                event = NavigationEvent.ReturnOnTrack,
            ),
        )
    }

    @Test
    fun endedNavigationIsTerminal() {
        NavigationEvent.entries.forEach { event ->
            assertEquals(
                NavigationState.Ended,
                NavigationStateReducer.reduce(NavigationState.Ended, event),
            )
        }
    }

    @Test
    fun pauseAndResumePreserveSafetyStateWhenSessionReducerIsUsed() {
        val suspected = NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAt = Instant.parse("2026-07-01T01:00:00Z"),
        )
            .reduce(NavigationEvent.StartNavigation)
            .reduce(NavigationEvent.SuspectOffRoute)

        val paused = suspected.reduce(NavigationEvent.Pause)
        val resumed = paused.reduce(NavigationEvent.Resume)

        assertEquals(NavigationState.Paused, paused.state)
        assertEquals(NavigationState.SuspectedOffRoute, resumed.state)
    }

    @Test
    fun duplicatePauseDoesNotOverwritePreviousSafetyState() {
        val suspected = NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAt = Instant.parse("2026-07-01T01:00:00Z"),
        )
            .reduce(NavigationEvent.StartNavigation)
            .reduce(NavigationEvent.SuspectOffRoute)

        val resumed = suspected
            .reduce(NavigationEvent.Pause)
            .reduce(NavigationEvent.Pause)
            .reduce(NavigationEvent.Resume)

        assertEquals(NavigationState.SuspectedOffRoute, resumed.state)
    }

    @Test
    fun snapshotCarriesProgressAndNearestExitWithoutOwningGeometryAlgorithm() {
        val snapshot = NavigationSnapshot(
            sessionId = NavigationSessionId("session-1"),
            state = NavigationState.SuspectedOffRoute,
            currentCoordinate = GeoCoordinate(30.245, 120.116, Elevation.meters(92.0)),
            gpsAccuracy = GpsAccuracy(8.0),
            batteryLevel = BatteryLevel(86),
            remainingDistance = Distance.meters(5400.0),
            remainingElevation = Elevation.meters(320.0),
            nextWaypoint = RouteWaypoint("cp1", "第一个检查点", WaypointType.Checkpoint, Distance.meters(1800.0)),
            nearestExit = RouteExitPoint("exit-road", "最近公路", ExitPointType.RoadAccess, Distance.meters(1100.0)),
            deviationDistance = Distance.meters(72.0),
            nearestRoutePoint = NearestRoutePointGuidance(
                directionText = "西北方向",
                distance = Distance.meters(110.0),
            ),
            updatedAt = Instant.parse("2026-07-01T02:00:00Z"),
        )

        assertEquals(NavigationState.SuspectedOffRoute, snapshot.state)
        assertEquals(Distance.meters(110.0), snapshot.nearestRoutePoint?.distance)
        assertEquals("最近公路", snapshot.nearestExit?.title)
    }
}
