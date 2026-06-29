package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsSignalLossWatchEngineTest {
    @Test
    fun freshLocatedFixWhileRecordingHidesWarning() {
        val presentation = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 45_000L
        )

        assertFalse(presentation.visible)
        assertEquals("定位正常", presentation.statusLabel)
    }

    @Test
    fun staleLocatedFixWhileRecordingShowsCaution() {
        val presentation = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 121_000L
        )

        assertTrue(presentation.visible)
        assertEquals("定位停更", presentation.title)
        assertEquals("等待新定位", presentation.statusLabel)
        assertEquals(GpsSignalLossWatchTone.CAUTION, presentation.tone)
        assertEquals("刷新定位", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("超过 1 分钟"))
        assertTrue(presentation.caption.contains("停下"))
        assertTrue(presentation.details.any { detail -> detail.label == "最近定位" && detail.value == "2 分钟前" })
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun severelyStaleFixWhileRecordingShowsAlert() {
        val presentation = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 6 * 60_000L + 1_000L
        )

        assertTrue(presentation.visible)
        assertEquals("定位失联", presentation.title)
        assertEquals("位置已过期", presentation.statusLabel)
        assertEquals(GpsSignalLossWatchTone.ALERT, presentation.tone)
        assertTrue(presentation.caption.contains("超过 5 分钟"))
        assertTrue(presentation.caption.contains("不要把旧位置当作当前位置"))
        assertTrue(presentation.caption.contains("离线地图"))
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun locatedFixThresholdsMatchStaleAndAlertBoundaries() {
        val exactlyFresh = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 61_000L
        )
        val justStale = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 61_001L
        )
        val exactlyAlert = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 301_000L
        )

        assertFalse(exactlyFresh.visible)
        assertEquals("定位正常", exactlyFresh.statusLabel)
        assertTrue(justStale.visible)
        assertEquals(GpsSignalLossWatchTone.CAUTION, justStale.tone)
        assertTrue(exactlyAlert.visible)
        assertEquals(GpsSignalLossWatchTone.ALERT, exactlyAlert.tone)
    }

    @Test
    fun staleLowAccuracyFixStillShowsSignalLossWarning() {
        val presentation = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(status = TrailMateLocationStatus.LOW_ACCURACY, timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 130_000L
        )

        assertTrue(presentation.visible)
        assertEquals("等待新定位", presentation.statusLabel)
        assertTrue(presentation.details.any { detail -> detail.label == "定位状态" && detail.value == "精度偏低" })
    }

    @Test
    fun invalidLocatedTimestampWhileRecordingShowsTimestampWarning() {
        val presentation = GpsSignalLossWatchEngine.present(
            snapshot = locatedAt(timestamp = NOW_EPOCH_MILLIS + 1L),
            trackRecording = recording(),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertTrue(presentation.visible)
        assertEquals("定位时间异常", presentation.title)
        assertEquals("需重新定位", presentation.statusLabel)
        assertEquals(GpsSignalLossWatchTone.ALERT, presentation.tone)
        assertTrue(presentation.caption.contains("定位点时间无效或来自未来"))
        assertTrue(presentation.details.any { detail -> detail.label == "定位时间" && detail.value == "时间异常" })
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun blockedLocationWhileRecordingShowsAlert() {
        val blockedStatuses = listOf(
            TrailMateLocationStatus.DISABLED,
            TrailMateLocationStatus.PERMISSION_REQUIRED,
            TrailMateLocationStatus.PROVIDER_DISABLED,
            TrailMateLocationStatus.UNAVAILABLE
        )

        blockedStatuses.forEach { status ->
            val presentation = GpsSignalLossWatchEngine.present(
                snapshot = emptySnapshot(status = status, timestamp = 1_000L),
                trackRecording = recording(),
                nowEpochMillis = 30_000L
            )

            assertTrue(presentation.visible)
            assertEquals("定位中断", presentation.title)
            assertEquals("无法可靠记录", presentation.statusLabel)
            assertEquals(GpsSignalLossWatchTone.ALERT, presentation.tone)
            assertEquals("恢复定位", presentation.primaryActionLabel)
            assertPolicyBoundaries(presentation)
        }
    }

    @Test
    fun searchingTooLongWhileRecordingShowsWaitingWarning() {
        val presentation = GpsSignalLossWatchEngine.present(
            snapshot = emptySnapshot(status = TrailMateLocationStatus.SEARCHING, timestamp = 1_000L),
            trackRecording = recording(),
            nowEpochMillis = 180_000L
        )

        assertTrue(presentation.visible)
        assertEquals("等待 GPS", presentation.title)
        assertEquals("尚未定位", presentation.statusLabel)
        assertEquals(GpsSignalLossWatchTone.CAUTION, presentation.tone)
        assertTrue(presentation.caption.contains("开阔处"))
        assertTrue(presentation.details.any { detail -> detail.label == "等待时间" && detail.value == "2 分钟" })
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun pausedAndFinishedRecordingSuppressWarning() {
        listOf(
            TrackRecordingStatus.IDLE,
            TrackRecordingStatus.PAUSED,
            TrackRecordingStatus.FINISHED
        ).forEach { status ->
            val presentation = GpsSignalLossWatchEngine.present(
                snapshot = locatedAt(timestamp = 1_000L),
                trackRecording = recording(status = status),
                nowEpochMillis = 10 * 60_000L
            )

            assertFalse(presentation.visible)
            assertEquals("记录未进行", presentation.statusLabel)
        }
    }

    private fun recording(status: TrackRecordingStatus = TrackRecordingStatus.RECORDING): TrackRecordingState =
        TrackRecordingState(status = status)

    private fun locatedAt(
        status: TrailMateLocationStatus = TrailMateLocationStatus.LOCATED,
        timestamp: Long
    ): TrailMateLocationSnapshot =
        TrailMateLocationSnapshot(
            status = status,
            latitude = 30.0,
            longitude = 120.0,
            elevationMeters = 120.0,
            horizontalAccuracyMeters = if (status == TrailMateLocationStatus.LOW_ACCURACY) 88.0 else 8.0,
            timestampEpochMillis = timestamp
        )

    private fun emptySnapshot(status: TrailMateLocationStatus, timestamp: Long): TrailMateLocationSnapshot =
        TrailMateLocationSnapshot(
            status = status,
            latitude = null,
            longitude = null,
            elevationMeters = null,
            horizontalAccuracyMeters = null,
            timestampEpochMillis = timestamp
        )

    private fun assertPolicyBoundaries(presentation: GpsSignalLossWatchPresentation) {
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

    private companion object {
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
