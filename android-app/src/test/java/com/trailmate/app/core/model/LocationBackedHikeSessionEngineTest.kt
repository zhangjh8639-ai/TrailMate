package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationBackedHikeSessionEngineTest {
    private val plan = HikePlanSummary(
        checkpoints = listOf(
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.START,
                title = "Start",
                distanceKm = 0.0,
                timeFromStart = "0:00",
                note = "Trailhead."
            ),
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.ENERGY_CHECK,
                title = "Energy check",
                distanceKm = 3.0,
                timeFromStart = "1:00",
                note = "Check food and water."
            ),
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.FINISH,
                title = "Finish",
                distanceKm = 6.0,
                timeFromStart = "2:00",
                note = "Finish."
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
            )
        )

        assertEquals(HikeSessionStatus.ACTIVE, update.session.status)
        assertEquals(1, update.session.reachedCheckpointIndex)
        assertEquals(LocationBackedHikeStatus.ON_ROUTE, update.status)
        assertTrue(update.caption.contains("Energy check"))
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
            )
        )

        assertEquals(0, update.session.reachedCheckpointIndex)
        assertEquals(LocationBackedHikeStatus.LOW_ACCURACY, update.status)
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
            )
        )

        assertEquals(0, update.session.reachedCheckpointIndex)
        assertEquals(LocationBackedHikeStatus.CHECK_ROUTE, update.status)
        assertTrue(update.caption.contains("110 m"))
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
            )
        )

        assertEquals(HikeSessionStatus.COMPLETED, update.session.status)
        assertEquals(LocationBackedHikeStatus.FINISHED, update.status)
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

        val readyUpdate = LocationBackedHikeSessionEngine.applyLocationFix(plan, ready, fix)
        val pausedUpdate = LocationBackedHikeSessionEngine.applyLocationFix(plan, paused, fix)
        val completedUpdate = LocationBackedHikeSessionEngine.applyLocationFix(plan, completed, fix)

        assertEquals(LocationBackedHikeStatus.WAITING, readyUpdate.status)
        assertTrue(readyUpdate.caption.contains("Start"))
        assertEquals(LocationBackedHikeStatus.WAITING, pausedUpdate.status)
        assertTrue(pausedUpdate.caption.contains("paused"))
        assertEquals(LocationBackedHikeStatus.FINISHED, completedUpdate.status)
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
            val update = LocationBackedHikeSessionEngine.applyLocationFix(plan, active, fix)

            assertEquals(active, update.session)
            assertEquals(LocationBackedHikeStatus.LOW_ACCURACY, update.status)
        }
    }
}
