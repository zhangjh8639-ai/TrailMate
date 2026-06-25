package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import com.trailmate.app.core.location.TrailMateLocationFixReliability
import com.trailmate.app.core.map.TrailMapReadiness
import java.util.Locale

enum class RouteCockpitPrimaryActionKind {
    REQUEST_LOCATION,
    OPEN_LOCATION_SETTINGS,
    START_HIKE,
    START_RECORDING,
    PAUSE_RECORDING,
    RESUME_RECORDING,
    SAVE_OFFLINE_ROUTE_PACK,
    OPEN_OFFLINE_BASE_MAP,
    SHOW_GEAR,
    VIEW_RECOVERY,
    REVIEW_TRACK,
    RESET_SESSION
}

enum class RouteCockpitReadinessTone {
    READY,
    ATTENTION,
    BLOCKED
}

enum class RouteCockpitReadinessActionKind {
    NONE,
    REQUEST_LOCATION,
    OPEN_LOCATION_SETTINGS,
    START_RECORDING,
    PAUSE_RECORDING,
    RESUME_RECORDING,
    REVIEW_TRACK,
    SAVE_OFFLINE_ROUTE_PACK,
    SHOW_GEAR,
    OPEN_OFFLINE_BASE_MAP
}

data class RouteCockpitPrimaryAction(
    val label: String,
    val kind: RouteCockpitPrimaryActionKind,
    val enabled: Boolean = true
)

data class RouteCockpitReadinessItem(
    val label: String,
    val value: String,
    val tone: RouteCockpitReadinessTone,
    val actionKind: RouteCockpitReadinessActionKind = RouteCockpitReadinessActionKind.NONE
)

data class RouteCockpitPresentation(
    val routeTitle: String,
    val routeSubtitle: String,
    val currentCheckpointLabel: String,
    val nextCheckpointLabel: String,
    val progressFraction: Float,
    val progressLabel: String,
    val routeMatchLabel: String,
    val fieldCaption: String,
    val primaryAction: RouteCockpitPrimaryAction,
    val readinessItems: List<RouteCockpitReadinessItem>
)

object RouteCockpitPresentationEngine {
    fun build(
        route: ImportedRoute,
        plan: HikePlanSummary,
        session: HikeSessionState,
        liveGuidance: LiveCheckpointGuidance,
        mapReadiness: TrailMapReadiness,
        departureReadiness: DepartureReadinessSummary,
        locationSnapshot: TrailMateLocationSnapshot,
        locationGuidanceStatus: LocationBackedHikeStatus,
        trackRecording: TrackRecordingState,
        wasRecentlyOffRoute: Boolean,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): RouteCockpitPresentation {
        val current = HikeSessionEngine.currentCheckpoint(plan, session)
            ?: plan.checkpoints.firstOrNull()
        val next = HikeSessionEngine.nextCheckpoint(plan, session)
        val progress = HikeSessionEngine.progressFraction(plan, session).toFloat()

        return RouteCockpitPresentation(
            routeTitle = route.routeName,
            routeSubtitle = "${route.distanceKm.formatKm()} · +${route.ascentMeters}m",
            currentCheckpointLabel = current?.checkpointLabel() ?: "等待路线",
            nextCheckpointLabel = next?.let { "下一站 ${it.checkpointLabel()}" }
                ?: if (session.status == HikeSessionStatus.COMPLETED) "路线已完成" else liveGuidance.title,
            progressFraction = progress,
            progressLabel = "${(progress * 100).toInt()}% · ${current?.distanceKm?.formatKm() ?: "0km"} / ${route.distanceKm.formatKm()}",
            routeMatchLabel = routeMatchLabel(
                locationGuidanceStatus = locationGuidanceStatus,
                wasRecentlyOffRoute = wasRecentlyOffRoute
            ),
            fieldCaption = liveGuidance.caption,
            primaryAction = primaryAction(
                session = session,
                departureReadiness = departureReadiness,
                locationSnapshot = locationSnapshot,
                locationGuidanceStatus = locationGuidanceStatus,
                trackRecording = trackRecording,
                wasRecentlyOffRoute = wasRecentlyOffRoute,
                nowEpochMillis = nowEpochMillis
            ),
            readinessItems = readinessItems(
                departureReadiness = departureReadiness,
                locationSnapshot = locationSnapshot,
                trackRecording = trackRecording,
                mapReadiness = mapReadiness
            )
        )
    }

    private fun primaryAction(
        session: HikeSessionState,
        departureReadiness: DepartureReadinessSummary,
        locationSnapshot: TrailMateLocationSnapshot,
        locationGuidanceStatus: LocationBackedHikeStatus,
        trackRecording: TrackRecordingState,
        wasRecentlyOffRoute: Boolean,
        nowEpochMillis: Long
    ): RouteCockpitPrimaryAction {
        if (
            session.status == HikeSessionStatus.READY &&
            !departureReadiness.primaryActionLabel.isStartHikeAction()
        ) {
            return departureReadiness.primaryRepairAction()
        }

        if (locationSnapshot.needsLocationAuthorization()) {
            return locationSnapshot.locationRepairPrimaryAction()
        }

        if (session.status == HikeSessionStatus.READY && !locationSnapshot.isReliableForDeparture(nowEpochMillis)) {
            return RouteCockpitPrimaryAction(
                label = "等待定位稳定",
                kind = RouteCockpitPrimaryActionKind.REQUEST_LOCATION
            )
        }

        when (trackRecording.status) {
            TrackRecordingStatus.RECORDING -> return RouteCockpitPrimaryAction(
                label = "暂停",
                kind = RouteCockpitPrimaryActionKind.PAUSE_RECORDING
            )
            TrackRecordingStatus.PAUSED -> return RouteCockpitPrimaryAction(
                label = "继续",
                kind = RouteCockpitPrimaryActionKind.RESUME_RECORDING
            )
            TrackRecordingStatus.FINISHED -> if (trackRecording.pointCount > 0) {
                return RouteCockpitPrimaryAction(
                    label = "查看轨迹回顾",
                    kind = RouteCockpitPrimaryActionKind.REVIEW_TRACK
                )
            }
            TrackRecordingStatus.IDLE -> Unit
        }

        if (locationGuidanceStatus == LocationBackedHikeStatus.CHECK_ROUTE || wasRecentlyOffRoute) {
            return RouteCockpitPrimaryAction(
                label = "查看恢复建议",
                kind = RouteCockpitPrimaryActionKind.VIEW_RECOVERY
            )
        }

        return when (trackRecording.status) {
            TrackRecordingStatus.FINISHED -> {
                session.primarySessionAction()
            }
            TrackRecordingStatus.IDLE -> if (session.status == HikeSessionStatus.ACTIVE) {
                RouteCockpitPrimaryAction(
                    label = "开始记录",
                    kind = RouteCockpitPrimaryActionKind.START_RECORDING
                )
            } else {
                session.primarySessionAction()
            }
            TrackRecordingStatus.RECORDING,
            TrackRecordingStatus.PAUSED -> session.primarySessionAction()
        }
    }

    private fun DepartureReadinessSummary.primaryRepairAction(): RouteCockpitPrimaryAction =
        when {
            primaryActionLabel.isSaveRouteAction() -> RouteCockpitPrimaryAction(
                label = primaryActionLabel,
                kind = RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK
            )
            primaryActionLabel.isOfflineBaseMapRepairAction() -> RouteCockpitPrimaryAction(
                label = primaryActionLabel,
                kind = RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP
            )
            primaryActionLabel == "授权定位" -> RouteCockpitPrimaryAction(
                label = primaryActionLabel,
                kind = RouteCockpitPrimaryActionKind.REQUEST_LOCATION
            )
            primaryActionLabel == "打开系统定位" -> RouteCockpitPrimaryAction(
                label = primaryActionLabel,
                kind = RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS
            )
            primaryActionLabel == "等待定位稳定" || primaryActionLabel == "重试定位" -> RouteCockpitPrimaryAction(
                label = primaryActionLabel,
                kind = RouteCockpitPrimaryActionKind.REQUEST_LOCATION
            )
            primaryActionLabel.startsWith("补齐") -> RouteCockpitPrimaryAction(
                label = primaryActionLabel,
                kind = RouteCockpitPrimaryActionKind.SHOW_GEAR
            )
            else -> RouteCockpitPrimaryAction(
                label = primaryActionLabel,
                kind = RouteCockpitPrimaryActionKind.RESET_SESSION,
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

    private fun String.isStartHikeAction(): Boolean =
        this == START_HIKE_WITH_TRACK_LABEL

    private fun HikeSessionState.primarySessionAction(): RouteCockpitPrimaryAction =
        when (status) {
            HikeSessionStatus.READY -> RouteCockpitPrimaryAction(
                label = START_HIKE_WITH_TRACK_LABEL,
                kind = RouteCockpitPrimaryActionKind.START_HIKE
            )
            HikeSessionStatus.ACTIVE -> RouteCockpitPrimaryAction(
                label = "开始记录",
                kind = RouteCockpitPrimaryActionKind.START_RECORDING
            )
            HikeSessionStatus.PAUSED -> RouteCockpitPrimaryAction(
                label = "继续",
                kind = RouteCockpitPrimaryActionKind.RESUME_RECORDING
            )
            HikeSessionStatus.COMPLETED -> RouteCockpitPrimaryAction(
                label = "查看轨迹回顾",
                kind = RouteCockpitPrimaryActionKind.REVIEW_TRACK
            )
        }

    private fun readinessItems(
        departureReadiness: DepartureReadinessSummary,
        locationSnapshot: TrailMateLocationSnapshot,
        trackRecording: TrackRecordingState,
        mapReadiness: TrailMapReadiness
    ): List<RouteCockpitReadinessItem> =
        listOf(
            gpsReadiness(locationSnapshot),
            recordingReadiness(trackRecording),
            departureReadiness.stepReadiness(
                sourceLabel = "离线路线",
                displayLabel = "离线路线",
                fallbackValue = mapReadiness.setupHint.statusLabel,
                attentionActionKind = RouteCockpitReadinessActionKind.SAVE_OFFLINE_ROUTE_PACK,
                blockedActionKind = RouteCockpitReadinessActionKind.SAVE_OFFLINE_ROUTE_PACK
            ),
            departureReadiness.stepReadiness(
                sourceLabel = "离线地图包",
                displayLabel = "离线地图包",
                fallbackValue = mapReadiness.setupHint.statusLabel,
                attentionActionKind = RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP,
                blockedActionKind = RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP
            ),
            departureReadiness.stepReadiness(
                sourceLabel = "装备",
                fallbackValue = "待检查",
                readyActionKind = RouteCockpitReadinessActionKind.SHOW_GEAR,
                attentionActionKind = RouteCockpitReadinessActionKind.SHOW_GEAR,
                blockedActionKind = RouteCockpitReadinessActionKind.SHOW_GEAR
            )
        )

    private fun gpsReadiness(locationSnapshot: TrailMateLocationSnapshot): RouteCockpitReadinessItem =
        when (locationSnapshot.status) {
            TrailMateLocationStatus.LOCATED -> RouteCockpitReadinessItem(
                label = "定位",
                value = locationSnapshot.horizontalAccuracyMeters?.let { "约 ${it.toInt()} m" } ?: "可用",
                tone = RouteCockpitReadinessTone.READY
            )
            TrailMateLocationStatus.SEARCHING,
            TrailMateLocationStatus.LOW_ACCURACY -> RouteCockpitReadinessItem(
                label = "定位",
                value = if (locationSnapshot.status == TrailMateLocationStatus.SEARCHING) "搜索中" else "精度弱",
                tone = RouteCockpitReadinessTone.ATTENTION,
                actionKind = RouteCockpitReadinessActionKind.REQUEST_LOCATION
            )
            TrailMateLocationStatus.PERMISSION_REQUIRED -> RouteCockpitReadinessItem(
                label = "定位",
                value = "待授权",
                tone = RouteCockpitReadinessTone.BLOCKED,
                actionKind = RouteCockpitReadinessActionKind.REQUEST_LOCATION
            )
            TrailMateLocationStatus.PROVIDER_DISABLED -> RouteCockpitReadinessItem(
                label = "定位",
                value = "系统未开启",
                tone = RouteCockpitReadinessTone.BLOCKED,
                actionKind = RouteCockpitReadinessActionKind.OPEN_LOCATION_SETTINGS
            )
            TrailMateLocationStatus.DISABLED,
            TrailMateLocationStatus.UNAVAILABLE -> RouteCockpitReadinessItem(
                label = "定位",
                value = "待确认",
                tone = RouteCockpitReadinessTone.BLOCKED,
                actionKind = RouteCockpitReadinessActionKind.REQUEST_LOCATION
            )
        }

    private fun recordingReadiness(trackRecording: TrackRecordingState): RouteCockpitReadinessItem =
        when (trackRecording.status) {
            TrackRecordingStatus.RECORDING -> RouteCockpitReadinessItem(
                label = "记录",
                value = "${trackRecording.pointCount} 点",
                tone = RouteCockpitReadinessTone.READY,
                actionKind = RouteCockpitReadinessActionKind.PAUSE_RECORDING
            )
            TrackRecordingStatus.PAUSED -> RouteCockpitReadinessItem(
                label = "记录",
                value = "暂停",
                tone = RouteCockpitReadinessTone.ATTENTION,
                actionKind = RouteCockpitReadinessActionKind.RESUME_RECORDING
            )
            TrackRecordingStatus.FINISHED -> RouteCockpitReadinessItem(
                label = "记录",
                value = if (trackRecording.pointCount > 0) "已保存" else "未开始",
                tone = if (trackRecording.pointCount > 0) {
                    RouteCockpitReadinessTone.READY
                } else {
                    RouteCockpitReadinessTone.ATTENTION
                },
                actionKind = if (trackRecording.pointCount > 0) {
                    RouteCockpitReadinessActionKind.REVIEW_TRACK
                } else {
                    RouteCockpitReadinessActionKind.START_RECORDING
                }
            )
            TrackRecordingStatus.IDLE -> RouteCockpitReadinessItem(
                label = "记录",
                value = "未开始",
                tone = RouteCockpitReadinessTone.ATTENTION,
                actionKind = RouteCockpitReadinessActionKind.START_RECORDING
            )
        }

    private fun DepartureReadinessSummary.stepReadiness(
        sourceLabel: String,
        displayLabel: String = sourceLabel,
        fallbackValue: String,
        readyActionKind: RouteCockpitReadinessActionKind = RouteCockpitReadinessActionKind.NONE,
        attentionActionKind: RouteCockpitReadinessActionKind = RouteCockpitReadinessActionKind.NONE,
        blockedActionKind: RouteCockpitReadinessActionKind = RouteCockpitReadinessActionKind.NONE
    ): RouteCockpitReadinessItem {
        val step = steps.firstOrNull { it.label == sourceLabel }
        val tone = when {
            step?.ready == true -> RouteCockpitReadinessTone.READY
            step?.value?.contains("缺") == true -> RouteCockpitReadinessTone.BLOCKED
            else -> RouteCockpitReadinessTone.ATTENTION
        }

        return RouteCockpitReadinessItem(
            label = displayLabel,
            value = step?.value ?: fallbackValue,
            tone = tone,
            actionKind = when (tone) {
                RouteCockpitReadinessTone.READY -> readyActionKind
                RouteCockpitReadinessTone.ATTENTION -> attentionActionKind
                RouteCockpitReadinessTone.BLOCKED -> blockedActionKind
            }
        )
    }

    private fun routeMatchLabel(
        locationGuidanceStatus: LocationBackedHikeStatus,
        wasRecentlyOffRoute: Boolean
    ): String =
        when {
            locationGuidanceStatus == LocationBackedHikeStatus.CHECK_ROUTE || wasRecentlyOffRoute -> "需核对路线"
            locationGuidanceStatus == LocationBackedHikeStatus.LOW_ACCURACY -> "待精度稳定"
            locationGuidanceStatus == LocationBackedHikeStatus.ON_ROUTE -> "在线路上"
            locationGuidanceStatus == LocationBackedHikeStatus.FINISHED -> "已完成"
            else -> "等待开始"
        }

    private fun TrailMateLocationSnapshot.needsLocationAuthorization(): Boolean =
        status == TrailMateLocationStatus.DISABLED ||
            status == TrailMateLocationStatus.PERMISSION_REQUIRED ||
        status == TrailMateLocationStatus.PROVIDER_DISABLED ||
            status == TrailMateLocationStatus.UNAVAILABLE

    private fun TrailMateLocationSnapshot.locationRepairPrimaryAction(): RouteCockpitPrimaryAction =
        when (status) {
            TrailMateLocationStatus.PROVIDER_DISABLED -> RouteCockpitPrimaryAction(
                label = "打开系统定位",
                kind = RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS
            )
            TrailMateLocationStatus.UNAVAILABLE -> RouteCockpitPrimaryAction(
                label = "重试定位",
                kind = RouteCockpitPrimaryActionKind.REQUEST_LOCATION
            )
            else -> RouteCockpitPrimaryAction(
                label = "授权定位",
                kind = RouteCockpitPrimaryActionKind.REQUEST_LOCATION
            )
        }

    private fun TrailMateLocationSnapshot.isReliableForDeparture(nowEpochMillis: Long): Boolean =
        TrailMateLocationFixReliability.isReliableForFieldUse(
            snapshot = this,
            nowEpochMillis = nowEpochMillis,
            maxAccuracyMeters = MAX_DEPARTURE_ACCURACY_METERS
        )

    private fun HikePlanCheckpoint.checkpointLabel(): String =
        "$title · ${distanceKm.formatKm()}"

    private fun Double.formatKm(): String =
        if (this == 0.0) "0km" else "${String.format(Locale.US, "%.1f", this)}km"

    private const val MAX_DEPARTURE_ACCURACY_METERS = 50.0
    private const val START_HIKE_WITH_TRACK_LABEL = "开始徒步并记录轨迹"
}
