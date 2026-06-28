package com.trailmate.app.core.model

import java.util.Locale

enum class BacktrackBreadcrumbGuidanceTone {
    READY,
    CAUTION,
    ALERT,
    UNAVAILABLE
}

data class BacktrackBreadcrumbGuidanceDetail(
    val label: String,
    val value: String
)

data class BacktrackBreadcrumbGuidancePresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val tone: BacktrackBreadcrumbGuidanceTone,
    val details: List<BacktrackBreadcrumbGuidanceDetail> = emptyList()
)

object BacktrackBreadcrumbGuidanceEngine {
    fun present(
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long
    ): BacktrackBreadcrumbGuidancePresentation {
        if (trackRecording.points.isEmpty()) {
            return unavailable()
        }

        return when (trackRecording.status) {
            TrackRecordingStatus.RECORDING -> activeRecording(trackRecording, nowEpochMillis)
            TrackRecordingStatus.PAUSED -> paused(trackRecording, nowEpochMillis)
            TrackRecordingStatus.FINISHED -> finished(trackRecording, nowEpochMillis)
            TrackRecordingStatus.IDLE -> unavailable()
        }
    }

    private fun activeRecording(
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long
    ): BacktrackBreadcrumbGuidancePresentation {
        if (trackRecording.points.size < MIN_BACKTRACK_POINT_COUNT) {
            return warmingUp(trackRecording, nowEpochMillis)
        }

        val latestPoint = trackRecording.points.last()
        val latestAgeMillis = (nowEpochMillis - latestPoint.timestampEpochMillis).coerceAtLeast(0L)
        if (latestAgeMillis > STALE_BREADCRUMB_MILLIS) {
            return stale(trackRecording, nowEpochMillis)
        }

        if (trackRecording.totalDistanceKm < MIN_MEANINGFUL_DISTANCE_KM) {
            return warmingUp(trackRecording, nowEpochMillis)
        }

        return BacktrackBreadcrumbGuidancePresentation(
            visible = true,
            title = "原路参照",
            statusLabel = "原路参照可用",
            caption = "已记录的实走轨迹可作为原路返回参照；沿已记录轨迹和可见路径返回，不要抄近路。",
            primaryActionLabel = "查看实走轨迹",
            tone = BacktrackBreadcrumbGuidanceTone.READY,
            details = trackRecording.details(nowEpochMillis)
        )
    }

    private fun warmingUp(
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long
    ): BacktrackBreadcrumbGuidancePresentation =
        BacktrackBreadcrumbGuidancePresentation(
            visible = true,
            title = "原路参照",
            statusLabel = "正在形成",
            caption = "实走轨迹还不够完整，不要把它当作撤退依据；先依赖离线地图、路标和可见路径。",
            primaryActionLabel = "继续记录",
            tone = BacktrackBreadcrumbGuidanceTone.CAUTION,
            details = trackRecording.details(nowEpochMillis)
        )

    private fun stale(
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long
    ): BacktrackBreadcrumbGuidancePresentation =
        BacktrackBreadcrumbGuidancePresentation(
            visible = true,
            title = "原路参照",
            statusLabel = "轨迹已停更",
            caption = "最近实走轨迹已停更；先刷新定位，并核对离线地图、路标和可见路径后再判断是否原路返回。",
            primaryActionLabel = "刷新定位",
            tone = BacktrackBreadcrumbGuidanceTone.ALERT,
            details = trackRecording.details(nowEpochMillis)
        )

    private fun paused(
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long
    ): BacktrackBreadcrumbGuidancePresentation =
        if (trackRecording.points.size >= MIN_BACKTRACK_POINT_COUNT) {
            BacktrackBreadcrumbGuidancePresentation(
                visible = true,
                title = "原路参照",
                statusLabel = "记录已暂停",
                caption = "实走轨迹只覆盖暂停前的移动，暂停后的移动不会被覆盖；返回前先核对当前位置和现场路径。",
                primaryActionLabel = "继续记录",
                tone = BacktrackBreadcrumbGuidanceTone.CAUTION,
                details = trackRecording.details(nowEpochMillis)
            )
        } else {
            unavailable()
        }

    private fun finished(
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long
    ): BacktrackBreadcrumbGuidancePresentation =
        if (trackRecording.points.size >= MIN_BACKTRACK_POINT_COUNT) {
            BacktrackBreadcrumbGuidancePresentation(
                visible = true,
                title = "原路参照",
                statusLabel = "轨迹已保存",
                caption = "已保存轨迹可用于复盘和返程确认，但不代表当前位置；需要先确认当前定位和现场路径。",
                primaryActionLabel = "查看轨迹",
                tone = BacktrackBreadcrumbGuidanceTone.CAUTION,
                details = trackRecording.details(nowEpochMillis)
            )
        } else {
            unavailable()
        }

    private fun unavailable(): BacktrackBreadcrumbGuidancePresentation =
        BacktrackBreadcrumbGuidancePresentation(
            visible = true,
            title = "原路参照",
            statusLabel = "无实走轨迹",
            caption = "当前没有可用于原路返回的实走轨迹；请依赖保存路线、离线地图、路标和可见路径。",
            primaryActionLabel = "查看路线地图",
            tone = BacktrackBreadcrumbGuidanceTone.UNAVAILABLE,
            details = listOf(
                BacktrackBreadcrumbGuidanceDetail(label = "已记录", value = "0.0 km"),
                BacktrackBreadcrumbGuidanceDetail(label = "轨迹点", value = "0 个"),
                BacktrackBreadcrumbGuidanceDetail(label = "可用性", value = "无证据")
            )
        )

    private fun TrackRecordingState.details(nowEpochMillis: Long): List<BacktrackBreadcrumbGuidanceDetail> =
        listOf(
            BacktrackBreadcrumbGuidanceDetail(label = "已记录", value = totalDistanceKm.coerceAtLeast(0.0).formatKm()),
            BacktrackBreadcrumbGuidanceDetail(label = "轨迹点", value = "$pointCount 个"),
            BacktrackBreadcrumbGuidanceDetail(label = "最近轨迹", value = latestPointAgeLabel(nowEpochMillis))
        )

    private fun TrackRecordingState.latestPointAgeLabel(nowEpochMillis: Long): String {
        val latestPoint = points.lastOrNull() ?: return "无"
        val ageMillis = (nowEpochMillis - latestPoint.timestampEpochMillis).coerceAtLeast(0L)
        return if (ageMillis < MILLIS_PER_MINUTE) {
            "刚刚"
        } else {
            "${ageMillis / MILLIS_PER_MINUTE} 分钟前"
        }
    }

    private fun Double.formatKm(): String =
        String.format(Locale.US, "%.1f km", this)

    private const val MIN_BACKTRACK_POINT_COUNT = 2
    private const val MIN_MEANINGFUL_DISTANCE_KM = 0.1
    private const val STALE_BREADCRUMB_MILLIS = 5 * 60_000L
    private const val MILLIS_PER_MINUTE = 60_000L
}
