package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BacktrackBreadcrumbGuidanceEngineTest {
    @Test
    fun freshRecordingWithMeaningfulTrackIsReadyForBacktrackingReference() {
        val presentation = BacktrackBreadcrumbGuidanceEngine.present(
            trackRecording = recording(
                totalDistanceKm = 0.42,
                points = listOf(pointAt(10_000L), pointAt(70_000L))
            ),
            nowEpochMillis = 100_000L
        )

        assertTrue(presentation.visible)
        assertEquals("原路参照", presentation.title)
        assertEquals("原路参照可用", presentation.statusLabel)
        assertEquals(BacktrackBreadcrumbGuidanceTone.READY, presentation.tone)
        assertEquals("查看实走轨迹", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("沿已记录轨迹"))
        assertTrue(presentation.caption.contains("不要抄近路"))
        assertTrue(presentation.details.any { detail -> detail.label == "已记录" && detail.value == "0.4 km" })
        assertTrue(presentation.details.any { detail -> detail.label == "轨迹点" && detail.value == "2 个" })
        assertNoUnsafeClaims(presentation)
    }

    @Test
    fun activeRecordingWithTooLittleTrackShowsWarmupState() {
        val presentation = BacktrackBreadcrumbGuidanceEngine.present(
            trackRecording = recording(
                totalDistanceKm = 0.04,
                points = listOf(pointAt(20_000L))
            ),
            nowEpochMillis = 90_000L
        )

        assertTrue(presentation.visible)
        assertEquals("正在形成", presentation.statusLabel)
        assertEquals(BacktrackBreadcrumbGuidanceTone.CAUTION, presentation.tone)
        assertTrue(presentation.caption.contains("轨迹还不够"))
        assertTrue(presentation.caption.contains("不要把它当作撤退依据"))
        assertNoUnsafeClaims(presentation)
    }

    @Test
    fun staleLatestPointWarnsBeforeUsingBreadcrumb() {
        val presentation = BacktrackBreadcrumbGuidanceEngine.present(
            trackRecording = recording(
                totalDistanceKm = 1.2,
                points = listOf(pointAt(10_000L), pointAt(70_000L))
            ),
            nowEpochMillis = 371_001L
        )

        assertTrue(presentation.visible)
        assertEquals("轨迹已停更", presentation.statusLabel)
        assertEquals(BacktrackBreadcrumbGuidanceTone.ALERT, presentation.tone)
        assertTrue(presentation.caption.contains("刷新定位"))
        assertTrue(presentation.caption.contains("离线地图"))
        assertTrue(presentation.caption.contains("路标"))
        assertTrue(presentation.details.any { detail -> detail.label == "最近轨迹" && detail.value == "5 分钟前" })
        assertNoUnsafeClaims(presentation)
    }

    @Test
    fun staleLatestPointWarnsEvenWhenRecordedDistanceIsShort() {
        val presentation = BacktrackBreadcrumbGuidanceEngine.present(
            trackRecording = recording(
                totalDistanceKm = 0.04,
                points = listOf(pointAt(10_000L), pointAt(70_000L))
            ),
            nowEpochMillis = 371_001L
        )

        assertTrue(presentation.visible)
        assertEquals("轨迹已停更", presentation.statusLabel)
        assertEquals(BacktrackBreadcrumbGuidanceTone.ALERT, presentation.tone)
        assertTrue(presentation.caption.contains("刷新定位"))
        assertNoUnsafeClaims(presentation)
    }

    @Test
    fun futureLatestPointDoesNotMakeBreadcrumbReady() {
        val presentation = BacktrackBreadcrumbGuidanceEngine.present(
            trackRecording = recording(
                totalDistanceKm = 0.42,
                points = listOf(pointAt(10_000L), pointAt(NOW_EPOCH_MILLIS + 1L))
            ),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertTrue(presentation.visible)
        assertEquals("轨迹时间异常", presentation.statusLabel)
        assertEquals(BacktrackBreadcrumbGuidanceTone.ALERT, presentation.tone)
        assertTrue(presentation.caption.contains("时间异常"))
        assertTrue(presentation.details.any { detail -> detail.label == "最近轨迹" && detail.value == "时间异常" })
        assertNoUnsafeClaims(presentation)
    }

    @Test
    fun pausedRecordingStatesBreadcrumbOnlyCoversRecordedSection() {
        val presentation = BacktrackBreadcrumbGuidanceEngine.present(
            trackRecording = recording(
                status = TrackRecordingStatus.PAUSED,
                totalDistanceKm = 0.8,
                points = listOf(pointAt(10_000L), pointAt(60_000L))
            ),
            nowEpochMillis = 120_000L
        )

        assertTrue(presentation.visible)
        assertEquals("记录已暂停", presentation.statusLabel)
        assertEquals(BacktrackBreadcrumbGuidanceTone.CAUTION, presentation.tone)
        assertTrue(presentation.caption.contains("暂停前"))
        assertTrue(presentation.caption.contains("暂停后的移动不会被覆盖"))
        assertNoUnsafeClaims(presentation)
    }

    @Test
    fun idleOrEmptyTrackStatesNoBreadcrumbEvidence() {
        listOf(
            TrackRecordingState(),
            recording(points = emptyList(), totalDistanceKm = 0.0)
        ).forEach { state ->
            val presentation = BacktrackBreadcrumbGuidanceEngine.present(
                trackRecording = state,
                nowEpochMillis = 120_000L
            )

            assertTrue(presentation.visible)
            assertEquals("无实走轨迹", presentation.statusLabel)
            assertEquals(BacktrackBreadcrumbGuidanceTone.UNAVAILABLE, presentation.tone)
            assertTrue(presentation.caption.contains("没有可用于原路返回的实走轨迹"))
            assertTrue(presentation.caption.contains("离线地图"))
            assertNoUnsafeClaims(presentation)
        }
    }

    @Test
    fun finishedRecordingDoesNotClaimCurrentBacktrackingCoverage() {
        val presentation = BacktrackBreadcrumbGuidanceEngine.present(
            trackRecording = recording(
                status = TrackRecordingStatus.FINISHED,
                totalDistanceKm = 2.0,
                points = listOf(pointAt(10_000L), pointAt(90_000L))
            ),
            nowEpochMillis = 120_000L
        )

        assertTrue(presentation.visible)
        assertEquals("轨迹已保存", presentation.statusLabel)
        assertEquals(BacktrackBreadcrumbGuidanceTone.CAUTION, presentation.tone)
        assertTrue(presentation.caption.contains("用于复盘"))
        assertTrue(presentation.caption.contains("不代表当前位置"))
        assertNoUnsafeClaims(presentation)
    }

    private fun recording(
        status: TrackRecordingStatus = TrackRecordingStatus.RECORDING,
        totalDistanceKm: Double = 0.2,
        points: List<RecordedTrackPoint> = listOf(pointAt(10_000L), pointAt(70_000L))
    ): TrackRecordingState =
        TrackRecordingState(
            status = status,
            points = points,
            totalDistanceKm = totalDistanceKm
        )

    private fun pointAt(timestampEpochMillis: Long): RecordedTrackPoint =
        RecordedTrackPoint(
            latitude = 30.0 + timestampEpochMillis / 10_000_000.0,
            longitude = 120.0,
            elevationMeters = null,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = timestampEpochMillis
        )

    private fun assertNoUnsafeClaims(presentation: BacktrackBreadcrumbGuidancePresentation) {
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
        assertFalse(text.contains("最近公路"))
        assertFalse(text.contains("最近出口"))
        assertFalse(text.contains("自动联系"))
        assertFalse(text.contains("自动救援"))
        assertFalse(text.contains("保证安全"))
        assertFalse(text.contains("医学"))
    }

    private companion object {
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
