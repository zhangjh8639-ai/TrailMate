package com.trailmate.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateLocationTimestampPolicyTest {
    @Test
    fun preservesPositiveProviderTimestamp() {
        assertEquals(
            1_700_000_060_000L,
            TrailMateLocationTimestampPolicy.fromAndroidProvider(1_700_000_060_000L)
        )
    }

    @Test
    fun preservesZeroProviderTimestampForReliabilityValidation() {
        assertEquals(0L, TrailMateLocationTimestampPolicy.fromAndroidProvider(0L))
    }

    @Test
    fun preservesNegativeProviderTimestampForReliabilityValidation() {
        assertEquals(-1L, TrailMateLocationTimestampPolicy.fromAndroidProvider(-1L))
    }
}
