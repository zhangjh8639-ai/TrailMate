package com.trailmate.app.core.model

import java.util.Locale

data class ProgressSafetyWatchDetail(
    val label: String,
    val value: String
)

enum class ProgressSafetyWatchTone {
    CAUTION,
    ALERT
}

data class ProgressSafetyWatchPresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val primaryActionRequiresSafetyShare: Boolean,
    val tone: ProgressSafetyWatchTone?,
    val details: List<ProgressSafetyWatchDetail>
)

object ProgressSafetyWatchEngine {
    fun present(
        route: ImportedRoute,
        plan: HikePlanSummary,
        trackRecording: TrackRecordingState,
        fix: HikeLocationFix?,
        nowEpochMillis: Long
    ): ProgressSafetyWatchPresentation {
        if (trackRecording.status == TrackRecordingStatus.FINISHED ||
            trackRecording.status == TrackRecordingStatus.IDLE
        ) {
            return hidden()
        }

        val startedAt = trackRecording.startedAtEpochMillis?.takeIf { it > 0L } ?: return hidden()
        val elapsedMinutes = ((nowEpochMillis - startedAt) / MILLIS_PER_MINUTE).takeIf { it >= 0L }
            ?: return hidden()
        val actualProgressKm = fix?.distanceAlongRouteKm
            ?.takeIf { it.isFinite() && it >= 0.0 }
            ?.coerceAtMost(route.distanceKm.coerceAtLeast(0.0))
            ?: return hidden()
        val plannedProgressKm = plannedDistanceAt(plan, elapsedMinutes) ?: return hidden()
        if (plannedProgressKm <= 0.0) {
            return hidden()
        }

        val behindKm = plannedProgressKm - actualProgressKm
        val progressRatio = actualProgressKm / plannedProgressKm
        val remainingKm = (route.distanceKm - actualProgressKm).coerceAtLeast(0.0)
        if (behindKm < CAUTION_BEHIND_KM || progressRatio > CAUTION_PROGRESS_RATIO + RATIO_EPSILON) {
            return hidden()
        }

        val details = listOf(
            ProgressSafetyWatchDetail(label = "计划进度", value = plannedProgressKm.formatKm()),
            ProgressSafetyWatchDetail(label = "实际进度", value = actualProgressKm.formatKm()),
            ProgressSafetyWatchDetail(label = "剩余距离", value = remainingKm.formatKm())
        )
        val severe = elapsedMinutes >= ALERT_MIN_ELAPSED_MINUTES &&
            behindKm >= ALERT_BEHIND_KM &&
            progressRatio <= ALERT_PROGRESS_RATIO + RATIO_EPSILON &&
            remainingKm > ALERT_MIN_REMAINING_KM

        return if (severe) {
            ProgressSafetyWatchPresentation(
                visible = true,
                title = TITLE,
                statusLabel = "进度压力高",
                caption = "当前进度明显落后且剩余距离较长，建议缩短路线或使用安全退出；位置消息需要你手动发送。",
                primaryActionLabel = "分享当前位置",
                primaryActionRequiresSafetyShare = true,
                tone = ProgressSafetyWatchTone.ALERT,
                details = details
            )
        } else if (elapsedMinutes >= CAUTION_MIN_ELAPSED_MINUTES) {
            ProgressSafetyWatchPresentation(
                visible = true,
                title = TITLE,
                statusLabel = "进度偏慢",
                caption = "当前进度慢于计划。先停下复核体力、补水补给、天气和返程时间；不要为了赶进度抄近路或离开可见路径。",
                primaryActionLabel = "先休息复核",
                primaryActionRequiresSafetyShare = false,
                tone = ProgressSafetyWatchTone.CAUTION,
                details = details
            )
        } else {
            hidden()
        }
    }

    private fun hidden(): ProgressSafetyWatchPresentation =
        ProgressSafetyWatchPresentation(
            visible = false,
            title = TITLE,
            statusLabel = "",
            caption = "",
            primaryActionLabel = "",
            primaryActionRequiresSafetyShare = false,
            tone = null,
            details = emptyList()
        )

    private fun plannedDistanceAt(plan: HikePlanSummary, elapsedMinutes: Long): Double? {
        val checkpoints = plan.checkpoints
            .mapNotNull { checkpoint ->
                val minutes = checkpoint.timeFromStart.toMinutesOrNull() ?: return@mapNotNull null
                if (checkpoint.distanceKm.isFinite() && checkpoint.distanceKm >= 0.0) {
                    minutes to checkpoint.distanceKm
                } else {
                    null
                }
            }
            .sortedBy { (minutes, _) -> minutes }
        if (checkpoints.size < 2) {
            return null
        }

        val first = checkpoints.first()
        if (elapsedMinutes <= first.first) {
            return first.second
        }
        checkpoints.zipWithNext().forEach { (start, end) ->
            val (startMinutes, startDistance) = start
            val (endMinutes, endDistance) = end
            if (elapsedMinutes <= endMinutes) {
                val segmentMinutes = (endMinutes - startMinutes).takeIf { it > 0L } ?: return startDistance
                val elapsedInSegment = (elapsedMinutes - startMinutes).coerceAtLeast(0L)
                val progress = elapsedInSegment.toDouble() / segmentMinutes.toDouble()
                return startDistance + (endDistance - startDistance) * progress
            }
        }
        return checkpoints.last().second
    }

    private fun String.toMinutesOrNull(): Long? {
        val parts = split(":")
        if (parts.size != 2) {
            return null
        }
        val hours = parts[0].toLongOrNull() ?: return null
        val minutes = parts[1].toLongOrNull() ?: return null
        if (hours < 0 || minutes !in 0..59) {
            return null
        }
        return hours * 60L + minutes
    }

    private fun Double.formatKm(): String =
        String.format(Locale.US, "%.1f km", this)

    private const val TITLE = "体力复核"
    private const val CAUTION_MIN_ELAPSED_MINUTES = 60L
    private const val CAUTION_BEHIND_KM = 1.0
    private const val CAUTION_PROGRESS_RATIO = 0.75
    private const val ALERT_MIN_ELAPSED_MINUTES = 90L
    private const val ALERT_BEHIND_KM = 2.0
    private const val ALERT_PROGRESS_RATIO = 0.60
    private const val ALERT_MIN_REMAINING_KM = 3.0
    private const val RATIO_EPSILON = 1.0e-9
    private const val MILLIS_PER_MINUTE = 60_000L
}
