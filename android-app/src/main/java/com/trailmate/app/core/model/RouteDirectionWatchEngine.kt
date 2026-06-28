package com.trailmate.app.core.model

import java.util.Locale

enum class RouteDirectionWatchTone {
    NEUTRAL,
    READY,
    ALERT
}

data class RouteDirectionWatchDetail(
    val label: String,
    val value: String
)

data class RouteDirectionWatchPresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val tone: RouteDirectionWatchTone,
    val details: List<RouteDirectionWatchDetail> = emptyList()
)

object RouteDirectionWatchEngine {
    fun present(
        previousFix: HikeLocationFix?,
        currentFix: HikeLocationFix?,
        locationStatus: LocationBackedHikeStatus,
        trackRecording: TrackRecordingState
    ): RouteDirectionWatchPresentation {
        if (trackRecording.status != TrackRecordingStatus.RECORDING) {
            return hidden(
                statusLabel = "记录未进行",
                tone = RouteDirectionWatchTone.NEUTRAL,
                caption = "开始轨迹记录后再判断行进方向。"
            )
        }
        if (locationStatus != LocationBackedHikeStatus.ON_ROUTE) {
            return hidden(
                statusLabel = "方向待确认",
                tone = RouteDirectionWatchTone.NEUTRAL,
                caption = "当前状态不适合判断行进方向，先按定位或偏航提示处理。"
            )
        }
        if (previousFix == null || currentFix == null || !previousFix.isUsable() || !currentFix.isUsable()) {
            return hidden(
                statusLabel = "方向待确认",
                tone = RouteDirectionWatchTone.NEUTRAL,
                caption = "缺少可靠路线进度样本，暂不判断方向。"
            )
        }

        val sampleWindowMillis = currentFix.timestampEpochMillis - previousFix.timestampEpochMillis
        if (sampleWindowMillis < MIN_SAMPLE_WINDOW_MILLIS) {
            return hidden(
                statusLabel = "样本不足",
                tone = RouteDirectionWatchTone.NEUTRAL,
                caption = "路线进度样本时间太短，暂不判断方向。"
            )
        }
        if (sampleWindowMillis > MAX_SAMPLE_WINDOW_MILLIS) {
            return hidden(
                statusLabel = "方向待确认",
                tone = RouteDirectionWatchTone.NEUTRAL,
                caption = "路线进度样本间隔过久，先刷新定位再判断方向。"
            )
        }

        val distanceDeltaKm = currentFix.distanceAlongRouteKm - previousFix.distanceAlongRouteKm
        if (distanceDeltaKm >= 0.0) {
            return hidden(
                statusLabel = "方向正常",
                tone = RouteDirectionWatchTone.READY,
                caption = "路线进度正在向前推进。"
            )
        }

        val backwardDistanceKm = -distanceDeltaKm
        if (backwardDistanceKm < BACKWARD_ALERT_DISTANCE_KM) {
            return hidden(
                statusLabel = "方向正常",
                tone = RouteDirectionWatchTone.READY,
                caption = "小幅路线进度回摆按 GPS 抖动处理。"
            )
        }

        val backwardMeters = (backwardDistanceKm * METERS_PER_KM).toInt()
        return RouteDirectionWatchPresentation(
            visible = true,
            title = "方向异常",
            statusLabel = "可能反向行进",
            caption = "路线进度倒退约 ${backwardMeters.formatMeters()}，请先停下，核对离线地图、路标和可见路径方向。",
            primaryActionLabel = "刷新定位核对",
            tone = RouteDirectionWatchTone.ALERT,
            details = listOf(
                RouteDirectionWatchDetail(label = "倒退距离", value = backwardMeters.formatMeters()),
                RouteDirectionWatchDetail(
                    label = "样本间隔",
                    value = "${(sampleWindowMillis / MILLIS_PER_SECOND)} 秒"
                )
            )
        )
    }

    private fun hidden(
        statusLabel: String,
        tone: RouteDirectionWatchTone,
        caption: String
    ): RouteDirectionWatchPresentation =
        RouteDirectionWatchPresentation(
            visible = false,
            title = "方向校验",
            statusLabel = statusLabel,
            caption = caption,
            primaryActionLabel = "刷新定位",
            tone = tone
        )

    private fun HikeLocationFix.isUsable(): Boolean =
        distanceAlongRouteKm.isFinite() &&
            distanceAlongRouteKm >= 0.0 &&
            horizontalAccuracyMeters.isFinite() &&
            horizontalAccuracyMeters <= MAX_DIRECTION_ACCURACY_METERS &&
            crossTrackErrorMeters.isFinite() &&
            crossTrackErrorMeters >= 0.0

    private fun Int.formatMeters(): String =
        String.format(Locale.US, "%d m", this)

    private const val BACKWARD_ALERT_DISTANCE_KM = 0.15
    private const val MAX_DIRECTION_ACCURACY_METERS = 50.0
    private const val MIN_SAMPLE_WINDOW_MILLIS = 60_000L
    private const val MAX_SAMPLE_WINDOW_MILLIS = 5 * 60_000L
    private const val MILLIS_PER_SECOND = 1_000L
    private const val METERS_PER_KM = 1_000.0
}
