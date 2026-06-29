package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecordingRouteMonitorEngineTest {
    @Test
    fun matchingRouteWithReliableFarPointTriggersOffRouteAlert() {
        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE, decision.kind)
        assertTrue(decision.shouldNotify)
        assertTrue(decision.shouldVibrate)
        assertTrue(decision.nextState.activeEpisode)
    }

    @Test
    fun repeatedOffRoutePointInsideCooldownStaysSilent() {
        val first = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        val repeated = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0011),
            state = first.nextState,
            nowEpochMillis = 40_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE_SILENT, repeated.kind)
        assertFalse(repeated.shouldNotify)
        assertFalse(repeated.shouldVibrate)
        assertTrue(repeated.nextState.activeEpisode)
    }

    @Test
    fun sameEpisodeWorsenedByAtLeastFiftyMetersEscalates() {
        val first = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        val worsened = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0016),
            state = first.nextState,
            nowEpochMillis = 40_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE_ESCALATED, worsened.kind)
        assertTrue(worsened.shouldNotify)
        assertTrue(worsened.shouldVibrate)
        assertTrue(worsened.nextState.activeEpisode)
    }

    @Test
    fun activeEpisodeFollowedByReliableNearRoutePointEmitsRejoinedWithoutVibration() {
        val first = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        val rejoined = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0001, timestampEpochMillis = 89_000L),
            state = first.nextState,
            nowEpochMillis = 90_000L
        )

        assertEquals(RouteDeviationAlertKind.REJOINED_ROUTE, rejoined.kind)
        assertTrue(rejoined.shouldNotify)
        assertFalse(rejoined.shouldVibrate)
        assertFalse(rejoined.nextState.activeEpisode)
    }

    @Test
    fun missingMismatchedOrInsufficientRouteGeometryReturnsNoneAndResetsState() {
        val staleState = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 120.0,
            rejoinNoticeEmitted = false
        )

        listOf(
            null,
            route(routeName = "Other route"),
            route(routePoints = listOf(routePoint(latitude = 30.0000, distanceAlongRouteKm = 0.0)))
        ).forEach { route ->
            val decision = TrackRecordingRouteMonitorEngine.evaluate(
                route = route,
                recordingRouteName = RouteName,
                recordingRouteKey = route().offlineRoutePackKey(),
                point = point(longitude = 120.0010),
                state = staleState,
                nowEpochMillis = 10_000L
            )

            assertEquals(RouteDeviationAlertKind.NONE, decision.kind)
            assertFalse(decision.shouldNotify)
            assertFalse(decision.shouldVibrate)
            assertEquals(RouteDeviationAlertState(), decision.nextState)
        }
    }

    @Test
    fun matchingRouteWithLowAccuracyPointWaitsForReliableFix() {
        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010, horizontalAccuracyMeters = 120.0),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(RouteDeviationAlertState(), decision.nextState)
    }

    @Test
    fun matchingRouteWithStaleOffRoutePointWaitsForReliableFix() {
        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010, timestampEpochMillis = 1_000L),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 70_001L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(RouteDeviationAlertState(), decision.nextState)
    }

    @Test
    fun matchingRouteWithFutureTimestampPointWaitsForReliableFix() {
        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010, timestampEpochMillis = 10_001L),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(RouteDeviationAlertState(), decision.nextState)
    }

    @Test
    fun matchingRouteWithZeroTimestampPointWaitsForReliableFix() {
        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0010, timestampEpochMillis = 0L),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(RouteDeviationAlertState(), decision.nextState)
    }

    @Test
    fun staleNearRoutePointDuringActiveEpisodeDoesNotEmitRejoinedNotice() {
        val activeState = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 120.0,
            rejoinNoticeEmitted = false
        )

        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = route().offlineRoutePackKey(),
            point = point(longitude = 120.0001, timestampEpochMillis = 1_000L),
            state = activeState,
            nowEpochMillis = 70_001L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(activeState, decision.nextState)
    }

    @Test
    fun sameNameDifferentRouteKeyReturnsNoneAndResetsState() {
        val staleState = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 120.0,
            rejoinNoticeEmitted = false
        )

        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route(),
            recordingRouteName = RouteName,
            recordingRouteKey = "other-file.gpx|$RouteName|1.1|0|2",
            point = point(longitude = 120.0010),
            state = staleState,
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.NONE, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(RouteDeviationAlertState(), decision.nextState)
    }

    private fun route(
        routeName: String = RouteName,
        routePoints: List<RoutePoint> = listOf(
            routePoint(latitude = 30.0000, distanceAlongRouteKm = 0.0),
            routePoint(latitude = 30.0100, distanceAlongRouteKm = 1.1)
        )
    ): ImportedRoute =
        ImportedRoute(
            routeName = routeName,
            fileName = "test-ridge.gpx",
            distanceKm = 1.1,
            ascentMeters = 0,
            status = RouteImportStatus.PARSED,
            pointCount = routePoints.size,
            routePoints = routePoints
        )

    private fun routePoint(
        latitude: Double,
        distanceAlongRouteKm: Double
    ): RoutePoint =
        RoutePoint(
            latitude = latitude,
            longitude = 120.0000,
            elevationMeters = null,
            distanceAlongRouteKm = distanceAlongRouteKm
        )

    private fun point(
        longitude: Double,
        horizontalAccuracyMeters: Double = 8.0,
        timestampEpochMillis: Long = 1_000L
    ): RecordedTrackPoint =
        RecordedTrackPoint(
            latitude = 30.0050,
            longitude = longitude,
            elevationMeters = null,
            horizontalAccuracyMeters = horizontalAccuracyMeters,
            timestampEpochMillis = timestampEpochMillis
        )

    private companion object {
        const val RouteName = "Test Ridge"
    }
}
