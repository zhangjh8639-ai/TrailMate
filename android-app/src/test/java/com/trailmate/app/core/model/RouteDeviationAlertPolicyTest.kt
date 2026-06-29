package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationAlertPolicyTest {
    @Test
    fun firstReliableOffRouteFixTriggersUrgentAlert() {
        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 112.0),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE, decision.kind)
        assertTrue(decision.shouldNotify)
        assertTrue(decision.shouldVibrate)
        assertEquals("疑似偏离路线", decision.title)
        assertEquals("请先停下，当前位置距计划路线约 112 m。核对地图、路标和现场路径后再继续。", decision.caption)
        assertEquals("停下核对路线", decision.primaryActionLabel)
        assertTrue(decision.nextState.activeEpisode)
        assertEquals(10_000L, decision.nextState.lastAlertEpochMillis)
        assertEquals(112.0, decision.nextState.lastAlertCrossTrackErrorMeters, 0.0)
    }

    @Test
    fun sameEpisodeInsideCooldownSuppressesRepeatedNotification() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 125.0),
            state = state,
            nowEpochMillis = 40_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE_SILENT, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals("偏离恢复中", decision.title)
        assertEquals("继续核对路线，暂不重复提醒。当前位置距计划路线约 125 m。", decision.caption)
        assertTrue(decision.nextState.activeEpisode)
        assertEquals(10_000L, decision.nextState.lastAlertEpochMillis)
        assertEquals(112.0, decision.nextState.lastAlertCrossTrackErrorMeters, 0.0)
    }

    @Test
    fun worseningDeviationInsideCooldownEscalatesAlert() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 172.0),
            state = state,
            nowEpochMillis = 40_000L
        )

        assertEquals(RouteDeviationAlertKind.OFF_ROUTE_ESCALATED, decision.kind)
        assertTrue(decision.shouldNotify)
        assertTrue(decision.shouldVibrate)
        assertEquals("偏离距离增加", decision.title)
        assertEquals("你可能正在远离计划路线，当前偏离约 172 m。请停下确认是否需要原路返回。", decision.caption)
        assertEquals(40_000L, decision.nextState.lastAlertEpochMillis)
        assertEquals(172.0, decision.nextState.lastAlertCrossTrackErrorMeters, 0.0)
    }

    @Test
    fun rejoiningRouteClearsEpisodeOnce() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 18.0).copy(timestampEpochMillis = 90_000L),
            state = state,
            nowEpochMillis = 90_000L
        )

        assertEquals(RouteDeviationAlertKind.REJOINED_ROUTE, decision.kind)
        assertTrue(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals("已回到路线", decision.title)
        assertEquals("当前位置已回到计划路线附近，请确认下一检查点后继续。", decision.caption)
        assertFalse(decision.nextState.activeEpisode)
        assertTrue(decision.nextState.rejoinNoticeEmitted)

        val repeated = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 12.0).copy(timestampEpochMillis = 95_000L),
            state = decision.nextState,
            nowEpochMillis = 95_000L
        )
        assertEquals(RouteDeviationAlertKind.NONE, repeated.kind)
        assertFalse(repeated.shouldNotify)
    }

    @Test
    fun onRouteWithMissingFixPreservesActiveEpisode() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = null,
            state = state,
            nowEpochMillis = 90_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(state, decision.nextState)
    }

    @Test
    fun onRouteWithPoorAccuracyFixPreservesActiveEpisode() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 18.0).copy(horizontalAccuracyMeters = 120.0),
            state = state,
            nowEpochMillis = 90_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(state, decision.nextState)
    }

    @Test
    fun unreliableFixDoesNotTriggerOffRouteAlert() {
        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 112.0).copy(horizontalAccuracyMeters = 120.0),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 10_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals("等待定位稳定", decision.title)
        assertEquals("当前定位精度约 120 m，先到开阔处等待可靠定位，再判断是否偏离路线。", decision.caption)
        assertFalse(decision.nextState.activeEpisode)
    }

    @Test
    fun staleOffRouteFixWaitsForReliableFixWithoutAlert() {
        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 112.0).copy(timestampEpochMillis = 1_000L),
            state = RouteDeviationAlertState(),
            nowEpochMillis = 62_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals("等待定位稳定", decision.title)
        assertFalse(decision.nextState.activeEpisode)
    }

    @Test
    fun invalidOffRouteFixWaitsForReliableFixWithoutAlert() {
        val invalidFixes = listOf(
            reliableFix(crossTrackErrorMeters = 112.0).copy(
                distanceAlongRouteKm = Double.NaN,
                timestampEpochMillis = NOW_EPOCH_MILLIS
            ),
            reliableFix(crossTrackErrorMeters = Double.NaN).copy(timestampEpochMillis = NOW_EPOCH_MILLIS),
            reliableFix(crossTrackErrorMeters = 112.0).copy(
                horizontalAccuracyMeters = Double.POSITIVE_INFINITY,
                timestampEpochMillis = NOW_EPOCH_MILLIS
            ),
            reliableFix(crossTrackErrorMeters = 112.0).copy(
                horizontalAccuracyMeters = -1.0,
                timestampEpochMillis = NOW_EPOCH_MILLIS
            ),
            reliableFix(crossTrackErrorMeters = 112.0).copy(timestampEpochMillis = 0L)
        )

        invalidFixes.forEach { fix ->
            val decision = RouteDeviationAlertPolicy.evaluate(
                status = LocationBackedHikeStatus.CHECK_ROUTE,
                fix = fix,
                state = RouteDeviationAlertState(),
                nowEpochMillis = NOW_EPOCH_MILLIS
            )

            assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
            assertFalse(decision.shouldNotify)
            assertFalse(decision.shouldVibrate)
            assertEquals("等待定位稳定", decision.title)
            assertFalse(decision.nextState.activeEpisode)
        }
    }

    @Test
    fun futureRejoinFixPreservesActiveEpisode() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val decision = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 18.0).copy(timestampEpochMillis = NOW_EPOCH_MILLIS + 1L),
            state = state,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, decision.kind)
        assertFalse(decision.shouldNotify)
        assertFalse(decision.shouldVibrate)
        assertEquals(state, decision.nextState)
    }

    @Test
    fun unreliableFixPreservesActiveEpisodeUntilReliableRejoin() {
        val state = RouteDeviationAlertState(
            activeEpisode = true,
            lastAlertEpochMillis = 10_000L,
            lastAlertCrossTrackErrorMeters = 112.0,
            rejoinNoticeEmitted = false
        )

        val unreliable = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 18.0).copy(horizontalAccuracyMeters = 120.0),
            state = state,
            nowEpochMillis = 60_000L
        )

        assertEquals(RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX, unreliable.kind)
        assertFalse(unreliable.shouldNotify)
        assertFalse(unreliable.shouldVibrate)
        assertEquals(state, unreliable.nextState)

        val rejoined = RouteDeviationAlertPolicy.evaluate(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = reliableFix(crossTrackErrorMeters = 18.0).copy(timestampEpochMillis = 90_000L),
            state = unreliable.nextState,
            nowEpochMillis = 90_000L
        )

        assertEquals(RouteDeviationAlertKind.REJOINED_ROUTE, rejoined.kind)
        assertTrue(rejoined.shouldNotify)
        assertFalse(rejoined.shouldVibrate)
        assertFalse(rejoined.nextState.activeEpisode)
    }

    private fun reliableFix(crossTrackErrorMeters: Double): HikeLocationFix =
        HikeLocationFix(
            distanceAlongRouteKm = 5.12,
            crossTrackErrorMeters = crossTrackErrorMeters,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = 1_000L
        )

    private companion object {
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
