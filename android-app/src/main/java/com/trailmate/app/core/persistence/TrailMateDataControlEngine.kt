package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.TrackRecordingState
import java.util.Locale

data class TrailMateDataControlSummary(
    val profileLine: String,
    val routeLine: String,
    val inventoryLine: String,
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
        val inventoryCount = snapshot.inventory.items.size
        val readyCount = snapshot.inventory.items.count { item -> item.available }
        val historyCount = snapshot.historicalActivities.size
        val trackLine = snapshot.latestTrackRecording.reviewLine()

        return TrailMateDataControlSummary(
            profileLine = profileLine,
            routeLine = routeLine,
            inventoryLine = "$inventoryCount 件装备 / $readyCount 件可用",
            trackLine = trackLine,
            hasRecordedTrack = snapshot.latestTrackRecording.pointCount > 0,
            exportPreview = buildExportPreview(
                snapshot = snapshot,
                inventoryCount = inventoryCount,
                readyCount = readyCount,
                historyCount = historyCount
            )
        )
    }

    private fun buildExportPreview(
        snapshot: TrailMateSnapshot,
        inventoryCount: Int,
        readyCount: Int,
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
            "装备：$inventoryCount 件，$readyCount 件可用",
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
