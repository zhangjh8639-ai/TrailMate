package com.trailmate.app.core.model

import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SafetyShareLocation(
    val latitude: Double?,
    val longitude: Double?,
    val horizontalAccuracyMeters: Double?
)

data class SafetyShareRoutePlan(
    val distanceKm: Double,
    val ascentMeters: Int,
    val estimatedDurationMinutes: Int?
)

data class SafetyShareDetail(
    val label: String,
    val value: String
)

data class SafetySharePresentation(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val shareText: String?,
    val details: List<SafetyShareDetail> = emptyList()
)

object SafetyShareEngine {
    fun present(
        routeName: String,
        location: SafetyShareLocation,
        trackRecording: TrackRecordingState,
        routePlan: SafetyShareRoutePlan? = null,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): SafetySharePresentation {
        val latitude = location.latitude
        val longitude = location.longitude
        if (latitude == null || longitude == null || !latitude.isFinite() || !longitude.isFinite()) {
            return SafetySharePresentation(
                title = "等待定位后分享",
                statusLabel = "待定位",
                caption = "授权定位后可分享当前位置；记录中会附带路线和已记录距离。",
                primaryActionLabel = "授权定位",
                shareText = null
            )
        }
        val accuracyMeters = location.horizontalAccuracyMeters
        if (accuracyMeters == null || !accuracyMeters.isFinite() || accuracyMeters < 0.0 || accuracyMeters > MAX_SHARE_ACCURACY_METERS) {
            return SafetySharePresentation(
                title = "等待定位稳定后分享",
                statusLabel = "精度待稳定",
                caption = accuracyMeters
                    ?.takeIf { it.isFinite() && it >= 0.0 }
                    ?.let { "当前定位精度约 ${it.toInt()} m，建议到开阔处重新定位后再分享。" }
                    ?: "缺少定位精度，建议等待定位稳定后再分享安全位置。",
                primaryActionLabel = "重新定位",
                shareText = null
            )
        }

        val isRecording = trackRecording.status == TrackRecordingStatus.RECORDING
        val title = if (isRecording) "安全分享可用" else "可分享当前位置"
        val statusLabel = if (isRecording) "记录中" else "位置可用"
        val actionLabel = if (isRecording) "分享当前记录位置" else "分享当前位置"
        val caption = if (isRecording) {
            "发送当前静态位置、路线计划和已记录距离，适合发给同行或安全联系人。"
        } else {
            "发送当前坐标、路线计划和高德位置链接，不上传到 TrailMate 服务器。"
        }

        return SafetySharePresentation(
            title = title,
            statusLabel = statusLabel,
            caption = caption,
            primaryActionLabel = actionLabel,
            shareText = shareText(
                routeName = routeName,
                latitude = latitude,
                longitude = longitude,
                horizontalAccuracyMeters = location.horizontalAccuracyMeters,
                trackRecording = trackRecording,
                routePlan = routePlan,
                nowEpochMillis = nowEpochMillis,
                zoneId = zoneId
            ),
            details = routePlan.safetyDetails(nowEpochMillis = nowEpochMillis, zoneId = zoneId)
        )
    }

    private fun shareText(
        routeName: String,
        latitude: Double,
        longitude: Double,
        horizontalAccuracyMeters: Double?,
        trackRecording: TrackRecordingState,
        routePlan: SafetyShareRoutePlan?,
        nowEpochMillis: Long,
        zoneId: ZoneId
    ): String {
        val lat = coordinate(latitude)
        val lon = coordinate(longitude)
        val markerName = "TrailMate · $routeName".urlQueryValue()
        val status = if (trackRecording.status == TrackRecordingStatus.RECORDING) {
            "正在记录，已记录 ${String.format(Locale.US, "%.1f", trackRecording.totalDistanceKm)} km"
        } else {
            "当前位置"
        }
        val accuracy = horizontalAccuracyMeters
            ?.takeIf { it.isFinite() && it >= 0.0 }
            ?.let { "（精度约 ${it.toInt()} m）" }
            .orEmpty()

        return buildString {
            appendLine("TrailMate 安全分享")
            appendLine("路线：$routeName")
            appendLine("状态：$status")
            if (trackRecording.status == TrackRecordingStatus.RECORDING) {
                appendLine("说明：这是当前时刻的静态位置，不是实时追踪链接。")
            }
            routePlan?.let { plan ->
                appendLine("计划：${plan.routeSummary()}")
                plan.estimatedDurationMinutes
                    ?.takeIf { it > 0 }
                    ?.let { durationMinutes ->
                        appendLine("预计完成：${expectedFinishLabel(nowEpochMillis, durationMinutes, zoneId)}")
                        appendLine("超时提示：若超过预计完成 60 分钟仍未联系，请先电话确认，再根据共享位置判断是否需要求助。")
                    }
            }
            appendLine("位置：$lat,$lon$accuracy")
            append("高德地图：https://uri.amap.com/marker?position=$lon,$lat&name=$markerName&coordinate=wgs84&src=TrailMate&callnative=1")
        }
    }

    private fun coordinate(value: Double): String =
        String.format(Locale.US, "%.5f", value)

    private fun String.urlQueryValue(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun expectedFinishLabel(
        nowEpochMillis: Long,
        durationMinutes: Int,
        zoneId: ZoneId
    ): String =
        Instant.ofEpochMilli(nowEpochMillis)
            .atZone(zoneId)
            .plusMinutes(durationMinutes.toLong())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    private fun SafetyShareRoutePlan?.safetyDetails(
        nowEpochMillis: Long,
        zoneId: ZoneId
    ): List<SafetyShareDetail> {
        val plan = this ?: return emptyList()
        return buildList {
            add(SafetyShareDetail(label = "路线", value = plan.routeSummary()))
            plan.estimatedDurationMinutes
                ?.takeIf { it > 0 }
                ?.let { durationMinutes ->
                    add(
                        SafetyShareDetail(
                            label = "预计完成",
                            value = expectedFinishLabel(nowEpochMillis, durationMinutes, zoneId)
                        )
                    )
                    add(SafetyShareDetail(label = "超时确认", value = "预计完成 +60 分钟"))
                }
        }
    }

    private fun SafetyShareRoutePlan.routeSummary(): String =
        "${String.format(Locale.US, "%.1f", distanceKm)} km / +$ascentMeters m"

    private const val MAX_SHARE_ACCURACY_METERS = 100.0
}
