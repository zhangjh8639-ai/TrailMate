package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressSafetyWatchEngineTest {
    private val startedAt = 1_800_000_000_000L
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
    fun missingProgressEvidenceHidesProgressSafetyWatch() {
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = activeRecording(startedAt),
            fix = null,
            nowEpochMillis = startedAt + 120 * 60_000L
        )

        assertFalse(presentation.visible)
        assertEquals(emptyList<ProgressSafetyWatchDetail>(), presentation.details)
    }

    @Test
    fun closeEnoughProgressDoesNotAddSafetyNoise() {
        val now = startedAt + 120 * 60_000L
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = activeRecording(startedAt),
            fix = fixAt(distanceKm = 4.3, nowEpochMillis = now),
            nowEpochMillis = now
        )

        assertFalse(presentation.visible)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
    }

    @Test
    fun exactlyTwentyFivePercentBehindShowsCaution() {
        val now = startedAt + 210 * 60_000L
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = activeRecording(startedAt),
            fix = fixAt(distanceKm = 5.475, nowEpochMillis = now),
            nowEpochMillis = now
        )

        assertTrue(presentation.visible)
        assertEquals("进度偏慢", presentation.statusLabel)
        assertEquals(ProgressSafetyWatchTone.CAUTION, presentation.tone)
    }

    @Test
    fun exactlyFortyPercentBehindWithLongRemainingRouteShowsAlert() {
        val now = startedAt + 198 * 60_000L
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = activeRecording(startedAt),
            fix = fixAt(distanceKm = 4.2, nowEpochMillis = now),
            nowEpochMillis = now
        )

        assertTrue(presentation.visible)
        assertEquals("进度压力高", presentation.statusLabel)
        assertEquals(ProgressSafetyWatchTone.ALERT, presentation.tone)
    }

    @Test
    fun slowProgressWarnsBeforeTheUserPushesFurther() {
        val now = startedAt + 120 * 60_000L
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = activeRecording(startedAt),
            fix = fixAt(distanceKm = 3.3, nowEpochMillis = now),
            nowEpochMillis = now
        )

        assertTrue(presentation.visible)
        assertEquals("体力复核", presentation.title)
        assertEquals("进度偏慢", presentation.statusLabel)
        assertEquals(ProgressSafetyWatchTone.CAUTION, presentation.tone)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("停下"))
        assertTrue(presentation.caption.contains("体力"))
        assertTrue(presentation.caption.contains("天气"))
        assertTrue(presentation.caption.contains("不要为了赶进度"))
        assertEquals("计划进度", presentation.details[0].label)
        assertEquals("实际进度", presentation.details[1].label)
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun severeProgressPressureSuggestsExitAndSafetyShare() {
        val now = startedAt + 210 * 60_000L
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = activeRecording(startedAt),
            fix = fixAt(distanceKm = 3.8, nowEpochMillis = now),
            nowEpochMillis = now
        )

        assertTrue(presentation.visible)
        assertEquals("进度压力高", presentation.statusLabel)
        assertEquals(ProgressSafetyWatchTone.ALERT, presentation.tone)
        assertEquals("分享当前位置", presentation.primaryActionLabel)
        assertTrue(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("缩短路线"))
        assertTrue(presentation.caption.contains("安全退出"))
        assertTrue(presentation.details.any { it.label == "剩余距离" && it.value == "11.4 km" })
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun finishedRouteHidesProgressSafetyWatch() {
        val now = startedAt + 500 * 60_000L
        val presentation = ProgressSafetyWatchEngine.present(
            route = route,
            plan = plan,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.FINISHED,
                startedAtEpochMillis = startedAt,
                totalDistanceKm = 15.1
            ),
            fix = fixAt(distanceKm = 15.1, nowEpochMillis = now),
            nowEpochMillis = now
        )

        assertFalse(presentation.visible)
    }

    @Test
    fun unreliableProgressFixDoesNotShowSafetyWatch() {
        val now = startedAt + 120 * 60_000L
        val unreliableFixes = listOf(
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                timestampEpochMillis = now - 61_000L
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                timestampEpochMillis = now + 1L
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                horizontalAccuracyMeters = 51.0
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                horizontalAccuracyMeters = Double.POSITIVE_INFINITY
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                horizontalAccuracyMeters = -1.0
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                distanceAlongRouteKm = -0.1
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                distanceAlongRouteKm = Double.NaN
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                crossTrackErrorMeters = Double.NaN
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                crossTrackErrorMeters = -1.0
            ),
            fixAt(distanceKm = 3.3, nowEpochMillis = now).copy(
                timestampEpochMillis = 0L
            )
        )

        unreliableFixes.forEach { fix ->
            val presentation = ProgressSafetyWatchEngine.present(
                route = route,
                plan = plan,
                trackRecording = activeRecording(startedAt),
                fix = fix,
                nowEpochMillis = now
            )

            assertFalse(presentation.visible)
        }
    }

    private fun activeRecording(startedAt: Long): TrackRecordingState =
        TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            startedAtEpochMillis = startedAt,
            totalDistanceKm = 0.0
        )

    private fun fixAt(
        distanceKm: Double,
        nowEpochMillis: Long
    ): HikeLocationFix =
        HikeLocationFix(
            distanceAlongRouteKm = distanceKm,
            crossTrackErrorMeters = 8.0,
            horizontalAccuracyMeters = 6.0,
            timestampEpochMillis = nowEpochMillis - 30_000L
        )

    private fun assertPolicyBoundaries(presentation: ProgressSafetyWatchPresentation) {
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
        assertFalse(text.contains("疲劳诊断"))
        assertFalse(text.contains("医学"))
        assertFalse(text.contains("自动联系"))
        assertFalse(text.contains("自动救援"))
        assertFalse(text.contains("保证安全"))
    }
}
