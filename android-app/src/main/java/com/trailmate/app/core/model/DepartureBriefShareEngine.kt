package com.trailmate.app.core.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DepartureBriefPlan(
    val routeName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val estimatedDurationMinutes: Int?
)

data class DepartureBriefShareDetail(
    val label: String,
    val value: String
)

data class DepartureBriefSharePresentation(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val shareText: String?,
    val chooserTitle: String? = null,
    val details: List<DepartureBriefShareDetail> = emptyList()
)

object DepartureBriefShareEngine {
    fun present(
        plan: DepartureBriefPlan,
        trackRecording: TrackRecordingState,
        routeSessionCompleted: Boolean = false,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DepartureBriefSharePresentation {
        val routeDetail = DepartureBriefShareDetail(label = "路线", value = plan.routeSummary())
        if (trackRecording.status == TrackRecordingStatus.FINISHED || routeSessionCompleted) {
            return DepartureBriefSharePresentation(
                title = TITLE,
                statusLabel = "已完成",
                caption = "行程已完成，出发报备不再作为主要操作；可查看复盘或按需分享当前位置。",
                primaryActionLabel = "查看复盘",
                shareText = null,
                details = listOf(routeDetail)
            )
        }

        val durationMinutes = plan.estimatedDurationMinutes
        if (durationMinutes == null || durationMinutes <= 0) {
            return DepartureBriefSharePresentation(
                title = TITLE,
                statusLabel = "缺少预计用时",
                caption = "当前路线缺少预计用时，无法生成可靠的出发报备；请先完成路线评估。",
                primaryActionLabel = "查看路线评估",
                shareText = null,
                details = listOf(routeDetail)
            )
        }

        val activeStartedAt = trackRecording.actualStartedAt()
        val startAt = activeStartedAt ?: nowEpochMillis
        val expectedFinishAt = startAt + durationMinutes.toLong() * MILLIS_PER_MINUTE
        val startLabel = if (activeStartedAt != null) "实际出发" else "计划出发"
        val statusLabel = if (activeStartedAt != null) "记录中" else "可发送"
        val details = listOf(
            routeDetail,
            DepartureBriefShareDetail(label = "预计完成", value = expectedFinishAt.timeLabel(zoneId)),
            DepartureBriefShareDetail(label = "确认规则", value = "预计完成 +60 分钟")
        )

        return DepartureBriefSharePresentation(
            title = TITLE,
            statusLabel = statusLabel,
            caption = "手动发送路线计划和预计返回时间，适合出发前发给安全联系人。",
            primaryActionLabel = "发送出发报备",
            shareText = buildShareText(
                plan = plan,
                startLabel = startLabel,
                startAt = startAt,
                expectedFinishAt = expectedFinishAt,
                zoneId = zoneId
            ),
            chooserTitle = "发送出发报备",
            details = details
        )
    }

    private fun buildShareText(
        plan: DepartureBriefPlan,
        startLabel: String,
        startAt: Long,
        expectedFinishAt: Long,
        zoneId: ZoneId
    ): String = buildString {
        appendLine("TrailMate 出发报备")
        appendLine("路线：${plan.routeName}")
        appendLine("计划：${plan.routeSummary()}")
        appendLine("$startLabel：${startAt.timeLabel(zoneId)}")
        appendLine("预计完成：${expectedFinishAt.timeLabel(zoneId)}")
        appendLine("确认规则：若超过预计完成 60 分钟仍未联系，请先电话确认，再根据最近一次主动分享的位置判断是否需要求助。")
        append("说明：这是静态行程计划，不是实时追踪链接；TrailMate 不会自动联系他人或发起救援。")
    }

    private fun TrackRecordingState.actualStartedAt(): Long? =
        if (status == TrackRecordingStatus.RECORDING || status == TrackRecordingStatus.PAUSED) {
            startedAtEpochMillis?.takeIf { it > 0L }
        } else {
            null
        }

    private fun DepartureBriefPlan.routeSummary(): String =
        "${String.format(Locale.US, "%.1f", distanceKm)} km / +$ascentMeters m"

    private fun Long.timeLabel(zoneId: ZoneId): String =
        Instant.ofEpochMilli(this)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    private const val TITLE = "出发报备"
    private const val MILLIS_PER_MINUTE = 60_000L
}
