package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteExitGuidanceEngineTest {
    private val route = ImportedRoute(
        routeName = "龙井山脊",
        fileName = "longjing.gpx",
        distanceKm = 15.2,
        ascentMeters = 860,
        status = RouteImportStatus.PARSED,
        pointCount = 128
    )
    private val plan = HikePlanSummary(
        checkpoints = listOf(
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.START,
                title = "起点",
                distanceKm = 0.0,
                timeFromStart = "0:00",
                note = "确认起点。"
            ),
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.ENERGY_CHECK,
                title = "补给检查",
                distanceKm = 5.3,
                timeFromStart = "2:10",
                note = "补给检查。"
            ),
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.REST_CHECK,
                title = "休息判断",
                distanceKm = 8.8,
                timeFromStart = "4:30",
                note = "短休复核。"
            ),
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.FINISH,
                title = "终点",
                distanceKm = 15.2,
                timeFromStart = "7:50",
                note = "结束记录。"
            )
        )
    )

    @Test
    fun recommendsBacktrackingWhenStartIsClosestSafeReference() {
        val presentation = RouteExitGuidanceEngine.present(
            route = route,
            plan = plan,
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 2.2,
                crossTrackErrorMeters = 8.0,
                horizontalAccuracyMeters = 6.0,
                timestampEpochMillis = 1_000L
            ),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING, totalDistanceKm = 2.1)
        )

        assertEquals("安全退出", presentation.title)
        assertEquals("建议原路返回", presentation.statusLabel)
        assertEquals("原路返回", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("起点更近"))
        assertEquals(RouteExitGuidanceTone.READY, presentation.tone)
        assertEquals(
            listOf("原路返回", "继续到补给检查"),
            presentation.options.map { option -> option.label }
        )
        assertTrue(presentation.options.first().emphasized)
        assertEquals("2.2 km", presentation.options.first().distanceLabel)
        assertChinese(presentation)
    }

    @Test
    fun recommendsNextCheckpointWhenItIsCloserThanBacktracking() {
        val presentation = RouteExitGuidanceEngine.present(
            route = route,
            plan = plan,
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.0,
                crossTrackErrorMeters = 12.0,
                horizontalAccuracyMeters = 7.0,
                timestampEpochMillis = 1_000L
            ),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING, totalDistanceKm = 4.9)
        )

        assertEquals("安全退出", presentation.title)
        assertEquals("先到下一检查点", presentation.statusLabel)
        assertEquals("前往补给检查", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("补给检查更近"))
        assertEquals(
            listOf("继续到补给检查", "原路返回"),
            presentation.options.map { option -> option.label }
        )
        assertTrue(presentation.options.first().emphasized)
        assertEquals("0.3 km", presentation.options.first().distanceLabel)
        assertChinese(presentation)
    }

    @Test
    fun asksForReliableGpsBeforeChoosingExitDirection() {
        val presentation = RouteExitGuidanceEngine.present(
            route = route,
            plan = plan,
            locationStatus = LocationBackedHikeStatus.LOW_ACCURACY,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.0,
                crossTrackErrorMeters = 12.0,
                horizontalAccuracyMeters = 96.0,
                timestampEpochMillis = 1_000L
            ),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING, totalDistanceKm = 4.9)
        )

        assertEquals("安全退出", presentation.title)
        assertEquals("先稳定定位", presentation.statusLabel)
        assertEquals("重新定位", presentation.primaryActionLabel)
        assertEquals(RouteExitGuidanceTone.CAUTION, presentation.tone)
        assertTrue(presentation.caption.contains("不要凭旧位置判断"))
        assertFalse(presentation.options.any { option -> option.emphasized })
        assertChinese(presentation)
    }

    @Test
    fun asksToResolveOffRouteStateBeforeChoosingExitDirection() {
        val presentation = RouteExitGuidanceEngine.present(
            route = route,
            plan = plan,
            locationStatus = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.0,
                crossTrackErrorMeters = 120.0,
                horizontalAccuracyMeters = 7.0,
                timestampEpochMillis = 1_000L
            ),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING, totalDistanceKm = 4.9)
        )

        assertEquals("先回到路线", presentation.statusLabel)
        assertEquals("重新定位", presentation.primaryActionLabel)
        assertEquals(RouteExitGuidanceTone.CAUTION, presentation.tone)
        assertTrue(presentation.caption.contains("疑似偏离"))
        assertFalse(presentation.options.any { option -> option.emphasized })
        assertFalse(presentation.options.any { option -> option.label.contains("补给检查") })
        assertChinese(presentation)
    }

    @Test
    fun usesFinishAsNextReferenceNearTheEnd() {
        val presentation = RouteExitGuidanceEngine.present(
            route = route,
            plan = plan,
            locationStatus = LocationBackedHikeStatus.ON_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 14.7,
                crossTrackErrorMeters = 10.0,
                horizontalAccuracyMeters = 6.0,
                timestampEpochMillis = 1_000L
            ),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING, totalDistanceKm = 14.6)
        )

        assertEquals("先到终点", presentation.statusLabel)
        assertEquals("前往终点", presentation.primaryActionLabel)
        assertEquals("0.5 km", presentation.options.first().distanceLabel)
        assertTrue(presentation.caption.contains("终点更近"))
        assertChinese(presentation)
    }

    @Test
    fun showsFinishedStateInsteadOfExitDirectionAfterRouteCompletion() {
        val presentation = RouteExitGuidanceEngine.present(
            route = route,
            plan = plan,
            locationStatus = LocationBackedHikeStatus.FINISHED,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 15.2,
                crossTrackErrorMeters = 6.0,
                horizontalAccuracyMeters = 5.0,
                timestampEpochMillis = 1_000L
            ),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.FINISHED, totalDistanceKm = 15.1)
        )

        assertEquals("路线已完成", presentation.statusLabel)
        assertEquals("查看轨迹", presentation.primaryActionLabel)
        assertEquals(RouteExitGuidanceTone.READY, presentation.tone)
        assertFalse(presentation.options.any { option -> option.label.startsWith("继续到") })
        assertChinese(presentation)
    }

    private fun assertChinese(presentation: RouteExitGuidancePresentation) {
        val text = buildString {
            append(presentation.title)
            append(presentation.statusLabel)
            append(presentation.caption)
            append(presentation.primaryActionLabel)
            presentation.options.forEach {
                append(it.label)
                append(it.caption)
            }
        }
        assertTrue(text.any { character -> character in '\u4e00'..'\u9fff' })
        assertFalse(text.contains("exit"))
        assertFalse(text.contains("checkpoint"))
    }
}
