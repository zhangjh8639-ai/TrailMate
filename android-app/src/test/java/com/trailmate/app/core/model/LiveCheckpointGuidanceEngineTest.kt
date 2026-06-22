package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveCheckpointGuidanceEngineTest {
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
                type = HikePlanCheckpointType.FINISH,
                title = "终点",
                distanceKm = 15.2,
                timeFromStart = "7:50",
                note = "结束记录。"
            )
        )
    )

    @Test
    fun activeSessionUsesRecordedDistanceAndGearReadinessForNextCheckpoint() {
        val guidance = LiveCheckpointGuidanceEngine.build(
            plan = plan,
            session = HikeSessionState(
                status = HikeSessionStatus.ACTIVE,
                reachedCheckpointIndex = 0
            ),
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                totalDistanceKm = 3.2
            ),
            gearRecommendations = listOf(
                GearRecommendation(
                    category = "备用水",
                    status = GearStatus.MISSING,
                    rationale = "长距离路线需要更多饮水余量。"
                )
            )
        )

        assertEquals("下一提示：补给检查", guidance.title)
        assertEquals("剩余 2.1km", guidance.distanceLabel)
        assertEquals("补给未齐", guidance.readinessLabel)
        assertTrue(guidance.caption.contains("备用水"))
    }

    @Test
    fun completedSessionShowsFinishGuidance() {
        val guidance = LiveCheckpointGuidanceEngine.build(
            plan = plan,
            session = HikeSessionState(
                status = HikeSessionStatus.COMPLETED,
                reachedCheckpointIndex = 2
            ),
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.FINISHED,
                totalDistanceKm = 15.2
            ),
            gearRecommendations = emptyList()
        )

        assertEquals("路线已完成", guidance.title)
        assertEquals("已到达终点", guidance.distanceLabel)
        assertEquals("准备收尾", guidance.readinessLabel)
        assertTrue(guidance.caption.contains("结束记录"))
    }
}
