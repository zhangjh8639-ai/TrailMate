package com.trailmate.app.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecordingForegroundRecoveryPolicyTest {
    @Test
    fun resumesForegroundServiceForPersistedRecordingOnCurrentRoute() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊"
        )

        assertTrue(
            TrackRecordingForegroundRecoveryPolicy.shouldResumeForegroundService(
                current = recording,
                routeName = "龙井山脊",
                alreadyAttempted = false
            )
        )
    }

    @Test
    fun doesNotResumeWhenAlreadyAttemptedOrRouteDoesNotMatch() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊"
        )

        assertFalse(
            TrackRecordingForegroundRecoveryPolicy.shouldResumeForegroundService(
                current = recording,
                routeName = "龙井山脊",
                alreadyAttempted = true
            )
        )
        assertFalse(
            TrackRecordingForegroundRecoveryPolicy.shouldResumeForegroundService(
                current = recording,
                routeName = "别的路线",
                alreadyAttempted = false
            )
        )
    }

    @Test
    fun doesNotResumeWhenRecordingIsNotActive() {
        assertFalse(
            TrackRecordingForegroundRecoveryPolicy.shouldResumeForegroundService(
                current = TrackRecordingState(status = TrackRecordingStatus.PAUSED, routeName = "龙井山脊"),
                routeName = "龙井山脊",
                alreadyAttempted = false
            )
        )
        assertFalse(
            TrackRecordingForegroundRecoveryPolicy.shouldResumeForegroundService(
                current = TrackRecordingState(status = TrackRecordingStatus.FINISHED, routeName = "龙井山脊"),
                routeName = "龙井山脊",
                alreadyAttempted = false
            )
        )
    }
}
