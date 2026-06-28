package com.trailmate.app.core.model

enum class TrackRecordingDepartureGateActionKind {
    APPLY_TRACK_ACTION,
    SAVE_OFFLINE_ROUTE_PACK,
    OPEN_OFFLINE_BASE_MAP,
    REQUEST_LOCATION,
    OPEN_LOCATION_SETTINGS,
    SHOW_GEAR,
    BLOCKED
}

data class TrackRecordingDepartureGateAction(
    val label: String,
    val kind: TrackRecordingDepartureGateActionKind,
    val enabled: Boolean = true
)

object TrackRecordingDepartureGateEngine {
    fun present(
        hikeSessionStatus: HikeSessionStatus,
        trackRecordingStatus: TrackRecordingStatus,
        currentTrackActionLabel: String,
        departureReadiness: DepartureReadinessSummary
    ): TrackRecordingDepartureGateAction {
        if (
            hikeSessionStatus != HikeSessionStatus.READY ||
            trackRecordingStatus == TrackRecordingStatus.RECORDING ||
            trackRecordingStatus == TrackRecordingStatus.PAUSED ||
            departureReadiness.primaryActionLabel == START_HIKE_WITH_TRACK_LABEL
        ) {
            return TrackRecordingDepartureGateAction(
                label = currentTrackActionLabel,
                kind = TrackRecordingDepartureGateActionKind.APPLY_TRACK_ACTION
            )
        }

        return departureReadiness.primaryActionLabel.toRepairAction()
    }

    private fun String.toRepairAction(): TrackRecordingDepartureGateAction =
        when {
            isSaveRouteAction() -> TrackRecordingDepartureGateAction(
                label = this,
                kind = TrackRecordingDepartureGateActionKind.SAVE_OFFLINE_ROUTE_PACK
            )
            isOfflineBaseMapRepairAction() -> TrackRecordingDepartureGateAction(
                label = this,
                kind = TrackRecordingDepartureGateActionKind.OPEN_OFFLINE_BASE_MAP
            )
            this == "授权定位" -> TrackRecordingDepartureGateAction(
                label = this,
                kind = TrackRecordingDepartureGateActionKind.REQUEST_LOCATION
            )
            this == "打开系统定位" -> TrackRecordingDepartureGateAction(
                label = this,
                kind = TrackRecordingDepartureGateActionKind.OPEN_LOCATION_SETTINGS
            )
            this == "等待定位稳定" || this == "重试定位" -> TrackRecordingDepartureGateAction(
                label = this,
                kind = TrackRecordingDepartureGateActionKind.REQUEST_LOCATION
            )
            startsWith("补齐") -> TrackRecordingDepartureGateAction(
                label = this,
                kind = TrackRecordingDepartureGateActionKind.SHOW_GEAR
            )
            else -> TrackRecordingDepartureGateAction(
                label = this,
                kind = TrackRecordingDepartureGateActionKind.BLOCKED,
                enabled = false
            )
        }

    private fun String.isOfflineBaseMapRepairAction(): Boolean =
        this == "导入离线地图包" ||
            this == "导入底图" ||
            this == "导入离线底图" ||
            this == "下载底图" ||
            contains("离线地图包") ||
            contains("离线底图") ||
            this == "飞行模式验证底图"

    private fun String.isSaveRouteAction(): Boolean =
        this == "保存离线路线" || this == "保存 GPX 路线" || this == "保存路线包"

    private const val START_HIKE_WITH_TRACK_LABEL = "开始徒步并记录轨迹"
}
