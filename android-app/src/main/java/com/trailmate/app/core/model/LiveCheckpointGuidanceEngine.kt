package com.trailmate.app.core.model

data class LiveCheckpointGuidance(
    val title: String,
    val distanceLabel: String,
    val readinessLabel: String,
    val caption: String
)

object LiveCheckpointGuidanceEngine {
    fun build(
        plan: HikePlanSummary,
        session: HikeSessionState,
        trackRecording: TrackRecordingState,
        gearRecommendations: List<GearRecommendation>
    ): LiveCheckpointGuidance {
        val next = HikeSessionEngine.nextCheckpoint(plan, session)
        if (next != null) {
            val detail = HikeCheckpointDetailAdvisor.build(
                checkpoint = next,
                trackRecording = trackRecording,
                gearRecommendations = gearRecommendations
            )
            return LiveCheckpointGuidance(
                title = "下一提示：${next.title}",
                distanceLabel = detail.distanceLabel,
                readinessLabel = detail.readinessLabel,
                caption = "${detail.actionTitle} · ${detail.readinessCaption}"
            )
        }

        val current = HikeSessionEngine.currentCheckpoint(plan, session)
            ?: return LiveCheckpointGuidance(
                title = "暂无提示点",
                distanceLabel = "等待路线",
                readinessLabel = "待规划",
                caption = "导入 GPX 后生成路线提示。"
            )
        val detail = HikeCheckpointDetailAdvisor.build(
            checkpoint = current,
            trackRecording = trackRecording,
            gearRecommendations = gearRecommendations
        )

        return LiveCheckpointGuidance(
            title = if (session.status == HikeSessionStatus.COMPLETED) {
                "路线已完成"
            } else {
                "当前提示：${current.title}"
            },
            distanceLabel = if (session.status == HikeSessionStatus.COMPLETED) {
                "已到达终点"
            } else {
                detail.distanceLabel
            },
            readinessLabel = detail.readinessLabel,
            caption = "${detail.actionTitle} · ${detail.readinessCaption}"
        )
    }
}
