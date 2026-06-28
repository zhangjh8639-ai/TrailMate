package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DepartureBriefShareEngineTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val now = Instant.parse("2026-06-19T01:00:00Z").toEpochMilli()
    private val plan = DepartureBriefPlan(
        routeName = "龙井山脊",
        distanceKm = 15.2,
        ascentMeters = 860,
        estimatedDurationMinutes = 410
    )

    @Test
    fun sharesDepartureBriefWithoutGps() {
        val presentation = DepartureBriefShareEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(),
            nowEpochMillis = now,
            zoneId = zoneId
        )

        assertEquals("出发报备", presentation.title)
        assertEquals("可发送", presentation.statusLabel)
        assertEquals("发送出发报备", presentation.primaryActionLabel)
        assertEquals("发送出发报备", presentation.chooserTitle)
        assertTrue(presentation.caption.contains("路线计划和预计返回时间"))
        val shareText = requireNotNull(presentation.shareText)
        assertTrue(shareText.contains("TrailMate 出发报备"))
        assertTrue(shareText.contains("路线：龙井山脊"))
        assertTrue(shareText.contains("计划：15.2 km / +860 m"))
        assertTrue(shareText.contains("计划出发：2026-06-19 09:00"))
        assertTrue(shareText.contains("预计完成：2026-06-19 15:50"))
        assertTrue(shareText.contains("超过预计完成 60 分钟仍未联系"))
        assertTrue(shareText.contains("静态行程计划，不是实时追踪链接"))
        assertTrue(shareText.contains("不会自动联系他人或发起救援"))
        assertFalse(shareText.contains("位置："))
        assertFalse(shareText.contains("高德地图"))
        assertEquals(
            listOf(
                DepartureBriefShareDetail(label = "路线", value = "15.2 km / +860 m"),
                DepartureBriefShareDetail(label = "预计完成", value = "2026-06-19 15:50"),
                DepartureBriefShareDetail(label = "确认规则", value = "预计完成 +60 分钟")
            ),
            presentation.details
        )
    }

    @Test
    fun activeRecordingUsesOriginalStartTimeForExpectedFinish() {
        val startedAt = now - 30 * 60_000L
        val presentation = DepartureBriefShareEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                startedAtEpochMillis = startedAt
            ),
            nowEpochMillis = now,
            zoneId = zoneId
        )

        val shareText = requireNotNull(presentation.shareText)
        assertEquals("记录中", presentation.statusLabel)
        assertTrue(shareText.contains("实际出发：2026-06-19 08:30"))
        assertTrue(shareText.contains("预计完成：2026-06-19 15:20"))
        assertFalse(shareText.contains("预计完成：2026-06-19 15:50"))
    }

    @Test
    fun refusesToInventExpectedFinishWhenDurationIsMissing() {
        val presentation = DepartureBriefShareEngine.present(
            plan = plan.copy(estimatedDurationMinutes = null),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = now,
            zoneId = zoneId
        )

        assertEquals("缺少预计用时", presentation.statusLabel)
        assertEquals("查看路线评估", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("无法生成可靠的出发报备"))
        assertNull(presentation.shareText)
        assertFalse(presentation.details.any { it.label == "预计完成" })
    }

    @Test
    fun finishedRouteDoesNotKeepDepartureShareAsPrimaryAction() {
        val presentation = DepartureBriefShareEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.FINISHED,
                startedAtEpochMillis = now,
                finishedAtEpochMillis = now + 380 * 60_000L
            ),
            nowEpochMillis = now + 500 * 60_000L,
            zoneId = zoneId
        )

        assertEquals("已完成", presentation.statusLabel)
        assertEquals("查看复盘", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("行程已完成"))
        assertNull(presentation.shareText)
        assertNull(presentation.chooserTitle)
    }

    @Test
    fun completedRouteSessionDoesNotKeepDepartureShareAsPrimaryAction() {
        val presentation = DepartureBriefShareEngine.present(
            plan = plan,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            routeSessionCompleted = true,
            nowEpochMillis = now,
            zoneId = zoneId
        )

        assertEquals("已完成", presentation.statusLabel)
        assertEquals("查看复盘", presentation.primaryActionLabel)
        assertTrue(presentation.caption.contains("行程已完成"))
        assertNull(presentation.shareText)
        assertNull(presentation.chooserTitle)
    }
}
