package com.trailmate.app.feature.navigation

import com.trailmate.app.core.database.TrackingSessionRecord
import com.trailmate.app.core.database.TrackingTrackPointRecord
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.feature.routes.RouteDetailState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class NavigationTabState(
    val title: String,
    val idleState: NavigationIdleState,
    val selectedRoute: NavigationRouteReadyState? = null,
    val trackingStartState: TrackingStartUiState = TrackingStartUiState.ready(),
    val recoveredSession: NavigationRecoveredTrackingSessionState? = null,
) {
    fun visibleText(): List<String> =
        buildList {
            add(title)
            visibleRecoveredSession()?.let { addAll(it.visibleText()) }
            selectedRoute?.let {
                addAll(
                    it.visibleText(
                        includeChangeRouteAction = trackingStartState.mode != TrackingStartMode.Active,
                    ),
                )
                if (visibleRecoveredSession() == null) {
                    addAll(trackingStartState.visibleText())
                } else {
                    add(RecoveryBlocksNewTrackingMessage)
                }
            } ?: addAll(idleState.visibleText())
        }

    fun visibleRecoveredSession(): NavigationRecoveredTrackingSessionState? =
        recoveredSession.takeIf { trackingStartState.mode != TrackingStartMode.Active }

    fun canStartNewTracking(): Boolean =
        selectedRoute != null &&
            trackingStartState.mode != TrackingStartMode.Active &&
            visibleRecoveredSession() == null
}

data class NavigationIdleState(
    val title: String,
    val body: String,
    val routeActionLabel: String,
) {
    fun visibleText(): List<String> = listOf(title, body, routeActionLabel)
}

data class NavigationRouteReadyState(
    val routeKey: String,
    val statusLabel: String,
    val routeName: String,
    val region: String,
    val sourceLabel: String,
    val offlineStatusLabel: String,
    val metrics: List<NavigationMetricState>,
    val confidenceLabel: String,
    val riskTags: List<String>,
    val boundaryNotes: List<String>,
    val changeRouteActionLabel: String = "更换路线",
) {
    fun visibleText(includeChangeRouteAction: Boolean = true): List<String> =
        buildList {
            add(statusLabel)
            add(routeName)
            add(region)
            add(sourceLabel)
            add(offlineStatusLabel)
            metrics.forEach { metric ->
                add(metric.label)
                add(metric.value)
            }
            add(confidenceLabel)
            if (includeChangeRouteAction) {
                add(changeRouteActionLabel)
            }
            addAll(riskTags)
            addAll(boundaryNotes)
        }
}

data class NavigationMetricState(
    val label: String,
    val value: String,
)

data class NavigationRecoveredTrackingSessionState(
    val sessionId: NavigationSessionId,
    val routeId: RouteId,
    val title: String,
    val body: String,
    val privacyLabel: String,
    val routeLabel: String,
    val pointCountLabel: String,
    val startedAtLabel: String,
    val lastRecordedAtLabel: String?,
    val endActionLabel: String,
) {
    fun visibleText(): List<String> =
        buildList {
            add(title)
            add(body)
            add(privacyLabel)
            add(routeLabel)
            add(pointCountLabel)
            add(startedAtLabel)
            lastRecordedAtLabel?.let(::add)
            add(endActionLabel)
        }

    companion object {
        fun from(
            record: TrackingSessionRecord,
            points: List<TrackingTrackPointRecord>,
        ): NavigationRecoveredTrackingSessionState {
            val sampleCount = record.sampleCount.coerceAtLeast(points.size)
            val lastRecordedAt = points.maxByOrNull { it.pointIndex }?.recordedAtEpochMillis

            return NavigationRecoveredTrackingSessionState(
                sessionId = record.sessionId,
                routeId = record.routeId,
                title = "发现未结束的本地记录",
                body = "这段记录仍保存在本机，尚未结束；它不会自动上传或分享，也不代表实时 GPS 服务正在运行。",
                privacyLabel = "本机私密",
                routeLabel = "路线 ID：${record.routeId.value}",
                pointCountLabel = "已记录 $sampleCount 个定位点",
                startedAtLabel = "开始：${formatLocalTime(record.startedAtEpochMillis)}",
                lastRecordedAtLabel = lastRecordedAt?.let { "最后定位点：${formatLocalTime(it)}" },
                endActionLabel = "结束本地记录",
            )
        }

        private fun formatLocalTime(epochMillis: Long): String =
            TimeFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
    }
}

const val RecoveryBlocksNewTrackingMessage = "请先结束本地记录，再开始新的轨迹导航。"

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")

object NavigationTabSampleState {
    fun build(): NavigationTabState =
        NavigationTabState(
            title = "导航",
            idleState = NavigationIdleState(
                title = "尚未选择路线",
                body = "从路线页选择可导航路线，确认轨迹、离线状态和风险后再进入导航。",
                routeActionLabel = "去路线页选择可导航路线",
            ),
        )
}

fun NavigationTabState.withSelectedRoute(detail: RouteDetailState): NavigationTabState =
    copy(selectedRoute = detail.toNavigationRouteReadyState())

fun NavigationTabState.withTrackingStartState(state: TrackingStartUiState): NavigationTabState =
    copy(trackingStartState = state)

fun NavigationTabState.withRecoveredTrackingSession(
    session: NavigationRecoveredTrackingSessionState?,
): NavigationTabState =
    copy(recoveredSession = session)

private fun RouteDetailState.toNavigationRouteReadyState(): NavigationRouteReadyState =
    NavigationRouteReadyState(
        routeKey = routeKey,
        statusLabel = "轨迹导航待开始",
        routeName = title,
        region = subtitle,
        sourceLabel = sourceLabel,
        offlineStatusLabel = offlineStatusLabel,
        metrics = metrics.map { metric ->
            NavigationMetricState(
                label = metric.label,
                value = metric.value,
            )
        },
        confidenceLabel = confidenceLabel,
        riskTags = riskTags,
        boundaryNotes = boundaryNotes,
    )
