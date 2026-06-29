package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackRecordingEngineTest {
    @Test
    fun recordingAppendsUsablePointsAndAccumulatesDistance() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L)

        val ignored = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 120.0,
                timestampEpochMillis = 1_100L
            ),
            nowEpochMillis = 1_100L
        )
        val first = TrackRecordingEngine.appendLocation(
            state = ignored,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = 100.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_200L
            ),
            nowEpochMillis = 1_200L
        )
        val second = TrackRecordingEngine.appendLocation(
            state = first,
            point = RecordedTrackPoint(
                latitude = 30.0100,
                longitude = 120.0000,
                elevationMeters = 120.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 421_200L
            ),
            nowEpochMillis = 421_200L
        )

        assertEquals(TrackRecordingStatus.RECORDING, second.status)
        assertEquals(2, second.points.size)
        assertEquals(1.1, second.totalDistanceKm, 0.1)
    }

    @Test
    fun recordingStartCapturesRouteKeyForStableRouteMonitoring() {
        val started = TrackRecordingEngine.start(
            routeName = "龙井山脊",
            routeKey = "longjing-ridge.gpx|龙井山脊|15.2|860|128",
            nowEpochMillis = 1_000L
        )

        assertEquals("龙井山脊", started.routeName)
        assertEquals("longjing-ridge.gpx|龙井山脊|15.2|860|128", started.routeKey)
    }

    @Test
    fun recordingIgnoresLocationFixesOlderThanRecordingStart() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 2_000L)

        val staleAppend = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_900L
            ),
            nowEpochMillis = 2_000L
        )

        assertEquals(TrackRecordingStatus.RECORDING, staleAppend.status)
        assertEquals(0, staleAppend.points.size)
    }

    @Test
    fun recordingIgnoresFutureLocationFixWithoutPoisoningTrack() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L)

        val futureAppend = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 999_999L
            ),
            nowEpochMillis = 2_000L
        )
        val currentAppend = TrackRecordingEngine.appendLocation(
            state = futureAppend,
            point = RecordedTrackPoint(
                latitude = 30.0005,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 2_100L
            ),
            nowEpochMillis = 2_100L
        )

        assertEquals(0, futureAppend.points.size)
        assertEquals(1, currentAppend.points.size)
        assertEquals(2_100L, currentAppend.points.single().timestampEpochMillis)
    }

    @Test
    fun recordingIgnoresZeroLocationTimestamp() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 0L)

        val zeroTimestampAppend = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 0L
            ),
            nowEpochMillis = 10L
        )

        assertEquals(0, zeroTimestampAppend.points.size)
    }

    @Test
    fun recordingIgnoresLastKnownLocationFromPausedWindowAfterResume() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L)
        val withPoint = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 2_000L
            ),
            nowEpochMillis = 2_000L
        )
        val paused = TrackRecordingEngine.pause(withPoint, nowEpochMillis = 10_000L)
        val resumed = TrackRecordingEngine.resume(paused, nowEpochMillis = 20_000L)

        val pausedWindowAppend = TrackRecordingEngine.appendLocation(
            state = resumed,
            point = RecordedTrackPoint(
                latitude = 30.0100,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 15_000L
            ),
            nowEpochMillis = 20_000L
        )

        assertEquals(TrackRecordingStatus.RECORDING, pausedWindowAppend.status)
        assertEquals(1, pausedWindowAppend.points.size)
        assertEquals(0.0, pausedWindowAppend.totalDistanceKm, 0.0)
    }

    @Test
    fun recordingIgnoresNonIncreasingLocationTimestamps() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L)
        val withPoint = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 2_000L
            ),
            nowEpochMillis = 2_000L
        )

        val outOfOrderAppend = TrackRecordingEngine.appendLocation(
            state = withPoint,
            point = RecordedTrackPoint(
                latitude = 30.0005,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_900L
            ),
            nowEpochMillis = 2_100L
        )

        assertEquals(1, outOfOrderAppend.points.size)
        assertEquals(0.0, outOfOrderAppend.totalDistanceKm, 0.0)
    }

    @Test
    fun recordingIgnoresImplausibleGpsJumpSpeeds() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L)
        val withPoint = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 2_000L
            ),
            nowEpochMillis = 2_000L
        )

        val jumpAppend = TrackRecordingEngine.appendLocation(
            state = withPoint,
            point = RecordedTrackPoint(
                latitude = 30.1000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 5_000L
            ),
            nowEpochMillis = 5_000L
        )

        assertEquals(1, jumpAppend.points.size)
        assertEquals(0.0, jumpAppend.totalDistanceKm, 0.0)
    }

    @Test
    fun pausedAndFinishedRecordingsDoNotAppendPoints() {
        val started = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L)
        val withPoint = TrackRecordingEngine.appendLocation(
            state = started,
            point = RecordedTrackPoint(
                latitude = 30.0000,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_100L
            ),
            nowEpochMillis = 1_100L
        )

        val paused = TrackRecordingEngine.pause(withPoint, nowEpochMillis = 1_200L)
        val pausedAppend = TrackRecordingEngine.appendLocation(
            state = paused,
            point = RecordedTrackPoint(
                latitude = 30.0100,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_300L
            ),
            nowEpochMillis = 1_300L
        )
        val resumed = TrackRecordingEngine.resume(pausedAppend, nowEpochMillis = 1_300L)
        val finished = TrackRecordingEngine.finish(resumed, nowEpochMillis = 1_400L)
        val finishedAppend = TrackRecordingEngine.appendLocation(
            state = finished,
            point = RecordedTrackPoint(
                latitude = 30.0200,
                longitude = 120.0000,
                elevationMeters = null,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_500L
            ),
            nowEpochMillis = 1_500L
        )

        assertEquals(TrackRecordingStatus.PAUSED, pausedAppend.status)
        assertEquals(1, pausedAppend.points.size)
        assertEquals(TrackRecordingStatus.FINISHED, finishedAppend.status)
        assertEquals(1, finishedAppend.points.size)
        assertEquals(1_400L, finishedAppend.finishedAtEpochMillis)
    }
}
