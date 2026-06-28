package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LowPowerGuidanceEngineTest {
    @Test
    fun normalBatteryHidesLowPowerGuidance() {
        val presentation = LowPowerGuidanceEngine.present(
            batteryStatus = RouteBatteryStatus.fromPercent(72),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            offlineRouteReady = true,
            offlineBaseMapReady = true
        )

        assertFalse(presentation.visible)
        assertEquals(emptyList<LowPowerGuidanceAction>(), presentation.actions)
        assertFalse(presentation.primaryActionRequestsFinalFix)
    }

    @Test
    fun unknownBatteryHidesLowPowerGuidance() {
        val presentation = LowPowerGuidanceEngine.present(
            batteryStatus = RouteBatteryStatus.UNKNOWN,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            offlineRouteReady = true,
            offlineBaseMapReady = true
        )

        assertFalse(presentation.visible)
        assertEquals(emptyList<LowPowerGuidanceAction>(), presentation.actions)
    }

    @Test
    fun lowBatteryShowsConservativeActionsWithoutDisablingGps() {
        val presentation = LowPowerGuidanceEngine.present(
            batteryStatus = RouteBatteryStatus.fromPercent(24),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            offlineRouteReady = true,
            offlineBaseMapReady = true
        )

        assertTrue(presentation.visible)
        assertEquals("低电量行动建议", presentation.title)
        assertEquals("电量偏低", presentation.statusLabel)
        assertEquals(LowPowerGuidanceTone.CAUTION, presentation.tone)
        assertTrue(presentation.primaryActionRequestsFinalFix)
        assertTrue(presentation.caption.contains("不能延长电量"))
        assertTrue(presentation.caption.contains("不会自动"))
        assertActionTitle(presentation, "降低屏幕使用")
        assertActionTitle(presentation, "确认返程")
        assertActionTitle(presentation, "准备补电")
        assertActionTitle(presentation, "保持离线上下文")
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun criticalBatteryPrioritizesExitAndFinalReliablePosition() {
        val presentation = LowPowerGuidanceEngine.present(
            batteryStatus = RouteBatteryStatus.fromPercent(12),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            offlineRouteReady = true,
            offlineBaseMapReady = true
        )

        assertTrue(presentation.visible)
        assertEquals("电量危险", presentation.statusLabel)
        assertEquals(LowPowerGuidanceTone.ALERT, presentation.tone)
        assertTrue(presentation.primaryActionLabel.contains("最后可靠位置"))
        assertTrue(presentation.primaryActionRequestsFinalFix)
        assertActionTitle(presentation, "立即缩短路线")
        assertActionTitle(presentation, "记录最后位置")
        assertActionTitle(presentation, "连接电源")
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun missingOfflineContextAddsLowBatteryOfflineAction() {
        val presentation = LowPowerGuidanceEngine.present(
            batteryStatus = RouteBatteryStatus.fromPercent(22),
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            offlineRouteReady = false,
            offlineBaseMapReady = false
        )

        assertActionTitle(presentation, "保留离线线索")
        assertTrue(presentation.actions.any { it.caption.contains("不要反复尝试下载") })
        assertPolicyBoundaries(presentation)
    }

    private fun assertActionTitle(
        presentation: LowPowerGuidancePresentation,
        title: String
    ) {
        assertTrue(
            "Expected action title <$title> in ${presentation.actions}",
            presentation.actions.any { it.title == title }
        )
    }

    private fun assertPolicyBoundaries(presentation: LowPowerGuidancePresentation) {
        val text = buildString {
            append(presentation.title)
            append(presentation.statusLabel)
            append(presentation.caption)
            append(presentation.primaryActionLabel)
            presentation.actions.forEach { action ->
                append(action.title)
                append(action.caption)
            }
        }
        assertFalse(text.contains("关闭 GPS"))
        assertFalse(text.contains("自动联系"))
        assertFalse(text.contains("自动救援"))
        assertFalse(text.contains("救援调度"))
    }
}
