package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecordingReviewEngineTest {
    @Test
    fun presentsFinishedRecordingForReviewAndExport() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.FINISHED,
            routeName = "龙井山脊",
            startedAtEpochMillis = 1_000L,
            finishedAtEpochMillis = 421_000L,
            points = listOf(
                RecordedTrackPoint(
                    latitude = 30.0,
                    longitude = 120.0,
                    elevationMeters = 100.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 1_000L
                ),
                RecordedTrackPoint(
                    latitude = 30.01,
                    longitude = 120.0,
                    elevationMeters = 120.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 421_000L
                )
            ),
            totalDistanceKm = 1.12
        )

        val review = TrackRecordingReviewEngine.present(recording)

        assertTrue(review.visible)
        assertEquals("轨迹已保存", review.title)
        assertEquals("龙井山脊", review.routeName)
        assertEquals("1.1 km", review.distanceLabel)
        assertEquals("2 点", review.pointCountLabel)
        assertEquals("7 分", review.durationLabel)
        assertEquals("可在数据页复盘本次路线表现。", review.caption)
        assertEquals("去数据页复盘", review.primaryActionLabel)
    }

    @Test
    fun hidesReviewWhenRecordingHasNoSavedPoints() {
        val review = TrackRecordingReviewEngine.present(
            TrackRecordingState(status = TrackRecordingStatus.FINISHED)
        )

        assertFalse(review.visible)
    }

    @Test
    fun hidesReviewWhenFinishedRecordingHasOnlyOnePoint() {
        val review = TrackRecordingReviewEngine.present(
            TrackRecordingState(
                status = TrackRecordingStatus.FINISHED,
                routeName = "龙井山脊",
                startedAtEpochMillis = 1_000L,
                finishedAtEpochMillis = 61_000L,
                points = listOf(
                    RecordedTrackPoint(
                        latitude = 30.0,
                        longitude = 120.0,
                        elevationMeters = 100.0,
                        horizontalAccuracyMeters = 8.0,
                        timestampEpochMillis = 1_000L
                    )
                ),
                totalDistanceKm = 0.0
            )
        )

        assertFalse(review.visible)
    }

    @Test
    fun hidesReviewWhenFinishedRecordingHasNoMovementDistance() {
        val review = TrackRecordingReviewEngine.present(
            TrackRecordingState(
                status = TrackRecordingStatus.FINISHED,
                routeName = "龙井山脊",
                startedAtEpochMillis = 1_000L,
                finishedAtEpochMillis = 121_000L,
                points = listOf(
                    RecordedTrackPoint(
                        latitude = 30.0,
                        longitude = 120.0,
                        elevationMeters = 100.0,
                        horizontalAccuracyMeters = 8.0,
                        timestampEpochMillis = 1_000L
                    ),
                    RecordedTrackPoint(
                        latitude = 30.0,
                        longitude = 120.0,
                        elevationMeters = 100.0,
                        horizontalAccuracyMeters = 8.0,
                        timestampEpochMillis = 121_000L
                    )
                ),
                totalDistanceKm = 0.0
            )
        )

        assertFalse(review.visible)
    }
}
