package com.trailmate.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateLocationActiveUsePolicyTest {
    @Test
    fun keepsReliableFreshFixWhenStartingActiveNavigation() {
        val current = TrailMateLocationSnapshot(
            status = TrailMateLocationStatus.LOCATED,
            latitude = 30.25,
            longitude = 120.15,
            elevationMeters = 142.0,
            horizontalAccuracyMeters = 13.4,
            timestampEpochMillis = 10_000L
        )

        val decision = TrailMateLocationActiveUsePolicy.prepare(
            currentSnapshot = current,
            nowEpochMillis = 22_000L,
            maxAccuracyMeters = 50.0
        )

        assertEquals(current, decision.snapshot)
        assertFalse(decision.shouldRestartTracking)
        assertFalse(decision.shouldClearProjectedFix)
    }

    @Test
    fun restartsTrackingWhenCurrentFixIsStale() {
        val current = TrailMateLocationSnapshot(
            status = TrailMateLocationStatus.LOCATED,
            latitude = 30.25,
            longitude = 120.15,
            elevationMeters = null,
            horizontalAccuracyMeters = 9.0,
            timestampEpochMillis = 10_000L
        )

        val decision = TrailMateLocationActiveUsePolicy.prepare(
            currentSnapshot = current,
            nowEpochMillis = 80_001L,
            maxAccuracyMeters = 50.0
        )

        assertEquals(TrailMateLocationStatus.SEARCHING, decision.snapshot.status)
        assertTrue(decision.shouldRestartTracking)
        assertTrue(decision.shouldClearProjectedFix)
    }
}
