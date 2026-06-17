package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HikeSessionEngineTest {
    private val plan = HikePlanEngine.build(
        route = TrailMateSampleData.importedTargetRoute,
        assessment = TrailMateSampleData.routeAssessment
    )

    @Test
    fun readySessionStartsAtTrailheadWithFirstMovingCheckpoint() {
        val session = HikeSessionEngine.ready(plan)

        assertEquals(HikeSessionStatus.READY, session.status)
        assertEquals("Start", HikeSessionEngine.currentCheckpoint(plan, session)?.title)
        assertEquals("Energy check", HikeSessionEngine.nextCheckpoint(plan, session)?.title)
        assertEquals(0.0, HikeSessionEngine.progressFraction(plan, session), 0.0)
    }

    @Test
    fun activeSessionCanPauseResumeAndAdvanceCheckpoints() {
        val active = HikeSessionEngine.start(HikeSessionEngine.ready(plan))
        val paused = HikeSessionEngine.pause(active)
        val resumed = HikeSessionEngine.resume(paused)
        val advanced = HikeSessionEngine.advance(plan, resumed)

        assertEquals(HikeSessionStatus.ACTIVE, active.status)
        assertEquals(HikeSessionStatus.PAUSED, paused.status)
        assertEquals(HikeSessionStatus.ACTIVE, resumed.status)
        assertEquals("Energy check", HikeSessionEngine.currentCheckpoint(plan, advanced)?.title)
        assertEquals("Rest check", HikeSessionEngine.nextCheckpoint(plan, advanced)?.title)
    }

    @Test
    fun advanceIgnoresSessionsThatAreNotInProgress() {
        val ready = HikeSessionEngine.ready(plan)
        val completed = HikeSessionState(
            status = HikeSessionStatus.COMPLETED,
            reachedCheckpointIndex = 0
        )

        assertEquals(ready, HikeSessionEngine.advance(plan, ready))
        assertEquals(completed, HikeSessionEngine.advance(plan, completed))
    }

    @Test
    fun advancingPastFinishCompletesSession() {
        var session = HikeSessionEngine.start(HikeSessionEngine.ready(plan))

        repeat(plan.checkpointCount) {
            session = HikeSessionEngine.advance(plan, session)
        }

        assertEquals(HikeSessionStatus.COMPLETED, session.status)
        assertEquals("Finish", HikeSessionEngine.currentCheckpoint(plan, session)?.title)
        assertNull(HikeSessionEngine.nextCheckpoint(plan, session))
        assertEquals(1.0, HikeSessionEngine.progressFraction(plan, session), 0.0)
    }

    @Test
    fun emptyPlanIsAlreadyCompletedWithoutCheckpoint() {
        val emptyPlan = HikePlanSummary(emptyList())
        val session = HikeSessionEngine.ready(emptyPlan)

        assertEquals(HikeSessionStatus.COMPLETED, session.status)
        assertNull(HikeSessionEngine.currentCheckpoint(emptyPlan, session))
        assertNull(HikeSessionEngine.nextCheckpoint(emptyPlan, session))
        assertEquals(1.0, HikeSessionEngine.progressFraction(emptyPlan, session), 0.0)
    }
}
