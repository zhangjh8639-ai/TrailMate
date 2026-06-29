package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TrackRecordingState
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordedTrackGpxExporterTest {
    @Test
    fun exportsRecordedTrackPointsAsGpxElevenTrack() {
        val started = TrackRecordingEngine.start(
            routeName = "龙井山脊 & 夜徒",
            nowEpochMillis = 1_000L
        )
        val first = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0,
                longitude = 120.0,
                elevationMeters = 100.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            nowEpochMillis = 1_000L
        )
        val second = TrackRecordingEngine.appendLocation(
            state = first,
            point = RecordedTrackPoint(
                latitude = 30.01,
                longitude = 120.0,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 421_000L
            ),
            nowEpochMillis = 421_000L
        )
        val finished = TrackRecordingEngine.finish(second, nowEpochMillis = 422_000L)

        val gpx = RecordedTrackGpxExporter.export(finished)

        requireNotNull(gpx)
        assertTrue(gpx.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        assertTrue(gpx.contains("<gpx version=\"1.1\" creator=\"TrailMate\""))
        assertTrue(gpx.contains("<name>龙井山脊 &amp; 夜徒</name>"))
        assertTrue(gpx.contains("<trkpt lat=\"30.0\" lon=\"120.0\">"))
        assertTrue(gpx.contains("<ele>100.0</ele>"))
        assertTrue(gpx.contains("<time>1970-01-01T00:00:01Z</time>"))
        assertTrue(gpx.contains("<trkpt lat=\"30.01\" lon=\"120.0\">"))
        assertTrue(gpx.contains("<time>1970-01-01T00:07:01Z</time>"))
    }

    @Test
    fun doesNotExportEmptyTrackRecording() {
        assertNull(RecordedTrackGpxExporter.export(TrackRecordingState()))
    }
}
