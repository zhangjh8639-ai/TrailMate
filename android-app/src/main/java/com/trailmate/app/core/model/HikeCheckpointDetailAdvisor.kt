package com.trailmate.app.core.model

import java.util.Locale

data class HikeCheckpointDetail(
    val distanceLabel: String,
    val etaLabel: String,
    val readinessLabel: String,
    val readinessCaption: String,
    val actionTitle: String
)

object HikeCheckpointDetailAdvisor {
    fun build(
        checkpoint: HikePlanCheckpoint,
        trackRecording: TrackRecordingState,
        gearRecommendations: List<GearRecommendation>
    ): HikeCheckpointDetail {
        val missingGear = gearRecommendations.filter { recommendation ->
            recommendation.status == GearStatus.MISSING
        }
        val waterGap = gearRecommendations.firstOrNull { recommendation ->
            recommendation.status == GearStatus.MISSING && recommendation.category.contains("水")
        }
        val readiness = when (checkpoint.type) {
            HikePlanCheckpointType.ENERGY_CHECK -> if (waterGap != null) {
                "补给未齐" to "${waterGap.category}未加入装备清单，先保守推进。"
            } else {
                "补给可用" to "按计划补水补能，复核体感和配速。"
            }
            HikePlanCheckpointType.RISK_CHECK -> if (missingGear.isNotEmpty()) {
                "装备缺口" to "仍有 ${missingGear.size} 项缺失，风险点前先复核。"
            } else {
                "装备已覆盖" to "风险点前复核天气、路况和撤退选择。"
            }
            HikePlanCheckpointType.REST_CHECK ->
                "体感优先" to "如果心率或腿部负担升高，安排 5-10 分钟短休。"
            HikePlanCheckpointType.START ->
                "准备出发" to "确认离线路线、定位、通知和基础装备后再开始。"
            HikePlanCheckpointType.FINISH ->
                "准备收尾" to "结束记录后保存轨迹，用于复盘和能力证据。"
        }

        return HikeCheckpointDetail(
            distanceLabel = checkpoint.distanceLabel(trackRecording),
            etaLabel = checkpoint.timeFromStart,
            readinessLabel = readiness.first,
            readinessCaption = readiness.second,
            actionTitle = checkpoint.actionTitle()
        )
    }

    private fun HikePlanCheckpoint.distanceLabel(trackRecording: TrackRecordingState): String =
        if (trackRecording.status == TrackRecordingStatus.RECORDING ||
            trackRecording.status == TrackRecordingStatus.PAUSED ||
            trackRecording.status == TrackRecordingStatus.FINISHED
        ) {
            val remainingKm = (distanceKm - trackRecording.totalDistanceKm).coerceAtLeast(0.0)
            "剩余 ${remainingKm.oneDecimal()}km"
        } else {
            "位置 ${distanceKm.oneDecimal()}km"
        }

    private fun HikePlanCheckpoint.actionTitle(): String =
        when (type) {
            HikePlanCheckpointType.START -> "确认离线路线与装备状态"
            HikePlanCheckpointType.ENERGY_CHECK -> "补水、补能量，复核配速"
            HikePlanCheckpointType.REST_CHECK -> "根据体感安排短休"
            HikePlanCheckpointType.RISK_CHECK -> "停下复核风险和天气"
            HikePlanCheckpointType.FINISH -> "结束记录并保存复盘"
        }

    private fun Double.oneDecimal(): String =
        String.format(Locale.US, "%.1f", this)
}
