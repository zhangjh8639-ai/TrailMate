package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationBackedHikeSessionEngineTest {
    private val plan = HikePlanSummary(
        checkpoints = listOf(
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.START,
                title = "起点",
                distanceKm = 0.0,
                timeFromStart = "0:00",
                note = "登山口。"
            ),
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.ENERGY_CHECK,
                title = "补给检查",
                distanceKm = 3.0,
                timeFromStart = "1:00",
                note = "检查食物和水。"
            ),
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.FINISH,
                title = "终点",
                distanceKm = 6.0,
                timeFromStart = "2:00",
                note = "完成。"
            )
        )
    )

    @Test
    fun accurateLocationFixAdvancesReachedCheckpoint() {
        val active = HikeSessionEngine.start(HikeSessionEngine.ready(plan))

        val update = LocationBackedHikeSessionEngine.applyLocationFix(
            plan = plan,
            session = active,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 3.05,
                crossTrackErrorMeters = 12.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            nowEpochMillis = 1_000L
        )

        assertEquals(HikeSessionStatus.ACTIVE, update.session.status)
        assertEquals(1, update.session.reachedCheckpointIndex)
        assertEquals(LocationBackedHikeStatus.ON_ROUTE, update.status)
        assertEquals("已对齐「补给检查」，路线进度 3.1 km。", update.caption)
        assertChineseCaption(update.caption)
    }

    @Test
    fun lowAccuracyFixDoesNotAdvanceCheckpoint() {
        val active = HikeSessionEngine.start(HikeSessionEngine.ready(plan))

        val update = LocationBackedHikeSessionEngine.applyLocationFix(
            plan = plan,
            session = active,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 3.05,
                crossTrackErrorMeters = 12.0,
                horizontalAccuracyMeters = 120.0,
                timestampEpochMillis = 1_000L
            ),
            nowEpochMillis = 1_000L
        )

        assertEquals(0, update.session.reachedCheckpointIndex)
        assertEquals(LocationBackedHikeStatus.LOW_ACCURACY, update.status)
        assertEquals("定位精度约 120 m，暂不推进检查点。", update.caption)
        assertChineseCaption(update.caption)
    }

    @Test
    fun staleLocationFixDoesNotAdvanceCheckpoint() {
        val active = HikeSessionEngine.start(HikeSessionEngine.ready(plan))

        val update = LocationBackedHikeSessionEngine.applyLocationFix(
            plan = plan,
            session = active,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 3.05,
                crossTrackErrorMeters = 12.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            nowEpochMillis = 62_000L
        )

        assertEquals(0, update.session.reachedCheckpointIndex)
        assertEquals(LocationBackedHikeStatus.LOW_ACCURACY, update.status)
        assertEquals("定位已超过 60 秒未更新，暂不推进检查点。", update.caption)
        assertChineseCaption(update.caption)
    }

    @Test
    fun offRouteFixWarnsWithoutAdvancingCheckpoint() {
        val active = HikeSessionEngine.start(HikeSessionEngine.ready(plan))

        val update = LocationBackedHikeSessionEngine.applyLocationFix(
            plan = plan,
            session = active,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 3.05,
                crossTrackErrorMeters = 110.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            nowEpochMillis = 1_000L
        )

        assertEquals(0, update.session.reachedCheckpointIndex)
        assertEquals(LocationBackedHikeStatus.CHECK_ROUTE, update.status)
        assertEquals("当前位置距计划路线约 110 m，请核对地图、路标和现场路径。", update.caption)
        assertChineseCaption(update.caption)
    }

    @Test
    fun finishCheckpointCompletesSessionFromLocationProgress() {
        val active = HikeSessionEngine.start(HikeSessionEngine.ready(plan))

        val update = LocationBackedHikeSessionEngine.applyLocationFix(
            plan = plan,
            session = active.copy(reachedCheckpointIndex = 1),
            fix = HikeLocationFix(
                distanceAlongRouteKm = 6.0,
                crossTrackErrorMeters = 8.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            nowEpochMillis = 1_000L
        )

        assertEquals(HikeSessionStatus.COMPLETED, update.session.status)
        assertEquals(LocationBackedHikeStatus.FINISHED, update.status)
        assertEquals("已对齐「终点」，路线进度 6.0 km。", update.caption)
    }

    @Test
    fun nonActiveSessionsReturnStatusSpecificNoOpUpdates() {
        val ready = HikeSessionEngine.ready(plan)
        val paused = HikeSessionEngine.pause(HikeSessionEngine.start(ready))
        val completed = HikeSessionState(
            status = HikeSessionStatus.COMPLETED,
            reachedCheckpointIndex = plan.checkpoints.lastIndex
        )
        val fix = HikeLocationFix(
            distanceAlongRouteKm = 6.0,
            crossTrackErrorMeters = 8.0,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = 1_000L
        )

        val readyUpdate = LocationBackedHikeSessionEngine.applyLocationFix(
            plan,
            ready,
            fix,
            nowEpochMillis = 1_000L
        )
        val pausedUpdate = LocationBackedHikeSessionEngine.applyLocationFix(
            plan,
            paused,
            fix,
            nowEpochMillis = 1_000L
        )
        val completedUpdate = LocationBackedHikeSessionEngine.applyLocationFix(
            plan,
            completed,
            fix,
            nowEpochMillis = 1_000L
        )

        assertEquals(LocationBackedHikeStatus.WAITING, readyUpdate.status)
        assertEquals("先开始徒步，再用定位推进检查点。", readyUpdate.caption)
        assertEquals(LocationBackedHikeStatus.WAITING, pausedUpdate.status)
        assertEquals("徒步已暂停，定位不会推进检查点。", pausedUpdate.caption)
        assertEquals(LocationBackedHikeStatus.FINISHED, completedUpdate.status)
        assertEquals("本次路线已完成。", completedUpdate.caption)
        assertEquals(completed, completedUpdate.session)
    }

    @Test
    fun invalidLocationFixDoesNotAdvanceCheckpoint() {
        val active = HikeSessionEngine.start(HikeSessionEngine.ready(plan))
        val invalidFixes = listOf(
            HikeLocationFix(Double.NaN, 8.0, 8.0, 1_000L),
            HikeLocationFix(Double.POSITIVE_INFINITY, 8.0, 8.0, 1_000L),
            HikeLocationFix(-1.0, 8.0, 8.0, 1_000L),
            HikeLocationFix(3.0, Double.NaN, 8.0, 1_000L),
            HikeLocationFix(3.0, -1.0, 8.0, 1_000L),
            HikeLocationFix(3.0, 8.0, Double.POSITIVE_INFINITY, 1_000L),
            HikeLocationFix(3.0, 8.0, -1.0, 1_000L)
        )

        invalidFixes.forEach { fix ->
            val update = LocationBackedHikeSessionEngine.applyLocationFix(
                plan,
                active,
                fix,
                nowEpochMillis = 1_000L
            )

            assertEquals(active, update.session)
            assertEquals(LocationBackedHikeStatus.LOW_ACCURACY, update.status)
            assertEquals("定位数据不可用，暂不推进检查点。", update.caption)
        }
    }

    private fun assertChineseCaption(caption: String) {
        assertTrue(caption.any { character -> character in '\u4e00'..'\u9fff' })
        assertFalse(caption.contains("Location"))
        assertFalse(caption.contains("planned route"))
    }
}
