package com.trailmate.app.core.location

import com.trailmate.app.core.model.Distance
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationProviderModelsTest {
    @Test
    fun validSystemReadingMapsToNavigationLocationSample() {
        val sample = SystemLocationSampleMapper.map(
            SystemLocationReading(
                latitude = 30.245,
                longitude = 120.116,
                accuracyMeters = 6.5,
                recordedAt = Instant.parse("2026-07-01T01:02:03Z"),
                altitudeMeters = 92.0,
                bearingDegrees = 275.0,
                speedMetersPerSecond = 1.4,
            ),
        )

        assertEquals(30.245, sample.coordinate.latitude, 0.000001)
        assertEquals(120.116, sample.coordinate.longitude, 0.000001)
        assertEquals(92.0, sample.coordinate.elevation?.meters ?: -1.0, 0.1)
        assertEquals(6.5, sample.accuracy.meters, 0.1)
        assertEquals(Instant.parse("2026-07-01T01:02:03Z"), sample.recordedAt)
        assertEquals(275.0, sample.bearingDegrees ?: -1.0, 0.1)
        assertEquals(1.4, sample.speedMetersPerSecond ?: -1.0, 0.1)
    }

    @Test
    fun invalidSystemReadingIsRejectedBeforeNavigation() {
        listOf(
            validReading().copy(latitude = 91.0),
            validReading().copy(latitude = Double.NaN),
            validReading().copy(latitude = Double.POSITIVE_INFINITY),
            validReading().copy(longitude = 181.0),
            validReading().copy(longitude = Double.NaN),
            validReading().copy(accuracyMeters = -1.0),
            validReading().copy(accuracyMeters = Double.NaN),
            validReading().copy(altitudeMeters = Double.POSITIVE_INFINITY),
            validReading().copy(bearingDegrees = Double.NaN),
            validReading().copy(speedMetersPerSecond = -0.1),
            validReading().copy(speedMetersPerSecond = Double.NEGATIVE_INFINITY),
        ).forEach { invalidReading ->
            assertThrows(IllegalArgumentException::class.java) {
                SystemLocationSampleMapper.map(invalidReading)
            }
        }
    }

    @Test
    fun locationRequestRejectsImpossibleSamplingValues() {
        assertThrows(IllegalArgumentException::class.java) {
            LocationProviderRequest(
                minTimeInterval = Duration.ofMillis(-1),
                minDistance = Distance.ZERO,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            LocationProviderRequest(
                minTimeInterval = Duration.ofSeconds(1),
                minDistance = Distance.meters(Double.NaN),
            )
        }
    }

    @Test
    fun stoppedSubscriptionCanBeStoppedRepeatedly() {
        val subscription = StoppedLocationSubscription

        assertTrue(subscription.isStopped)
        subscription.stop()
        subscription.stop()
        assertTrue(subscription.isStopped)
    }

    @Test
    fun observerCanReceiveStatusWithoutLocationSample() {
        val observer = RecordingLocationObserver()

        observer.onProviderStatus(LocationProviderStatus.PermissionDenied)

        assertEquals(LocationProviderStatus.PermissionDenied, observer.statuses.single())
        assertTrue(observer.samples.isEmpty())
        assertFalse(observer.statuses.contains(LocationProviderStatus.Ready))
    }

    private fun validReading(): SystemLocationReading =
        SystemLocationReading(
            latitude = 30.245,
            longitude = 120.116,
            accuracyMeters = 5.0,
            recordedAt = Instant.parse("2026-07-01T01:02:03Z"),
        )
}
