package com.trailmate.app.core.location

import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackRecordingBroadcastCodecTest {
    @Test
    fun trackRecordingRoundTripsForForegroundServiceBroadcasts() {
        val recording = TrackRecordingEngine.appendLocation(
            state = TrackRecordingEngine.start(
                routeName = "龙井山脊",
                nowEpochMillis = 1_000L
            ),
            point = RecordedTrackPoint(
                latitude = 30.0,
                longitude = 120.0,
                elevationMeters = 100.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_100L
            ),
            nowEpochMillis = 1_100L
        )

        val decoded = TrackRecordingBroadcastCodec.decode(
            TrackRecordingBroadcastCodec.encode(recording)
        )

        assertEquals(TrackRecordingStatus.RECORDING, decoded?.status)
        assertEquals("龙井山脊", decoded?.routeName)
        assertEquals(1, decoded?.points?.size)
        assertEquals(30.0, decoded?.points?.first()?.latitude ?: 0.0, 0.0001)
    }

    @Test
    fun invalidPayloadReturnsNull() {
        assertNull(TrackRecordingBroadcastCodec.decode("not-a-trailmate-snapshot"))
    }

    @Test
    fun idleRecordingRoundTripsForServiceRejectionBroadcasts() {
        val decoded = TrackRecordingBroadcastCodec.decode(
            TrackRecordingBroadcastCodec.encode(TrackRecordingState())
        )

        assertEquals(TrackRecordingStatus.IDLE, decoded?.status)
        assertEquals(0, decoded?.pointCount)
    }
}
