package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ReturnEtaWatchEngineTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val startedAt = Instant.parse("2026-06-19T01:00:00Z").toEpochMilli()
    private val plan = ReturnEtaPlan(
        estimatedDurationMinutes = 410
    )

    @Test
    fun waitsForRouteStartBeforeCountingReturnEta() {
        val presentation = ReturnEtaWatchEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            nowEpochMillis = startedAt,
            zoneId = zoneId
        )

        assertEquals("预计返回", presentation.title)
        assertEquals("出发后开始", presentation.statusLabel)
        assertEquals("开始徒步后计算", presentation.primaryActionLabel)
        assertEquals(ReturnEtaWatchTone.NEUTRAL, presentation.tone)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("开始记录后"))
        assertChinese(presentation)
    }

    @Test
    fun activeRouteBeforePlannedFinishShowsRemainingTime() {
        val presentation = ReturnEtaWatchEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                startedAtEpochMillis = startedAt
            ),
            nowEpochMillis = startedAt + 2 * 60 * 60_000L,
            zoneId = zoneId
        )

        assertEquals("预计返回", presentation.title)
        assertEquals("计划内", presentation.statusLabel)
        assertEquals("继续观察", presentation.primaryActionLabel)
        assertEquals(ReturnEtaWatchTone.READY, presentation.tone)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("预计 2026-06-19 15:50 返回"))
        assertEquals(
            listOf(
                ReturnEtaWatchDetail(label = "预计返回", value = "2026-06-19 15:50"),
                ReturnEtaWatchDetail(label = "剩余计划", value = "4h50"),
                ReturnEtaWatchDetail(label = "确认窗口", value = "+60 分钟")
            ),
            presentation.details
        )
        assertChinese(presentation)
    }

    @Test
    fun activeRouteAfterPlannedFinishWarnsWithinGraceWindow() {
        val presentation = ReturnEtaWatchEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                startedAtEpochMillis = startedAt
            ),
            nowEpochMillis = startedAt + 430 * 60_000L,
            zoneId = zoneId
        )

        assertEquals("预计返回", presentation.title)
        assertEquals("已超过计划", presentation.statusLabel)
        assertEquals("分享当前位置", presentation.primaryActionLabel)
        assertEquals(ReturnEtaWatchTone.CAUTION, presentation.tone)
        assertTrue(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("已超过预计返回 20 分钟"))
        assertTrue(presentation.caption.contains("40 分钟后进入逾期确认"))
        assertChinese(presentation)
    }

    @Test
    fun activeRouteAfterGraceWindowEscalatesToOverdueSafetyShare() {
        val presentation = ReturnEtaWatchEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                startedAtEpochMillis = startedAt
            ),
            nowEpochMillis = startedAt + 500 * 60_000L,
            zoneId = zoneId
        )

        assertEquals("预计返回", presentation.title)
        assertEquals("已超过确认时间", presentation.statusLabel)
        assertEquals("分享当前位置", presentation.primaryActionLabel)
        assertEquals(ReturnEtaWatchTone.ALERT, presentation.tone)
        assertTrue(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("已超过确认窗口 30 分钟"))
        assertTrue(presentation.caption.contains("不会自动发送消息"))
        assertChinese(presentation)
    }

    @Test
    fun refusesToInventEtaWhenDurationIsMissing() {
        val presentation = ReturnEtaWatchEngine.present(
            plan = ReturnEtaPlan(estimatedDurationMinutes = null),
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                startedAtEpochMillis = startedAt
            ),
            nowEpochMillis = startedAt + 2 * 60 * 60_000L,
            zoneId = zoneId
        )

        assertEquals("缺少预计时长", presentation.statusLabel)
        assertEquals(ReturnEtaWatchTone.CAUTION, presentation.tone)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("无法计算预计返回时间"))
        assertFalse(presentation.caption.contains("2026-06-19"))
        assertChinese(presentation)
    }

    @Test
    fun finishedRouteShowsWrapUpInsteadOfOverdueWarning() {
        val presentation = ReturnEtaWatchEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.FINISHED,
                startedAtEpochMillis = startedAt,
                finishedAtEpochMillis = startedAt + 380 * 60_000L
            ),
            nowEpochMillis = startedAt + 500 * 60_000L,
            zoneId = zoneId
        )

        assertEquals("已完成", presentation.statusLabel)
        assertEquals("保存复盘", presentation.primaryActionLabel)
        assertEquals(ReturnEtaWatchTone.READY, presentation.tone)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("路线已完成"))
        assertFalse(presentation.caption.contains("逾期"))
        assertChinese(presentation)
    }

    private fun assertChinese(presentation: ReturnEtaWatchPresentation) {
        val text = buildString {
            append(presentation.title)
            append(presentation.statusLabel)
            append(presentation.caption)
            append(presentation.primaryActionLabel)
            presentation.details.forEach {
                append(it.label)
                append(it.value)
            }
        }
        assertTrue(text.any { character -> character in '\u4e00'..'\u9fff' })
        assertFalse(text.contains("overdue"))
        assertFalse(text.contains("ETA"))
    }
}
