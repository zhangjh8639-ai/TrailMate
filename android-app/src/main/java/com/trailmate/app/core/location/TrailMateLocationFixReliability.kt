package com.trailmate.app.core.location

object TrailMateLocationFixReliability {
    const val MAX_RELIABLE_FIX_AGE_MILLIS = 60_000L

    fun isReliableForFieldUse(
        snapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long,
        maxAccuracyMeters: Double
    ): Boolean =
        snapshot.status == TrailMateLocationStatus.LOCATED &&
            snapshot.horizontalAccuracyMeters != null &&
            snapshot.horizontalAccuracyMeters <= maxAccuracyMeters &&
            isFresh(snapshot = snapshot, nowEpochMillis = nowEpochMillis)

    fun isFresh(
        snapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long
    ): Boolean =
        hasValidTimestamp(snapshot = snapshot, nowEpochMillis = nowEpochMillis) &&
            fixAgeMillis(snapshot = snapshot, nowEpochMillis = nowEpochMillis) <= MAX_RELIABLE_FIX_AGE_MILLIS

    fun hasValidTimestamp(
        snapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long
    ): Boolean =
        snapshot.timestampEpochMillis > 0L &&
            snapshot.timestampEpochMillis <= nowEpochMillis

    fun fixAgeMillis(
        snapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long
    ): Long =
        if (!hasValidTimestamp(snapshot = snapshot, nowEpochMillis = nowEpochMillis)) {
            MAX_RELIABLE_FIX_AGE_MILLIS + 1L
        } else {
            nowEpochMillis - snapshot.timestampEpochMillis
        }
}
