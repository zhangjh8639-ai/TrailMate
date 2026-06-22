package com.trailmate.app.core.map

import com.trailmate.app.core.location.TrailMateLocationFixReliability
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus

enum class TrailMapUserLocationConfidence {
    PRECISE,
    APPROXIMATE
}

data class TrailMapUserLocationOverlay(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double?,
    val confidence: TrailMapUserLocationConfidence,
    val staleAgeMinutes: Long? = null
) {
    val title: String =
        when (confidence) {
            TrailMapUserLocationConfidence.PRECISE -> "当前位置"
            TrailMapUserLocationConfidence.APPROXIMATE -> "大致位置"
        }

    val accuracyLabel: String =
        when (confidence) {
            TrailMapUserLocationConfidence.PRECISE ->
                accuracyMeters?.let { "精度约 ${it.toInt()} m" } ?: "定位精度待确认"
            TrailMapUserLocationConfidence.APPROXIMATE ->
                staleAgeMinutes?.let { "定位已过期 · $it 分钟前" }
                    ?: accuracyMeters?.let { "精度较低 · 约 ${it.toInt()} m" }
                    ?: "定位精度待确认"
        }
}

object TrailMapUserLocationOverlayPolicy {
    fun resolve(
        gpsEnabled: Boolean,
        locationSnapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): TrailMapUserLocationOverlay? {
        val latitude = locationSnapshot.latitude
        val longitude = locationSnapshot.longitude
        if (!gpsEnabled ||
            latitude == null ||
            longitude == null ||
            !latitude.isValidLatitude() ||
            !longitude.isValidLongitude() ||
            locationSnapshot.status !in visibleStatuses
        ) {
            return null
        }

        return TrailMapUserLocationOverlay(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = locationSnapshot.horizontalAccuracyMeters?.takeIf { it.isFinite() && it > 0.0 },
            confidence = locationSnapshot.userLocationConfidence(nowEpochMillis),
            staleAgeMinutes = locationSnapshot.staleAgeMinutes(nowEpochMillis)
        )
    }

    private fun TrailMateLocationSnapshot.userLocationConfidence(nowEpochMillis: Long): TrailMapUserLocationConfidence =
        if (TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = this,
                nowEpochMillis = nowEpochMillis,
                maxAccuracyMeters = PRECISE_LOCATION_ACCURACY_METERS
            )
        ) {
            TrailMapUserLocationConfidence.PRECISE
        } else {
            TrailMapUserLocationConfidence.APPROXIMATE
        }

    private fun TrailMateLocationSnapshot.staleAgeMinutes(nowEpochMillis: Long): Long? {
        val ageMillis = TrailMateLocationFixReliability.fixAgeMillis(
            snapshot = this,
            nowEpochMillis = nowEpochMillis
        )
        if (ageMillis <= TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS) {
            return null
        }

        return (ageMillis / 60_000L).coerceAtLeast(1L)
    }

    private const val PRECISE_LOCATION_ACCURACY_METERS = 50.0

    private val visibleStatuses = setOf(
        TrailMateLocationStatus.LOCATED,
        TrailMateLocationStatus.LOW_ACCURACY
    )
}

private fun Double.isValidLatitude(): Boolean =
    isFinite() && this in -90.0..90.0

private fun Double.isValidLongitude(): Boolean =
    isFinite() && this in -180.0..180.0
