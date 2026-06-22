package com.trailmate.app.core.map

import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrailMapUserLocationOverlayPolicyTest {
    @Test
    fun resolvesOverlayFromCurrentGpsFix() {
        val overlay = TrailMapUserLocationOverlayPolicy.resolve(
            gpsEnabled = true,
            locationSnapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOCATED,
                latitude = 30.2381,
                longitude = 120.1121,
                elevationMeters = 112.0,
                horizontalAccuracyMeters = 5.0,
                timestampEpochMillis = NOW_EPOCH_MILLIS
            ),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(30.2381, overlay?.latitude)
        assertEquals(120.1121, overlay?.longitude)
        assertEquals(5.0, overlay?.accuracyMeters)
        assertEquals("当前位置", overlay?.title)
        assertEquals(TrailMapUserLocationConfidence.PRECISE, overlay?.confidence)
        assertEquals("精度约 5 m", overlay?.accuracyLabel)
    }

    @Test
    fun marksLowAccuracyFixAsApproximateLocation() {
        val overlay = TrailMapUserLocationOverlayPolicy.resolve(
            gpsEnabled = true,
            locationSnapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOW_ACCURACY,
                latitude = 30.2381,
                longitude = 120.1121,
                elevationMeters = null,
                horizontalAccuracyMeters = 118.0,
                timestampEpochMillis = NOW_EPOCH_MILLIS
            ),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("大致位置", overlay?.title)
        assertEquals(TrailMapUserLocationConfidence.APPROXIMATE, overlay?.confidence)
        assertEquals("精度较低 · 约 118 m", overlay?.accuracyLabel)
    }

    @Test
    fun marksLocatedFixWithoutAccuracyAsApproximateLocation() {
        val overlay = TrailMapUserLocationOverlayPolicy.resolve(
            gpsEnabled = true,
            locationSnapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOCATED,
                latitude = 30.2381,
                longitude = 120.1121,
                elevationMeters = null,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = NOW_EPOCH_MILLIS
            ),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("大致位置", overlay?.title)
        assertEquals(TrailMapUserLocationConfidence.APPROXIMATE, overlay?.confidence)
        assertEquals("定位精度待确认", overlay?.accuracyLabel)
    }

    @Test
    fun marksStaleLocatedFixAsApproximateLocation() {
        val overlay = TrailMapUserLocationOverlayPolicy.resolve(
            gpsEnabled = true,
            locationSnapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOCATED,
                latitude = 30.2381,
                longitude = 120.1121,
                elevationMeters = null,
                horizontalAccuracyMeters = 5.0,
                timestampEpochMillis = NOW_EPOCH_MILLIS - 120_000L
            ),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("大致位置", overlay?.title)
        assertEquals(TrailMapUserLocationConfidence.APPROXIMATE, overlay?.confidence)
        assertEquals("定位已过期 · 2 分钟前", overlay?.accuracyLabel)
    }

    @Test
    fun hidesOverlayWhenGpsIsOffOrFixIsNotUsable() {
        val located = TrailMateLocationSnapshot(
            status = TrailMateLocationStatus.LOCATED,
            latitude = 30.2381,
            longitude = 120.1121,
            elevationMeters = null,
            horizontalAccuracyMeters = 5.0,
            timestampEpochMillis = 1_000L
        )

        assertNull(
            TrailMapUserLocationOverlayPolicy.resolve(
                gpsEnabled = false,
                locationSnapshot = located,
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
        assertNull(
            TrailMapUserLocationOverlayPolicy.resolve(
                gpsEnabled = true,
                locationSnapshot = TrailMateLocationSnapshot.searching(),
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
        assertNull(
            TrailMapUserLocationOverlayPolicy.resolve(
                gpsEnabled = true,
                locationSnapshot = located.copy(latitude = Double.NaN),
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
        assertNull(
            TrailMapUserLocationOverlayPolicy.resolve(
                gpsEnabled = true,
                locationSnapshot = located.copy(latitude = 91.0),
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
        assertNull(
            TrailMapUserLocationOverlayPolicy.resolve(
                gpsEnabled = true,
                locationSnapshot = located.copy(longitude = 181.0),
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
    }

    private companion object {
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
