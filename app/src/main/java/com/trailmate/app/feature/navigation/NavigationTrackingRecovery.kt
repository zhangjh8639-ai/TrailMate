package com.trailmate.app.feature.navigation

import com.trailmate.app.core.model.NavigationSessionId
import java.time.Instant

data class TrackingRecoveryDecision(
    val recoveredSession: NavigationRecoveredTrackingSessionState?,
    val effect: TrackingRecoveryEffect?,
    val stopForegroundTrackingService: Boolean,
)

sealed class TrackingRecoveryEffect {
    data class EndRecoveredSession(
        val sessionId: NavigationSessionId,
        val endedAt: Instant,
    ) : TrackingRecoveryEffect()
}

class NavigationTrackingRecoveryReducer {
    fun onEndRecoveredSessionClicked(
        sessionId: NavigationSessionId,
        endedAt: Instant,
    ): TrackingRecoveryDecision =
        TrackingRecoveryDecision(
            recoveredSession = null,
            effect = TrackingRecoveryEffect.EndRecoveredSession(
                sessionId = sessionId,
                endedAt = endedAt,
            ),
            stopForegroundTrackingService = true,
        )
}
