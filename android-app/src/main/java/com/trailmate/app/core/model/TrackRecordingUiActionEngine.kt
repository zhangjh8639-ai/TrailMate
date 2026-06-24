package com.trailmate.app.core.model

enum class TrackRecordingServiceCommand {
    NONE,
    START,
    PAUSE,
    RESUME,
    FINISH
}

data class TrackRecordingUiActionDecision(
    val serviceCommand: TrackRecordingServiceCommand,
    val trackRecording: TrackRecordingState,
    val shouldPublishTrackRecording: Boolean
)

object TrackRecordingUiActionEngine {
    fun resolvePrimaryAction(
        current: TrackRecordingState,
        nowEpochMillis: Long
    ): TrackRecordingUiActionDecision =
        when (current.status) {
            TrackRecordingStatus.IDLE,
            TrackRecordingStatus.FINISHED -> TrackRecordingUiActionDecision(
                serviceCommand = TrackRecordingServiceCommand.START,
                trackRecording = current,
                shouldPublishTrackRecording = false
            )
            TrackRecordingStatus.PAUSED -> TrackRecordingUiActionDecision(
                serviceCommand = TrackRecordingServiceCommand.RESUME,
                trackRecording = current,
                shouldPublishTrackRecording = false
            )
            TrackRecordingStatus.RECORDING -> TrackRecordingUiActionDecision(
                serviceCommand = TrackRecordingServiceCommand.PAUSE,
                trackRecording = TrackRecordingEngine.pause(current, nowEpochMillis),
                shouldPublishTrackRecording = true
            )
        }

    fun resolveFinishAction(
        current: TrackRecordingState,
        nowEpochMillis: Long
    ): TrackRecordingUiActionDecision =
        TrackRecordingUiActionDecision(
            serviceCommand = TrackRecordingServiceCommand.FINISH,
            trackRecording = TrackRecordingEngine.finish(current, nowEpochMillis),
            shouldPublishTrackRecording = current.status == TrackRecordingStatus.RECORDING ||
                current.status == TrackRecordingStatus.PAUSED
        )
}
