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
        val departureAction = DepartureReadinessPrimaryActionEngine.resolve(departureReadiness)
        if (
            hikeSessionStatus != HikeSessionStatus.READY ||
            trackRecordingStatus == TrackRecordingStatus.RECORDING ||
            trackRecordingStatus == TrackRecordingStatus.PAUSED ||
            departureAction.kind == DepartureReadinessPrimaryActionKind.START_HIKE_AND_RECORD
        ) {
            return TrackRecordingDepartureGateAction(
                label = currentTrackActionLabel,
                kind = TrackRecordingDepartureGateActionKind.APPLY_TRACK_ACTION
            )
        }

        return departureAction.toTrackAction()
    }

    private fun DepartureReadinessPrimaryAction.toTrackAction(): TrackRecordingDepartureGateAction =
        when (kind) {
            DepartureReadinessPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK -> TrackRecordingDepartureGateAction(
                label = label,
                kind = TrackRecordingDepartureGateActionKind.SAVE_OFFLINE_ROUTE_PACK
            )
            DepartureReadinessPrimaryActionKind.OPEN_OFFLINE_BASE_MAP -> TrackRecordingDepartureGateAction(
                label = label,
                kind = TrackRecordingDepartureGateActionKind.OPEN_OFFLINE_BASE_MAP
            )
            DepartureReadinessPrimaryActionKind.REQUEST_LOCATION -> TrackRecordingDepartureGateAction(
                label = label,
                kind = TrackRecordingDepartureGateActionKind.REQUEST_LOCATION
            )
            DepartureReadinessPrimaryActionKind.OPEN_LOCATION_SETTINGS -> TrackRecordingDepartureGateAction(
                label = label,
                kind = TrackRecordingDepartureGateActionKind.OPEN_LOCATION_SETTINGS
            )
            DepartureReadinessPrimaryActionKind.SHOW_GEAR -> TrackRecordingDepartureGateAction(
                label = label,
                kind = TrackRecordingDepartureGateActionKind.SHOW_GEAR
            )
            DepartureReadinessPrimaryActionKind.START_HIKE_AND_RECORD,
            DepartureReadinessPrimaryActionKind.BLOCKED -> TrackRecordingDepartureGateAction(
                label = label,
                kind = TrackRecordingDepartureGateActionKind.BLOCKED,
                enabled = false
            )
        }
}
