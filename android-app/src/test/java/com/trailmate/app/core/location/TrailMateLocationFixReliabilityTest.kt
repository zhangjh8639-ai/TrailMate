package com.trailmate.app.core.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateLocationFixReliabilityTest {
    @Test
    fun futureTimestampIsNotFreshOrReliableForFieldUse() {
        val snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS + 1L)

        assertFalse(
            TrailMateLocationFixReliability.isFresh(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
    }

    @Test
    fun zeroTimestampIsNotFreshOrReliableForFieldUse() {
        val snapshot = reliableSnapshot(timestampEpochMillis = 0L)

        assertFalse(
            TrailMateLocationFixReliability.isFresh(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
    }

    @Test
    fun invalidTimestampAgeExceedsReliableWindow() {
        listOf(0L, NOW_EPOCH_MILLIS + 1L).forEach { timestampEpochMillis ->
            assertEquals(
                TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS + 1L,
                TrailMateLocationFixReliability.fixAgeMillis(
                    snapshot = reliableSnapshot(timestampEpochMillis = timestampEpochMillis),
                    nowEpochMillis = NOW_EPOCH_MILLIS
                )
            )
        }
    }

    @Test
    fun invalidTimestampIsNotValidForFieldUse() {
        listOf(0L, -1L, NOW_EPOCH_MILLIS + 1L).forEach { timestampEpochMillis ->
            assertFalse(
                TrailMateLocationFixReliability.hasValidTimestamp(
                    snapshot = reliableSnapshot(timestampEpochMillis = timestampEpochMillis),
                    nowEpochMillis = NOW_EPOCH_MILLIS
                )
            )
        }
    }

    @Test
    fun currentTimestampRemainsFreshAndReliableForFieldUse() {
        val snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS)

        assertTrue(
            TrailMateLocationFixReliability.isFresh(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
        assertTrue(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
        assertTrue(
            TrailMateLocationFixReliability.hasValidTimestamp(
                snapshot = snapshot,
                nowEpochMillis = NOW_EPOCH_MILLIS
            )
        )
    }

    @Test
    fun malformedAccuracyIsNotReliableForFieldUse() {
        listOf(null, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { accuracyMeters ->
            assertFalse(
                TrailMateLocationFixReliability.isReliableForFieldUse(
                    snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(
                        horizontalAccuracyMeters = accuracyMeters
                    ),
                    nowEpochMillis = NOW_EPOCH_MILLIS,
                    maxAccuracyMeters = 50.0
                )
            )
        }
    }

    @Test
    fun overThresholdAccuracyIsNotReliableForFieldUse() {
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(
                    horizontalAccuracyMeters = 50.1
                ),
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
    }

    @Test
    fun boundaryAccuracyAtThresholdRemainsReliableForFieldUse() {
        assertTrue(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(
                    horizontalAccuracyMeters = 50.0
                ),
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 50.0
            )
        )
    }

    @Test
    fun zeroMaxAccuracyOnlyAcceptsZeroAccuracyForFieldUse() {
        assertTrue(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(
                    horizontalAccuracyMeters = 0.0
                ),
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 0.0
            )
        )
        assertFalse(
            TrailMateLocationFixReliability.isReliableForFieldUse(
                snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS).copy(
                    horizontalAccuracyMeters = 0.1
                ),
                nowEpochMillis = NOW_EPOCH_MILLIS,
                maxAccuracyMeters = 0.0
            )
        )
    }

    @Test
    fun invalidMaxAccuracyThresholdIsNotReliableForFieldUse() {
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { threshold ->
            assertFalse(
                TrailMateLocationFixReliability.isReliableForFieldUse(
                    snapshot = reliableSnapshot(timestampEpochMillis = NOW_EPOCH_MILLIS),
                    nowEpochMillis = NOW_EPOCH_MILLIS,
                    maxAccuracyMeters = threshold
                )
            )
        }
    }

    private fun reliableSnapshot(timestampEpochMillis: Long): TrailMateLocationSnapshot =
        TrailMateLocationSnapshot(
            status = TrailMateLocationStatus.LOCATED,
            latitude = 30.25,
            longitude = 120.15,
            elevationMeters = 142.0,
            horizontalAccuracyMeters = 12.0,
            timestampEpochMillis = timestampEpochMillis
        )

    private companion object {
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
