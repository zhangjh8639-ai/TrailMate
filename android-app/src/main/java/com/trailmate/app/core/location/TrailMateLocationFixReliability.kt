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
        fixAgeMillis(snapshot = snapshot, nowEpochMillis = nowEpochMillis) <= MAX_RELIABLE_FIX_AGE_MILLIS

    fun fixAgeMillis(
        snapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long
    ): Long =
        (nowEpochMillis - snapshot.timestampEpochMillis).coerceAtLeast(0L)
}
