package com.trailmate.app.core.model

import com.trailmate.app.core.location.LocationReliabilityLevel
import com.trailmate.app.core.location.LocationReliabilityPresentation
import com.trailmate.app.core.map.TrailMapReadiness

data class RouteFieldStatusItem(
    val label: String,
    val value: String
)

data class RouteFieldStatusSummary(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val items: List<RouteFieldStatusItem>
)

object RouteFieldStatusEngine {
    fun build(
        mapReadiness: TrailMapReadiness,
        locationReliability: LocationReliabilityPresentation,
        trackRecording: TrackRecordingState,
        notificationPermissionGranted: Boolean
    ): RouteFieldStatusSummary {
        val title = when (trackRecording.status) {
            TrackRecordingStatus.RECORDING -> "正在记录实走轨迹"
            TrackRecordingStatus.PAUSED -> "轨迹已暂停"
            TrackRecordingStatus.FINISHED -> if (trackRecording.pointCount > 0) {
                "轨迹已保存"
            } else {
                "准备定位记录"
            }
            TrackRecordingStatus.IDLE -> "准备定位记录"
        }
        val statusLabel = when (trackRecording.status) {
            TrackRecordingStatus.RECORDING -> if (locationReliability.level == LocationReliabilityLevel.GOOD) {
                "记录中"
            } else {
                "记录中 · 等信号"
            }
            TrackRecordingStatus.PAUSED -> "暂停中"
            TrackRecordingStatus.FINISHED -> if (trackRecording.pointCount > 0) {
                "已保存"
            } else {
                "待定位"
            }
            TrackRecordingStatus.IDLE -> if (locationReliability.level == LocationReliabilityLevel.GOOD) {
                "可开始"
            } else {
                "待定位"
            }
        }
        val caption = when (trackRecording.status) {
            TrackRecordingStatus.RECORDING -> if (locationReliability.level == LocationReliabilityLevel.GOOD) {
                "前台服务已开启，锁屏或切后台后仍会保存可信定位点。"
            } else {
                "正在等待定位稳定；只会把可信定位点写入轨迹。"
            }
            TrackRecordingStatus.PAUSED ->
                "当前不会继续写入定位点；继续记录前请确认方向与电量。"
            TrackRecordingStatus.FINISHED -> if (trackRecording.pointCount > 0) {
                "轨迹已保存在本机，可到数据页复盘本次路线表现。"
            } else {
                "先授权定位；出发前建议保存离线路线并允许轨迹通知。"
            }
            TrackRecordingStatus.IDLE ->
                "先授权定位；出发前建议保存离线路线并允许轨迹通知。"
        }

        return RouteFieldStatusSummary(
            title = title,
            statusLabel = statusLabel,
            caption = caption,
            items = listOf(
                RouteFieldStatusItem(label = "定位", value = locationReliability.statusLabel),
                RouteFieldStatusItem(label = "轨迹", value = "${trackRecording.pointCount} 点"),
                RouteFieldStatusItem(label = "底图", value = mapReadiness.setupHint.statusLabel),
                RouteFieldStatusItem(
                    label = "通知",
                    value = if (notificationPermissionGranted) "可锁屏" else "待允许"
                )
            )
        )
    }
}
