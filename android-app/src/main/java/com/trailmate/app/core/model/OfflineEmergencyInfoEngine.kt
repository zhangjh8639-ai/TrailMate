package com.trailmate.app.core.model

import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class OfflineEmergencyRouteSummary(
    val routeName: String,
    val distanceKm: Double,
    val ascentMeters: Int
)

data class OfflineEmergencyLocation(
    val latitude: Double?,
    val longitude: Double?,
    val horizontalAccuracyMeters: Double?,
    val timestampEpochMillis: Long?
)

data class OfflineEmergencyProgress(
    val currentCheckpointLabel: String,
    val nextCheckpointLabel: String?,
    val recordedDistanceKm: Double,
    val recordingActive: Boolean
)

data class OfflineEmergencyInfoDetail(
    val label: String,
    val value: String
)

data class OfflineEmergencyInfoPresentation(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val chooserTitle: String,
    val shareText: String,
    val details: List<OfflineEmergencyInfoDetail>
)

data class OfflineEmergencyInfoShareAction(
    val shareText: String,
    val chooserTitle: String
)

object OfflineEmergencyInfoActionEngine {
    fun resolveShareAction(
        route: OfflineEmergencyRouteSummary,
        location: OfflineEmergencyLocation,
        progress: OfflineEmergencyProgress,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): OfflineEmergencyInfoShareAction {
        val presentation = OfflineEmergencyInfoEngine.present(
            route = route,
            location = location,
            progress = progress,
            nowEpochMillis = nowEpochMillis,
            zoneId = zoneId
        )
        return OfflineEmergencyInfoShareAction(
            shareText = presentation.shareText,
            chooserTitle = presentation.chooserTitle
        )
    }
}

object OfflineEmergencyInfoEngine {
    fun present(
        route: OfflineEmergencyRouteSummary,
        location: OfflineEmergencyLocation,
        progress: OfflineEmergencyProgress,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): OfflineEmergencyInfoPresentation {
        val usableLocation = location.usable(nowEpochMillis)
        val locationStatus = if (usableLocation != null) "定位可用" else "定位待刷新"
        val caption = if (usableLocation != null) {
            "包含当前静态坐标、路线和进度，适合低信号时手动发给同行或救援接线员。"
        } else {
            "缺少可靠的当前 GPS 坐标，仍可分享路线和进度，并提示先到开阔处刷新定位。"
        }
        val details = listOf(
            OfflineEmergencyInfoDetail(label = "路线", value = route.routeSummary()),
            OfflineEmergencyInfoDetail(label = "进度", value = progress.recordedDistanceLabel()),
            OfflineEmergencyInfoDetail(label = "定位", value = usableLocation?.coordinateLabel ?: "待刷新")
        )

        return OfflineEmergencyInfoPresentation(
            title = "求助信息",
            statusLabel = locationStatus,
            caption = caption,
            primaryActionLabel = "分享求助信息",
            chooserTitle = "分享求助信息",
            shareText = buildShareText(
                route = route,
                progress = progress,
                usableLocation = usableLocation,
                zoneId = zoneId
            ),
            details = details
        )
    }

    private fun buildShareText(
        route: OfflineEmergencyRouteSummary,
        progress: OfflineEmergencyProgress,
        usableLocation: UsableEmergencyLocation?,
        zoneId: ZoneId
    ): String = buildString {
        appendLine("TrailMate 求助信息")
        appendLine("路线：${route.routeName}")
        appendLine("计划：${route.routeSummary()}")
        appendLine("进度：${progress.progressLine()}")
        if (usableLocation != null) {
            appendLine(
                "坐标：${usableLocation.coordinateLabel}（精度约 ${usableLocation.accuracyMeters.toInt()} m，时间 ${usableLocation.timestampEpochMillis.timeLabel(zoneId)}）"
            )
            appendLine("高德地图：${usableLocation.amapUrl(route.routeName)}")
        } else {
            appendLine("坐标：暂无可靠当前 GPS 坐标")
            appendLine("位置建议：到开阔处刷新定位；如果无法移动，请描述最近经过的检查点、岔路和明显地形。")
        }
        append("说明：这是静态求助信息，不是实时追踪链接；TrailMate 不会自动联系他人、持续监控或发起救援。")
    }

    private fun OfflineEmergencyLocation.usable(nowEpochMillis: Long): UsableEmergencyLocation? {
        val lat = latitude?.takeIf { it.isFinite() } ?: return null
        val lon = longitude?.takeIf { it.isFinite() } ?: return null
        val accuracy = horizontalAccuracyMeters
            ?.takeIf { it.isFinite() && it in 0.0..MAX_LOCATION_ACCURACY_METERS }
            ?: return null
        val timestamp = timestampEpochMillis?.takeIf { it > 0L && it <= nowEpochMillis } ?: return null
        val ageMillis = nowEpochMillis - timestamp
        if (ageMillis > MAX_LOCATION_AGE_MILLIS) {
            return null
        }
        return UsableEmergencyLocation(
            latitude = lat,
            longitude = lon,
            accuracyMeters = accuracy,
            timestampEpochMillis = timestamp
        )
    }

    private data class UsableEmergencyLocation(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Double,
        val timestampEpochMillis: Long
    ) {
        val coordinateLabel: String
            get() = "${latitude.coordinate()},${longitude.coordinate()}"

        fun amapUrl(routeName: String): String =
            "https://uri.amap.com/marker?position=${longitude.coordinate()},${latitude.coordinate()}&name=${
                "TrailMate · $routeName".urlQueryValue()
            }&coordinate=wgs84&src=TrailMate&callnative=1"
    }

    private fun OfflineEmergencyRouteSummary.routeSummary(): String =
        "${String.format(Locale.US, "%.1f", distanceKm)} km / +$ascentMeters m"

    private fun OfflineEmergencyProgress.progressLine(): String =
        listOfNotNull(
            currentCheckpointLabel.takeIf { it.isNotBlank() },
            nextCheckpointLabel?.takeIf { it.isNotBlank() },
            recordedDistanceLine()
        ).joinToString("，")

    private fun OfflineEmergencyProgress.recordedDistanceLine(): String =
        if (recordingActive || recordedDistanceKm > 0.0) {
            "已记录 ${String.format(Locale.US, "%.1f", recordedDistanceKm)} km"
        } else {
            "未记录轨迹距离"
        }

    private fun OfflineEmergencyProgress.recordedDistanceLabel(): String =
        if (recordingActive || recordedDistanceKm > 0.0) {
            "已记录 ${String.format(Locale.US, "%.1f", recordedDistanceKm)} km"
        } else {
            "未记录"
        }

    private fun Double.coordinate(): String =
        String.format(Locale.US, "%.5f", this)

    private fun Long.timeLabel(zoneId: ZoneId): String =
        Instant.ofEpochMilli(this)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    private fun String.urlQueryValue(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private const val MAX_LOCATION_ACCURACY_METERS = 100.0
    private const val MAX_LOCATION_AGE_MILLIS = 2 * 60_000L
}
