package com.trailmate.app.core.location

import com.trailmate.app.core.model.LocationBackedHikeStatus

enum class LocationReliabilityLevel {
    OFF,
    SEARCHING,
    GOOD,
    CAUTION,
    BLOCKED
}

data class LocationReliabilityDetail(
    val label: String,
    val value: String
)

data class LocationReliabilityPresentation(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val level: LocationReliabilityLevel,
    val details: List<LocationReliabilityDetail>,
    val actionLabel: String? = null
)

object TrailMateLocationReliabilityEngine {
    fun present(
        snapshot: TrailMateLocationSnapshot,
        routePointCount: Int,
        guidanceStatus: LocationBackedHikeStatus,
        guidanceCaption: String,
        nowEpochMillis: Long
    ): LocationReliabilityPresentation {
        val details = listOf(
            LocationReliabilityDetail(label = "定位精度", value = snapshot.accuracyLabel()),
            LocationReliabilityDetail(
                label = "路线匹配",
                value = snapshot.routeMatchingLabel(routePointCount, guidanceStatus)
            ),
            LocationReliabilityDetail(label = "最近更新", value = snapshot.lastUpdatedLabel(nowEpochMillis))
        )

        return when (snapshot.status) {
            TrailMateLocationStatus.DISABLED -> LocationReliabilityPresentation(
                title = "授权定位后开始校准",
                statusLabel = "未启用",
                caption = "用于实时位置、路线校验和轨迹记录。",
                level = LocationReliabilityLevel.OFF,
                details = details,
                actionLabel = "授权定位"
            )
            TrailMateLocationStatus.PERMISSION_REQUIRED -> LocationReliabilityPresentation(
                title = "需要定位权限",
                statusLabel = "需授权",
                caption = "授权定位后才能校验路线和记录轨迹。",
                level = LocationReliabilityLevel.BLOCKED,
                details = details,
                actionLabel = "授权定位"
            )
            TrailMateLocationStatus.SEARCHING -> LocationReliabilityPresentation(
                title = if (snapshot.isSlowFirstFix(nowEpochMillis)) {
                    "仍在等待 GPS 信号"
                } else {
                    "正在获取当前位置"
                },
                statusLabel = if (snapshot.isSlowFirstFix(nowEpochMillis)) {
                    "校准中"
                } else {
                    "搜索中"
                },
                caption = if (snapshot.isSlowFirstFix(nowEpochMillis)) {
                    "请移到开阔处，保持屏幕点亮继续等待首个定位点。"
                } else {
                    "请保持手机朝向开阔天空，等待第一组定位点。"
                },
                level = if (snapshot.isSlowFirstFix(nowEpochMillis)) {
                    LocationReliabilityLevel.CAUTION
                } else {
                    LocationReliabilityLevel.SEARCHING
                },
                details = details,
                actionLabel = "继续校准"
            )
            TrailMateLocationStatus.LOCATED -> snapshot.locatedPresentation(
                guidanceStatus = guidanceStatus,
                guidanceCaption = guidanceCaption,
                details = details,
                nowEpochMillis = nowEpochMillis
            )
            TrailMateLocationStatus.LOW_ACCURACY -> LocationReliabilityPresentation(
                title = "定位精度偏低",
                statusLabel = "谨慎使用",
                caption = "尽量到开阔处等待信号稳定，再依赖路线校验。",
                level = LocationReliabilityLevel.CAUTION,
                details = details,
                actionLabel = "继续校准"
            )
            TrailMateLocationStatus.PROVIDER_DISABLED -> LocationReliabilityPresentation(
                title = "系统定位未开启",
                statusLabel = "无法定位",
                caption = "请先在系统设置里打开定位服务。",
                level = LocationReliabilityLevel.BLOCKED,
                details = details,
                actionLabel = "打开系统定位"
            )
            TrailMateLocationStatus.UNAVAILABLE -> LocationReliabilityPresentation(
                title = "定位暂不可用",
                statusLabel = "需重试",
                caption = "当前位置暂时无法获取，请稍后重试或检查系统定位。",
                level = LocationReliabilityLevel.BLOCKED,
                details = details,
                actionLabel = "重试定位"
            )
        }
    }

    private fun TrailMateLocationSnapshot.locatedPresentation(
        guidanceStatus: LocationBackedHikeStatus,
        guidanceCaption: String,
        details: List<LocationReliabilityDetail>,
        nowEpochMillis: Long
    ): LocationReliabilityPresentation =
        if (!TrailMateLocationFixReliability.isFresh(snapshot = this, nowEpochMillis = nowEpochMillis)) {
            LocationReliabilityPresentation(
                title = "定位已过期",
                statusLabel = "需校准",
                caption = "当前位置已超过 60 秒未更新，请等待新的定位点。",
                level = LocationReliabilityLevel.CAUTION,
                details = details,
                actionLabel = "继续校准"
            )
        } else if (horizontalAccuracyMeters == null) {
            LocationReliabilityPresentation(
                title = "等待定位精度",
                statusLabel = "校准中",
                caption = "已获取位置，正在等待精度数据稳定。",
                level = LocationReliabilityLevel.CAUTION,
                details = details,
                actionLabel = "继续校准"
            )
        } else when (guidanceStatus) {
            LocationBackedHikeStatus.CHECK_ROUTE -> LocationReliabilityPresentation(
                title = "请核对当前位置",
                statusLabel = "需确认",
                caption = guidanceCaption,
                level = LocationReliabilityLevel.CAUTION,
                details = details,
                actionLabel = "继续校准"
            )
            LocationBackedHikeStatus.LOW_ACCURACY -> LocationReliabilityPresentation(
                title = "定位精度偏低",
                statusLabel = "谨慎使用",
                caption = "尽量到开阔处等待信号稳定，再依赖路线校验。",
                level = LocationReliabilityLevel.CAUTION,
                details = details,
                actionLabel = "继续校准"
            )
            else -> LocationReliabilityPresentation(
                title = "定位可用于导航",
                statusLabel = "可靠",
                caption = guidanceCaption,
                level = LocationReliabilityLevel.GOOD,
                details = details
            )
        }

    private fun TrailMateLocationSnapshot.accuracyLabel(): String =
        horizontalAccuracyMeters?.let { accuracy -> "约 ${accuracy.toInt()} m" } ?: "等待信号"

    private fun TrailMateLocationSnapshot.isSlowFirstFix(nowEpochMillis: Long): Boolean =
        TrailMateLocationFixReliability.fixAgeMillis(
            snapshot = this,
            nowEpochMillis = nowEpochMillis
        ) >= FIRST_FIX_SLOW_MILLIS

    private fun TrailMateLocationSnapshot.routeMatchingLabel(
        routePointCount: Int,
        guidanceStatus: LocationBackedHikeStatus
    ): String {
        if (latitude == null || longitude == null) {
            return "等待定位"
        }
        if (routePointCount <= 0) {
            return "仅记录轨迹"
        }

        return when (guidanceStatus) {
            LocationBackedHikeStatus.ON_ROUTE,
            LocationBackedHikeStatus.FINISHED -> "可校验偏离"
            LocationBackedHikeStatus.CHECK_ROUTE -> "疑似偏离"
            LocationBackedHikeStatus.LOW_ACCURACY -> "待精度稳定"
            LocationBackedHikeStatus.WAITING -> "等待开始徒步"
        }
    }

    private fun TrailMateLocationSnapshot.lastUpdatedLabel(nowEpochMillis: Long): String {
        if (latitude == null || longitude == null) {
            return "未定位"
        }

        val elapsedMillis = (nowEpochMillis - timestampEpochMillis).coerceAtLeast(0L)
        if (elapsedMillis < 60_000L) {
            return "刚刚"
        }

        val elapsedMinutes = (elapsedMillis / 60_000L).coerceAtLeast(1L)
        if (elapsedMinutes < 60L) {
            return "$elapsedMinutes 分钟前"
        }

        return "${elapsedMinutes / 60L} 小时前"
    }

    private const val FIRST_FIX_SLOW_MILLIS = 45_000L
}
