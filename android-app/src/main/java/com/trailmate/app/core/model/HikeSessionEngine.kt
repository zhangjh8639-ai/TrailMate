package com.trailmate.app.core.model

object HikeSessionEngine {
    fun ready(plan: HikePlanSummary): HikeSessionState =
        HikeSessionState(
            status = if (plan.checkpoints.isEmpty()) HikeSessionStatus.COMPLETED else HikeSessionStatus.READY,
            reachedCheckpointIndex = 0
        )

    fun start(session: HikeSessionState): HikeSessionState =
        when (session.status) {
            HikeSessionStatus.READY,
            HikeSessionStatus.PAUSED -> session.copy(status = HikeSessionStatus.ACTIVE)
            else -> session
        }

    fun pause(session: HikeSessionState): HikeSessionState =
        if (session.status == HikeSessionStatus.ACTIVE) {
            session.copy(status = HikeSessionStatus.PAUSED)
        } else {
            session
        }

    fun resume(session: HikeSessionState): HikeSessionState = start(session)

    fun advance(plan: HikePlanSummary, session: HikeSessionState): HikeSessionState {
        if (session.status != HikeSessionStatus.ACTIVE && session.status != HikeSessionStatus.PAUSED) {
            return session
        }

        val lastIndex = plan.checkpoints.lastIndex
        if (lastIndex < 0) {
            return HikeSessionState(status = HikeSessionStatus.COMPLETED, reachedCheckpointIndex = 0)
        }

        val nextIndex = (session.reachedCheckpointIndex + 1).coerceAtMost(lastIndex)
        return session.copy(
            status = if (nextIndex >= lastIndex) HikeSessionStatus.COMPLETED else session.status,
            reachedCheckpointIndex = nextIndex
        )
    }

    fun currentCheckpoint(plan: HikePlanSummary, session: HikeSessionState): HikePlanCheckpoint? {
        if (plan.checkpoints.isEmpty()) {
            return null
        }

        return plan.checkpoints.getOrNull(session.reachedCheckpointIndex.coerceIn(0, plan.checkpoints.lastIndex))
    }

    fun nextCheckpoint(plan: HikePlanSummary, session: HikeSessionState): HikePlanCheckpoint? =
        plan.checkpoints.getOrNull(session.reachedCheckpointIndex + 1)

    fun progressFraction(plan: HikePlanSummary, session: HikeSessionState): Double {
        val lastIndex = plan.checkpoints.lastIndex
        if (lastIndex <= 0) {
            return 1.0
        }

        return (session.reachedCheckpointIndex.toDouble() / lastIndex).coerceIn(0.0, 1.0)
    }
}
