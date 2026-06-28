package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationFixReliability
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus

enum class GpsSignalLossWatchTone {
    CAUTION,
    ALERT
}

data class GpsSignalLossWatchDetail(
    val label: String,
    val value: String
)

data class GpsSignalLossWatchPresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val tone: GpsSignalLossWatchTone,
    val details: List<GpsSignalLossWatchDetail> = emptyList()
)

object GpsSignalLossWatchEngine {
    fun present(
        snapshot: TrailMateLocationSnapshot,
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long
    ): GpsSignalLossWatchPresentation {
        if (trackRecording.status != TrackRecordingStatus.RECORDING) {
            return hidden(statusLabel = "记录未进行")
        }

        return when (snapshot.status) {
            TrailMateLocationStatus.LOCATED,
            TrailMateLocationStatus.LOW_ACCURACY -> coordinateBearingPresentation(
                snapshot = snapshot,
                nowEpochMillis = nowEpochMillis
            )
            TrailMateLocationStatus.SEARCHING -> searchingPresentation(
                snapshot = snapshot,
                nowEpochMillis = nowEpochMillis
            )
            TrailMateLocationStatus.DISABLED,
            TrailMateLocationStatus.PERMISSION_REQUIRED,
            TrailMateLocationStatus.PROVIDER_DISABLED,
            TrailMateLocationStatus.UNAVAILABLE -> interruptedPresentation(snapshot)
        }
    }

    private fun coordinateBearingPresentation(
        snapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long
    ): GpsSignalLossWatchPresentation {
        val ageMillis = TrailMateLocationFixReliability.fixAgeMillis(snapshot, nowEpochMillis)
        if (ageMillis <= TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS) {
            return hidden(statusLabel = "定位正常")
        }

        val details = listOf(
            GpsSignalLossWatchDetail(label = "最近定位", value = "${ageMillis.toWholeMinutes()} 分钟前"),
            GpsSignalLossWatchDetail(label = "定位状态", value = snapshot.status.signalLossLabel()),
            GpsSignalLossWatchDetail(label = "轨迹记录", value = "记录中")
        )

        return if (ageMillis >= ALERT_FIX_AGE_MILLIS) {
            GpsSignalLossWatchPresentation(
                visible = true,
                title = "定位失联",
                statusLabel = "位置已过期",
                caption = "当前位置已超过 5 分钟未更新；不要把旧位置当作当前位置，优先查看离线地图、路标和可见路径。",
                primaryActionLabel = "刷新定位",
                tone = GpsSignalLossWatchTone.ALERT,
                details = details
            )
        } else {
            GpsSignalLossWatchPresentation(
                visible = true,
                title = "定位停更",
                statusLabel = "等待新定位",
                caption = "当前位置已超过 1 分钟未更新；先停下确认路径，等待新的定位点后再继续依赖路线进度。",
                primaryActionLabel = "刷新定位",
                tone = GpsSignalLossWatchTone.CAUTION,
                details = details
            )
        }
    }

    private fun searchingPresentation(
        snapshot: TrailMateLocationSnapshot,
        nowEpochMillis: Long
    ): GpsSignalLossWatchPresentation {
        val waitingMillis = TrailMateLocationFixReliability.fixAgeMillis(snapshot, nowEpochMillis)
        if (waitingMillis <= TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS) {
            return hidden(statusLabel = "正在定位")
        }

        val details = listOf(
            GpsSignalLossWatchDetail(label = "等待时间", value = "${waitingMillis.toWholeMinutes()} 分钟"),
            GpsSignalLossWatchDetail(label = "定位状态", value = "搜索中"),
            GpsSignalLossWatchDetail(label = "轨迹记录", value = "记录中")
        )
        val severe = waitingMillis >= ALERT_FIX_AGE_MILLIS

        return GpsSignalLossWatchPresentation(
            visible = true,
            title = "等待 GPS",
            statusLabel = if (severe) "定位超时" else "尚未定位",
            caption = if (severe) {
                "GPS 已等待超过 5 分钟；请先到开阔处刷新定位，并核对离线地图、路标和可见路径。"
            } else {
                "GPS 仍在搜索；请移到开阔处，保持 TrailMate 在前台，等首个定位点后再依赖路线进度。"
            },
            primaryActionLabel = "刷新定位",
            tone = if (severe) GpsSignalLossWatchTone.ALERT else GpsSignalLossWatchTone.CAUTION,
            details = details
        )
    }

    private fun interruptedPresentation(snapshot: TrailMateLocationSnapshot): GpsSignalLossWatchPresentation =
        GpsSignalLossWatchPresentation(
            visible = true,
            title = "定位中断",
            statusLabel = "无法可靠记录",
            caption = "轨迹记录正在等待系统定位恢复；请恢复定位权限或打开系统定位后，再依赖路线进度。",
            primaryActionLabel = "恢复定位",
            tone = GpsSignalLossWatchTone.ALERT,
            details = listOf(
                GpsSignalLossWatchDetail(label = "定位状态", value = snapshot.status.signalLossLabel()),
                GpsSignalLossWatchDetail(label = "最近定位", value = "无可用定位"),
                GpsSignalLossWatchDetail(label = "轨迹记录", value = "记录中")
            )
        )

    private fun hidden(statusLabel: String): GpsSignalLossWatchPresentation =
        GpsSignalLossWatchPresentation(
            visible = false,
            title = "GPS 状态",
            statusLabel = statusLabel,
            caption = "",
            primaryActionLabel = "刷新定位",
            tone = GpsSignalLossWatchTone.CAUTION
        )

    private fun Long.toWholeMinutes(): Long =
        (this / MILLIS_PER_MINUTE).coerceAtLeast(1L)

    private fun TrailMateLocationStatus.signalLossLabel(): String =
        when (this) {
            TrailMateLocationStatus.DISABLED -> "未启用"
            TrailMateLocationStatus.PERMISSION_REQUIRED -> "需授权"
            TrailMateLocationStatus.SEARCHING -> "搜索中"
            TrailMateLocationStatus.LOCATED -> "已定位"
            TrailMateLocationStatus.LOW_ACCURACY -> "精度偏低"
            TrailMateLocationStatus.PROVIDER_DISABLED -> "系统定位关闭"
            TrailMateLocationStatus.UNAVAILABLE -> "暂不可用"
        }

    private const val ALERT_FIX_AGE_MILLIS = 5 * 60_000L
    private const val MILLIS_PER_MINUTE = 60_000L
}
