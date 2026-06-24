package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecordingUiActionEngineTest {
    @Test
    fun startRequestsServiceWithoutPublishingRecordingBeforeServiceConfirmation() {
        val decision = TrackRecordingUiActionEngine.resolvePrimaryAction(
            current = TrackRecordingState(),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingServiceCommand.START, decision.serviceCommand)
        assertEquals(TrackRecordingStatus.IDLE, decision.trackRecording.status)
        assertFalse(decision.shouldPublishTrackRecording)
    }

    @Test
    fun resumeRequestsServiceWithoutPublishingRecordingBeforeServiceConfirmation() {
        val paused = TrackRecordingState(status = TrackRecordingStatus.PAUSED)

        val decision = TrackRecordingUiActionEngine.resolvePrimaryAction(
            current = paused,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingServiceCommand.RESUME, decision.serviceCommand)
        assertEquals(TrackRecordingStatus.PAUSED, decision.trackRecording.status)
        assertFalse(decision.shouldPublishTrackRecording)
    }

    @Test
    fun pausePublishesPausedStateImmediately() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊",
            startedAtEpochMillis = NOW_EPOCH_MILLIS - 5_000L,
            recordingActiveSinceEpochMillis = NOW_EPOCH_MILLIS - 5_000L
        )

        val decision = TrackRecordingUiActionEngine.resolvePrimaryAction(
            current = recording,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingServiceCommand.PAUSE, decision.serviceCommand)
        assertEquals(TrackRecordingStatus.PAUSED, decision.trackRecording.status)
        assertTrue(decision.shouldPublishTrackRecording)
    }

    @Test
    fun finishPublishesFinishedStateImmediately() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊",
            startedAtEpochMillis = NOW_EPOCH_MILLIS - 5_000L,
            recordingActiveSinceEpochMillis = NOW_EPOCH_MILLIS - 5_000L
        )

        val decision = TrackRecordingUiActionEngine.resolveFinishAction(
            current = recording,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingServiceCommand.FINISH, decision.serviceCommand)
        assertEquals(TrackRecordingStatus.FINISHED, decision.trackRecording.status)
        assertTrue(decision.shouldPublishTrackRecording)
    }

    private companion object {
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
