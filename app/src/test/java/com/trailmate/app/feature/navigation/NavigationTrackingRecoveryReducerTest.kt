package com.trailmate.app.feature.navigation

import com.trailmate.app.core.model.NavigationSessionId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTrackingRecoveryReducerTest {
    private val reducer = NavigationTrackingRecoveryReducer()

    @Test
    fun endRecoveredSessionCreatesLocalEndEffectAndClearsRecoveryState() {
        val endedAt = Instant.parse("2026-07-01T02:03:04Z")
        val decision = reducer.onEndRecoveredSessionClicked(
            sessionId = NavigationSessionId("session-1"),
            endedAt = endedAt,
        )

        assertNull(decision.recoveredSession)
        assertEquals(
            TrackingRecoveryEffect.EndRecoveredSession(
                sessionId = NavigationSessionId("session-1"),
                endedAt = endedAt,
            ),
            decision.effect,
        )
        assertTrue(decision.stopForegroundTrackingService)
    }
}
