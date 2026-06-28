package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDirectionWatchEngineTest {
    @Test
    fun reliableBackwardMovementShowsDirectionAlert() {
        val presentation = RouteDirectionWatchEngine.present(
            previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L),
            currentFix = fixAt(distanceKm = 5.05, timestamp = 91_000L),
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = recording()
        )

        assertTrue(presentation.visible)
        assertEquals("方向异常", presentation.title)
        assertEquals("可能反向行进", presentation.statusLabel)
        assertEquals(RouteDirectionWatchTone.ALERT, presentation.tone)
        assertEquals("刷新定位核对", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("路线进度倒退约 250 m"))
        assertTrue(presentation.caption.contains("停下"))
        assertTrue(presentation.caption.contains("路标"))
        assertTrue(presentation.details.any { it.label == "倒退距离" && it.value == "250 m" })
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun smallBackwardMovementIsTreatedAsGpsJitter() {
        val presentation = RouteDirectionWatchEngine.present(
            previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L),
            currentFix = fixAt(distanceKm = 5.18, timestamp = 91_000L),
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = recording()
        )

        assertFalse(presentation.visible)
        assertEquals(RouteDirectionWatchTone.READY, presentation.tone)
    }

    @Test
    fun forwardMovementDoesNotShowDirectionWarning() {
        val presentation = RouteDirectionWatchEngine.present(
            previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L),
            currentFix = fixAt(distanceKm = 5.6, timestamp = 91_000L),
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = recording()
        )

        assertFalse(presentation.visible)
        assertEquals("方向正常", presentation.statusLabel)
    }

    @Test
    fun shortSampleWindowDoesNotShowDirectionWarning() {
        val presentation = RouteDirectionWatchEngine.present(
            previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L),
            currentFix = fixAt(distanceKm = 5.05, timestamp = 30_000L),
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = recording()
        )

        assertFalse(presentation.visible)
        assertEquals("样本不足", presentation.statusLabel)
    }

    @Test
    fun staleSampleWindowDoesNotShowDirectionWarning() {
        val presentation = RouteDirectionWatchEngine.present(
            previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L),
            currentFix = fixAt(distanceKm = 5.05, timestamp = 11 * 60_000L),
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = recording()
        )

        assertFalse(presentation.visible)
        assertEquals("方向待确认", presentation.statusLabel)
    }

    @Test
    fun lowConfidenceStatesSuppressDirectionWarning() {
        val previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L)
        val currentFix = fixAt(distanceKm = 5.05, timestamp = 91_000L)
        val suppressedStates = listOf(
            LocationBackedHikeStatus.WAITING,
            LocationBackedHikeStatus.LOW_ACCURACY,
            LocationBackedHikeStatus.CHECK_ROUTE,
            LocationBackedHikeStatus.FINISHED
        )

        suppressedStates.forEach { status ->
            val presentation = RouteDirectionWatchEngine.present(
                previousFix = previousFix,
                currentFix = currentFix,
                locationStatus = status,
                trackRecording = recording()
            )

            assertFalse(presentation.visible)
            assertEquals(RouteDirectionWatchTone.NEUTRAL, presentation.tone)
        }
    }

    @Test
    fun pausedAndFinishedRecordingSuppressDirectionWarning() {
        val previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L)
        val currentFix = fixAt(distanceKm = 5.05, timestamp = 91_000L)

        listOf(
            TrackRecordingStatus.IDLE,
            TrackRecordingStatus.PAUSED,
            TrackRecordingStatus.FINISHED
        ).forEach { status ->
            val presentation = RouteDirectionWatchEngine.present(
                previousFix = previousFix,
                currentFix = currentFix,
                locationStatus = LocationBackedHikeStatus.ON_ROUTE,
                trackRecording = recording(status = status)
            )

            assertFalse(presentation.visible)
            assertEquals("记录未进行", presentation.statusLabel)
        }
    }

    @Test
    fun invalidFixesSuppressDirectionWarning() {
        val invalidFixes = listOf(
            fixAt(distanceKm = Double.NaN, timestamp = 91_000L),
            fixAt(distanceKm = Double.POSITIVE_INFINITY, timestamp = 91_000L),
            fixAt(distanceKm = -0.1, timestamp = 91_000L)
        )

        invalidFixes.forEach { currentFix ->
            val presentation = RouteDirectionWatchEngine.present(
                previousFix = fixAt(distanceKm = 5.3, timestamp = 1_000L),
                currentFix = currentFix,
                locationStatus = LocationBackedHikeStatus.ON_ROUTE,
                trackRecording = recording()
            )

            assertFalse(presentation.visible)
            assertEquals("方向待确认", presentation.statusLabel)
        }
    }

    private fun recording(status: TrackRecordingStatus = TrackRecordingStatus.RECORDING): TrackRecordingState =
        TrackRecordingState(status = status)

    private fun fixAt(distanceKm: Double, timestamp: Long): HikeLocationFix =
        HikeLocationFix(
            distanceAlongRouteKm = distanceKm,
            crossTrackErrorMeters = 8.0,
            horizontalAccuracyMeters = 6.0,
            timestampEpochMillis = timestamp
        )

    private fun assertPolicyBoundaries(presentation: RouteDirectionWatchPresentation) {
        val text = buildString {
            append(presentation.title)
            append(presentation.statusLabel)
            append(presentation.caption)
            append(presentation.primaryActionLabel)
            presentation.details.forEach { detail ->
                append(detail.label)
                append(detail.value)
            }
        }
        assertFalse(text.contains("转弯导航"))
        assertFalse(text.contains("自动联系"))
        assertFalse(text.contains("自动救援"))
        assertFalse(text.contains("保证安全"))
        assertFalse(text.contains("医学"))
    }
}
