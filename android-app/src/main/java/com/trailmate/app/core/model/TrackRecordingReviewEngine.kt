package com.trailmate.app.core.model

import java.util.Locale
import kotlin.math.roundToInt

data class TrackRecordingReviewPresentation(
    val visible: Boolean,
    val title: String,
    val routeName: String,
    val distanceLabel: String,
    val pointCountLabel: String,
    val durationLabel: String,
    val caption: String,
    val primaryActionLabel: String
)

object TrackRecordingReviewEngine {
    fun present(trackRecording: TrackRecordingState): TrackRecordingReviewPresentation {
        if (
            trackRecording.status != TrackRecordingStatus.FINISHED ||
            trackRecording.pointCount < MIN_REVIEW_POINT_COUNT ||
            trackRecording.totalDistanceKm <= 0.0
        ) {
            return hidden()
        }

        return TrackRecordingReviewPresentation(
            visible = true,
            title = "轨迹已保存",
            routeName = trackRecording.routeName?.takeIf { it.isNotBlank() } ?: "本次徒步",
            distanceLabel = "${String.format(Locale.US, "%.1f", trackRecording.totalDistanceKm)} km",
            pointCountLabel = "${trackRecording.pointCount} 点",
            durationLabel = trackRecording.durationLabel(),
            caption = "可在数据页复盘本次路线表现。",
            primaryActionLabel = "去数据页复盘"
        )
    }

    private fun hidden(): TrackRecordingReviewPresentation =
        TrackRecordingReviewPresentation(
            visible = false,
            title = "",
            routeName = "",
            distanceLabel = "",
            pointCountLabel = "",
            durationLabel = "",
            caption = "",
            primaryActionLabel = ""
        )

    private fun TrackRecordingState.durationLabel(): String {
        val start = startedAtEpochMillis ?: points.firstOrNull()?.timestampEpochMillis
        val end = finishedAtEpochMillis ?: points.lastOrNull()?.timestampEpochMillis
        val durationMinutes = if (start != null && end != null && end >= start) {
            ((end - start) / 60_000.0).roundToInt().coerceAtLeast(1)
        } else {
            return "未记录"
        }
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        return if (hours > 0) {
            if (minutes > 0) "${hours}小时${minutes}分" else "${hours}小时"
        } else {
            "${minutes} 分"
        }
    }

    private const val MIN_REVIEW_POINT_COUNT = 2
}
