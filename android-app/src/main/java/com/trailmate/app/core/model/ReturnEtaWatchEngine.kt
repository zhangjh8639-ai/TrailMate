package com.trailmate.app.core.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ReturnEtaPlan(
    val estimatedDurationMinutes: Int?
)

data class ReturnEtaWatchDetail(
    val label: String,
    val value: String
)

enum class ReturnEtaWatchTone {
    NEUTRAL,
    READY,
    CAUTION,
    ALERT
}

data class ReturnEtaWatchPresentation(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val tone: ReturnEtaWatchTone,
    val primaryActionRequiresSafetyShare: Boolean,
    val details: List<ReturnEtaWatchDetail> = emptyList()
)

object ReturnEtaWatchEngine {
    fun present(
        plan: ReturnEtaPlan,
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ReturnEtaWatchPresentation {
        if (trackRecording.status == TrackRecordingStatus.FINISHED) {
            return ReturnEtaWatchPresentation(
                title = TITLE,
                statusLabel = "已完成",
                caption = "路线已完成，先保存轨迹、复核装备和返程安排。",
                primaryActionLabel = "保存复盘",
                tone = ReturnEtaWatchTone.READY,
                primaryActionRequiresSafetyShare = false,
                details = listOf(
                    ReturnEtaWatchDetail(label = "实际结束", value = trackRecording.finishedAtEpochMillis.timeLabelOrPending(zoneId))
                )
            )
        }

        val durationMinutes = plan.estimatedDurationMinutes
        if (durationMinutes == null || durationMinutes <= 0) {
            return ReturnEtaWatchPresentation(
                title = TITLE,
                statusLabel = "缺少预计时长",
                caption = "当前路线无法计算预计返回时间，建议出发前先完成路线评估或手动确认返程时间。",
                primaryActionLabel = "查看路线评估",
                tone = ReturnEtaWatchTone.CAUTION,
                primaryActionRequiresSafetyShare = false
            )
        }

        val startedAt = trackRecording.startedAtEpochMillis
        if (trackRecording.status != TrackRecordingStatus.RECORDING && trackRecording.status != TrackRecordingStatus.PAUSED ||
            startedAt == null ||
            startedAt <= 0L
        ) {
            return ReturnEtaWatchPresentation(
                title = TITLE,
                statusLabel = "出发后开始",
                caption = "开始记录后，TrailMate 会按路线预计用时计算返回窗口。",
                primaryActionLabel = "开始徒步后计算",
                tone = ReturnEtaWatchTone.NEUTRAL,
                primaryActionRequiresSafetyShare = false
            )
        }

        val expectedFinishAt = startedAt + durationMinutes * MILLIS_PER_MINUTE
        val confirmAt = expectedFinishAt + CONFIRMATION_GRACE_MINUTES * MILLIS_PER_MINUTE
        val details = listOf(
            ReturnEtaWatchDetail(label = "预计返回", value = expectedFinishAt.timeLabel(zoneId)),
            ReturnEtaWatchDetail(label = "剩余计划", value = remainingOrExceededLabel(expectedFinishAt - nowEpochMillis)),
            ReturnEtaWatchDetail(label = "确认窗口", value = "+$CONFIRMATION_GRACE_MINUTES 分钟")
        )

        if (nowEpochMillis <= expectedFinishAt) {
            val remaining = compactDuration((expectedFinishAt - nowEpochMillis) / MILLIS_PER_MINUTE)
            return ReturnEtaWatchPresentation(
                title = TITLE,
                statusLabel = "计划内",
                caption = "预计 ${expectedFinishAt.timeLabel(zoneId)} 返回，剩余计划约 $remaining。",
                primaryActionLabel = "继续观察",
                tone = ReturnEtaWatchTone.READY,
                primaryActionRequiresSafetyShare = false,
                details = details
            )
        }

        if (nowEpochMillis <= confirmAt) {
            val exceeded = compactDuration((nowEpochMillis - expectedFinishAt) / MILLIS_PER_MINUTE)
            val untilConfirm = compactDuration((confirmAt - nowEpochMillis) / MILLIS_PER_MINUTE)
            return ReturnEtaWatchPresentation(
                title = TITLE,
                statusLabel = "已超过计划",
                caption = "已超过预计返回 $exceeded，建议复核体力、天气和返程；${untilConfirm}后进入逾期确认。",
                primaryActionLabel = "分享当前位置",
                tone = ReturnEtaWatchTone.CAUTION,
                primaryActionRequiresSafetyShare = true,
                details = details
            )
        }

        val overdue = compactDuration((nowEpochMillis - confirmAt) / MILLIS_PER_MINUTE)
        return ReturnEtaWatchPresentation(
            title = TITLE,
            statusLabel = "已超过确认时间",
            caption = "已超过确认窗口 $overdue，请主动分享当前位置或联系安全联系人；TrailMate 不会自动发送消息。",
            primaryActionLabel = "分享当前位置",
            tone = ReturnEtaWatchTone.ALERT,
            primaryActionRequiresSafetyShare = true,
            details = details
        )
    }

    private fun Long.timeLabel(zoneId: ZoneId): String =
        Instant.ofEpochMilli(this)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    private fun Long?.timeLabelOrPending(zoneId: ZoneId): String =
        this?.takeIf { it > 0L }?.timeLabel(zoneId) ?: "待保存"

    private fun remainingOrExceededLabel(deltaMillis: Long): String {
        val minutes = deltaMillis / MILLIS_PER_MINUTE
        return if (minutes >= 0) {
            compactDuration(minutes)
        } else {
            "超出 ${compactDuration(-minutes)}"
        }
    }

    private fun compactDuration(totalMinutes: Long): String {
        val minutes = totalMinutes.coerceAtLeast(0L)
        val hours = minutes / 60L
        val remainder = minutes % 60L
        return if (hours > 0L) {
            "${hours}h${remainder.toString().padStart(2, '0')}"
        } else {
            "$remainder 分钟"
        }
    }

    private const val TITLE = "预计返回"
    private const val CONFIRMATION_GRACE_MINUTES = 60
    private const val MILLIS_PER_MINUTE = 60_000L
}
