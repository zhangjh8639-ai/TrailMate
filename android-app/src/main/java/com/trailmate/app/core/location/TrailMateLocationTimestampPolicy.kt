package com.trailmate.app.core.location

internal object TrailMateLocationTimestampPolicy {
    fun fromAndroidProvider(providerTimeEpochMillis: Long): Long =
        providerTimeEpochMillis
}
