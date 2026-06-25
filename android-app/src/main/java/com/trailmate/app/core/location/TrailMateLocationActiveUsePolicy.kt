package com.trailmate.app.core.location

data class TrailMateLocationActiveUseDecision(
    val snapshot: TrailMateLocationSnapshot,
    val shouldRestartTracking: Boolean,
    val shouldClearProjectedFix: Boolean
)

object TrailMateLocationActiveUsePolicy {
    fun prepare(
        currentSnapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long,
        maxAccuracyMeters: Double
    ): TrailMateLocationActiveUseDecision {
        val canKeepCurrentFix = TrailMateLocationFixReliability.isReliableForFieldUse(
            snapshot = currentSnapshot,
            nowEpochMillis = nowEpochMillis,
            maxAccuracyMeters = maxAccuracyMeters
        )
        return if (canKeepCurrentFix) {
            TrailMateLocationActiveUseDecision(
                snapshot = currentSnapshot,
                shouldRestartTracking = false,
                shouldClearProjectedFix = false
            )
        } else {
            TrailMateLocationActiveUseDecision(
                snapshot = TrailMateLocationSnapshot.searching(),
                shouldRestartTracking = true,
                shouldClearProjectedFix = true
            )
        }
    }
}
