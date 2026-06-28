package com.trailmate.app.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class DaylightReturnWatchDetail(
    val label: String,
    val value: String
)

enum class DaylightReturnWatchTone {
    CAUTION,
    ALERT
}

data class DaylightReturnWatchPresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val primaryActionRequiresSafetyShare: Boolean,
    val tone: DaylightReturnWatchTone?,
    val details: List<DaylightReturnWatchDetail>
)

data class DaylightSafetyWindow(
    val sunsetEpochMillis: Long,
    val civilDuskEpochMillis: Long
)

object DaylightReturnWatchEngine {
    fun present(
        route: ImportedRoute,
        trackRecording: TrackRecordingState,
        expectedFinishEpochMillis: Long?,
        nowEpochMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DaylightReturnWatchPresentation {
        if (trackRecording.status != TrackRecordingStatus.RECORDING &&
            trackRecording.status != TrackRecordingStatus.PAUSED
        ) {
            return hidden()
        }

        val centroid = route.routeCentroidOrNull() ?: return hidden()
        val windowDate = Instant.ofEpochMilli(nowEpochMillis)
            .atZone(zoneId)
            .toLocalDate()
        val window = estimateDaylightWindow(
            latitude = centroid.latitude,
            longitude = centroid.longitude,
            date = windowDate,
            zoneId = zoneId
        ) ?: return hidden()

        val details = details(
            expectedFinishEpochMillis = expectedFinishEpochMillis,
            window = window,
            zoneId = zoneId
        )
        if (expectedFinishEpochMillis != null && expectedFinishEpochMillis >= window.sunsetEpochMillis) {
            return alert(
                statusLabel = "可能天黑后完成",
                caption = if (expectedFinishEpochMillis >= window.civilDuskEpochMillis) {
                    "预计完成已接近民用暮光后。建议立即缩短路线或使用安全退出，必要时手动分享当前位置。"
                } else {
                    "预计完成已进入日落后。建议立即缩短路线或使用安全退出，必要时手动分享当前位置。"
                },
                details = details
            )
        }

        if (nowEpochMillis >= window.sunsetEpochMillis) {
            return alert(
                statusLabel = "已经接近日落后",
                caption = "当前位置已接近日落后，先停止推进难走路段，复核安全退出和手动位置分享。",
                details = details
            )
        }

        if (expectedFinishEpochMillis != null &&
            expectedFinishEpochMillis >= window.sunsetEpochMillis - CAUTION_WINDOW_MINUTES * MILLIS_PER_MINUTE
        ) {
            return DaylightReturnWatchPresentation(
                visible = true,
                title = TITLE,
                statusLabel = "日照窗口收紧",
                caption = "预计完成接近日落估算。先复核头灯、电量、撤退点和配速，必要时缩短路线。",
                primaryActionLabel = "复核天黑前路线",
                primaryActionRequiresSafetyShare = false,
                tone = DaylightReturnWatchTone.CAUTION,
                details = details
            )
        }

        return hidden()
    }

    internal fun estimateDaylightWindow(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zoneId: ZoneId
    ): DaylightSafetyWindow? {
        if (!latitude.isFinite() || !longitude.isFinite()) return null
        if (latitude !in -72.0..72.0 || longitude !in -180.0..180.0) return null

        val sunset = estimateSolarEvent(
            latitude = latitude,
            longitude = longitude,
            date = date,
            zoneId = zoneId,
            zenithDegrees = SUNSET_ZENITH_DEGREES
        ) ?: return null
        val civilDusk = estimateSolarEvent(
            latitude = latitude,
            longitude = longitude,
            date = date,
            zoneId = zoneId,
            zenithDegrees = CIVIL_DUSK_ZENITH_DEGREES
        ) ?: return null

        return DaylightSafetyWindow(
            sunsetEpochMillis = sunset,
            civilDuskEpochMillis = civilDusk.coerceAtLeast(sunset)
        )
    }

    private fun details(
        expectedFinishEpochMillis: Long?,
        window: DaylightSafetyWindow,
        zoneId: ZoneId
    ): List<DaylightReturnWatchDetail> =
        listOf(
            DaylightReturnWatchDetail(
                label = "预计完成",
                value = expectedFinishEpochMillis?.timeLabel(zoneId) ?: "待估算"
            ),
            DaylightReturnWatchDetail(
                label = "日落估算",
                value = window.sunsetEpochMillis.clockLabel(zoneId)
            ),
            DaylightReturnWatchDetail(
                label = "民用暮光",
                value = window.civilDuskEpochMillis.clockLabel(zoneId)
            )
        )

    private fun alert(
        statusLabel: String,
        caption: String,
        details: List<DaylightReturnWatchDetail>
    ): DaylightReturnWatchPresentation =
        DaylightReturnWatchPresentation(
            visible = true,
            title = TITLE,
            statusLabel = statusLabel,
            caption = caption,
            primaryActionLabel = "分享当前位置",
            primaryActionRequiresSafetyShare = true,
            tone = DaylightReturnWatchTone.ALERT,
            details = details
        )

    private fun hidden(): DaylightReturnWatchPresentation =
        DaylightReturnWatchPresentation(
            visible = false,
            title = TITLE,
            statusLabel = "",
            caption = "",
            primaryActionLabel = "",
            primaryActionRequiresSafetyShare = false,
            tone = null,
            details = emptyList()
        )

    private fun ImportedRoute.routeCentroidOrNull(): RouteCentroid? {
        val validPoints = routePoints.filter { point ->
            point.latitude.isFinite() &&
                point.longitude.isFinite() &&
                point.latitude in -72.0..72.0 &&
                point.longitude in -180.0..180.0
        }
        if (validPoints.isEmpty()) {
            return null
        }
        return RouteCentroid(
            latitude = validPoints.map { it.latitude }.average(),
            longitude = validPoints.map { it.longitude }.average()
        )
    }

    private fun estimateSolarEvent(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zoneId: ZoneId,
        zenithDegrees: Double
    ): Long? {
        // NOAA fractional-year approximation; TrailMate uses it only as a local field estimate.
        val dayOfYear = date.dayOfYear
        val denominator = if (date.isLeapYear) 366.0 else 365.0
        val gamma = 2.0 * PI / denominator * (dayOfYear - 1.0)
        val equationOfTimeMinutes = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2.0 * gamma) -
                0.040849 * sin(2.0 * gamma)
            )
        val declination = 0.006918 -
            0.399912 * cos(gamma) +
            0.070257 * sin(gamma) -
            0.006758 * cos(2.0 * gamma) +
            0.000907 * sin(2.0 * gamma) -
            0.002697 * cos(3.0 * gamma) +
            0.00148 * sin(3.0 * gamma)
        val latitudeRadians = latitude.toRadians()
        val hourAngleCosine = (cos(zenithDegrees.toRadians()) /
            (cos(latitudeRadians) * cos(declination))) -
            tan(latitudeRadians) * tan(declination)
        if (hourAngleCosine !in -1.0..1.0) {
            return null
        }

        val hourAngleDegrees = acos(hourAngleCosine).toDegrees()
        val zoneOffsetMinutes = zoneId.rules
            .getOffset(date.atTime(12, 0).atZone(zoneId).toInstant())
            .totalSeconds / 60.0
        val localMinutes = 720.0 -
            4.0 * (longitude - hourAngleDegrees) -
            equationOfTimeMinutes +
            zoneOffsetMinutes

        return date.atStartOfDay(zoneId)
            .toInstant()
            .plusMillis((localMinutes * MILLIS_PER_MINUTE).toLong())
            .toEpochMilli()
    }

    private fun Long.timeLabel(zoneId: ZoneId): String =
        Instant.ofEpochMilli(this)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    private fun Long.clockLabel(zoneId: ZoneId): String =
        Instant.ofEpochMilli(this)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("HH:mm"))

    private fun Double.toRadians(): Double = this / 180.0 * PI

    private fun Double.toDegrees(): Double = this * 180.0 / PI

    private data class RouteCentroid(
        val latitude: Double,
        val longitude: Double
    )

    private const val TITLE = "日照窗口"
    private const val CAUTION_WINDOW_MINUTES = 45
    private const val SUNSET_ZENITH_DEGREES = 90.833
    private const val CIVIL_DUSK_ZENITH_DEGREES = 96.0
    private const val MILLIS_PER_MINUTE = 60_000L
}
