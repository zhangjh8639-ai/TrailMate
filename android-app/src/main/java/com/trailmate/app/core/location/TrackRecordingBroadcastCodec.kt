package com.trailmate.app.core.location

import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.persistence.TrailMateSnapshot
import com.trailmate.app.core.persistence.TrailMateSnapshotCodec

object TrackRecordingBroadcastCodec {
    fun encode(trackRecording: TrackRecordingState): String =
        TrailMateSnapshotCodec.encode(
            TrailMateSnapshot(latestTrackRecording = trackRecording)
        )

    fun decode(payload: String?): TrackRecordingState? =
        payload
            ?.takeIf { it.contains("version=") }
            ?.let(TrailMateSnapshotCodec::decode)
            ?.latestTrackRecording
}
