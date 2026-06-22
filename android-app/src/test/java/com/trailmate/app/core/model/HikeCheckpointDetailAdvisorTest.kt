package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HikeCheckpointDetailAdvisorTest {
    @Test
    fun energyCheckpointUsesRecordedDistanceAndWaterGap() {
        val detail = HikeCheckpointDetailAdvisor.build(
            checkpoint = HikePlanCheckpoint(
                type = HikePlanCheckpointType.ENERGY_CHECK,
                title = "CP2",
                distanceKm = 5.3,
                timeFromStart = "2:10",
                note = "补给检查。"
            ),
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                totalDistanceKm = 3.2
            ),
            gearRecommendations = listOf(
                GearRecommendation(
                    category = "备用水",
                    status = GearStatus.MISSING,
                    rationale = "路线用时偏长，建议增加余量。"
                )
            )
        )

        assertEquals("剩余 2.1km", detail.distanceLabel)
        assertEquals("补给未齐", detail.readinessLabel)
        assertTrue(detail.readinessCaption.contains("备用水"))
        assertEquals("补水、补能量，复核配速", detail.actionTitle)
    }

    @Test
    fun riskCheckpointHighlightsMissingGearCount() {
        val detail = HikeCheckpointDetailAdvisor.build(
            checkpoint = HikePlanCheckpoint(
                type = HikePlanCheckpointType.RISK_CHECK,
                title = "暴露山脊",
                distanceKm = 9.4,
                timeFromStart = "4:10",
                note = "山脊风大。"
            ),
            trackRecording = TrackRecordingState(),
            gearRecommendations = listOf(
                GearRecommendation("雨衣", GearStatus.COVERED, "已匹配。"),
                GearRecommendation("登山杖", GearStatus.MISSING, "长下坡需要支撑。"),
                GearRecommendation("保暖层", GearStatus.MISSING, "高点停留会冷。")
            )
        )

        assertEquals("位置 9.4km", detail.distanceLabel)
        assertEquals("装备缺口", detail.readinessLabel)
        assertTrue(detail.readinessCaption.contains("2 项"))
        assertEquals("停下复核风险和天气", detail.actionTitle)
    }
}
