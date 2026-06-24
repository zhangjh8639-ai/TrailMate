package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.TrackRecordingState
import java.util.Locale

data class TrailMateDataControlSummary(
    val profileLine: String,
    val routeLine: String,
    val gearMatchLine: String,
    val trackLine: String,
    val hasRecordedTrack: Boolean,
    val exportPreview: String
)

object TrailMateDataControlEngine {
    fun summarize(snapshot: TrailMateSnapshot): TrailMateDataControlSummary {
        val profileLine = snapshot.profile?.let { "资料已保存" } ?: "未保存资料"
        val routeLine = snapshot.importedRoute?.let { route ->
            "${route.routeName} / ${String.format(Locale.US, "%.1f km", route.distanceKm)} / +${route.ascentMeters} m"
        } ?: "尚未导入路线"
        val historyCount = snapshot.historicalActivities.size
        val trackLine = snapshot.latestTrackRecording.reviewLine()

        return TrailMateDataControlSummary(
            profileLine = profileLine,
            routeLine = routeLine,
            gearMatchLine = "装备匹配缓存来自服务端品牌库",
            trackLine = trackLine,
            hasRecordedTrack = snapshot.latestTrackRecording.pointCount > 0,
            exportPreview = buildExportPreview(
                snapshot = snapshot,
                historyCount = historyCount
            )
        )
    }

    private fun buildExportPreview(
        snapshot: TrailMateSnapshot,
        historyCount: Int
    ): String {
        val profilePreview = if (snapshot.profile != null) {
            "资料：已保存"
        } else {
            "资料：未保存"
        }
        val routePreview = snapshot.importedRoute?.let { route ->
            "路线：${route.routeName}，${String.format(Locale.US, "%.1f km", route.distanceKm)}，+${route.ascentMeters} m"
        } ?: "路线：无"
        val trackPreview = snapshot.latestTrackRecording.takeIf { it.pointCount > 0 }?.let { track ->
            "轨迹：${track.displayName()}，${String.format(Locale.US, "%.1f km", track.totalDistanceKm)}，${track.pointCount} 个点"
        }

        return listOfNotNull(
            profilePreview,
            routePreview,
            "历史：$historyCount 条 GPX",
            "装备匹配：服务端品牌库候选缓存",
            trackPreview
        ).joinToString("; ")
    }

    private fun TrackRecordingState.reviewLine(): String =
        if (pointCount > 0) {
            "${displayName()} / 已记录 ${String.format(Locale.US, "%.1f km", totalDistanceKm)} / $pointCount 个点"
        } else {
            "尚未记录轨迹"
        }

    private fun TrackRecordingState.displayName(): String =
        routeName?.takeIf { it.isNotBlank() } ?: "未命名轨迹"
}
