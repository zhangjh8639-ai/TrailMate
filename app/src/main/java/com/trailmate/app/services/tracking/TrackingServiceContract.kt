package com.trailmate.app.services.tracking

import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteId
import java.time.Instant

object TrackingServiceIntents {
    const val ActionStart = "com.trailmate.app.tracking.action.START"
    const val ActionStop = "com.trailmate.app.tracking.action.STOP"
    const val ForegroundNotificationId = 1001
    const val ExtraSessionId = "com.trailmate.app.tracking.extra.SESSION_ID"
    const val ExtraRouteId = "com.trailmate.app.tracking.extra.ROUTE_ID"
    const val ExtraStartedAtEpochMillis = "com.trailmate.app.tracking.extra.STARTED_AT_EPOCH_MILLIS"
    const val ExtraDirection = "com.trailmate.app.tracking.extra.DIRECTION"
}

data class TrackingServiceStartRequest(
    val sessionId: NavigationSessionId,
    val routeId: RouteId,
    val startedAtEpochMillis: Long,
    val direction: NavigationDirection = NavigationDirection.Forward,
) {
    init {
        require(startedAtEpochMillis > 0L) { "Tracking start timestamp must be positive." }
    }

    fun toNavigationSession(): NavigationSession =
        NavigationSession(
            id = sessionId,
            routeId = routeId,
            startedAt = Instant.ofEpochMilli(startedAtEpochMillis),
            state = NavigationState.Navigating,
            direction = direction,
            visibility = PrivacyVisibility.Private,
        )

    companion object {
        fun fromSession(session: NavigationSession): TrackingServiceStartRequest =
            TrackingServiceStartRequest(
                sessionId = session.id,
                routeId = session.routeId,
                startedAtEpochMillis = session.startedAt.toEpochMilli(),
                direction = session.direction,
            )
    }
}

enum class TrackingServiceCommand {
    StartForeground,
    StartLocationUpdates,
    StopLocationUpdates,
    StopForeground,
    StopSelf,
}

enum class TrackingServiceStartResult {
    NotSticky,
}

data class TrackingServiceDecision(
    val commands: List<TrackingServiceCommand>,
    val startResult: TrackingServiceStartResult = TrackingServiceStartResult.NotSticky,
)

class TrackingServiceController {
    fun handle(
        action: String?,
        canStartLocationForeground: Boolean,
        hasTrackingStartContext: Boolean,
    ): TrackingServiceDecision =
        when {
            action == TrackingServiceIntents.ActionStart &&
                canStartLocationForeground &&
                hasTrackingStartContext ->
                TrackingServiceDecision(
                    commands = listOf(
                        TrackingServiceCommand.StartForeground,
                        TrackingServiceCommand.StartLocationUpdates,
                    ),
                )
            action == TrackingServiceIntents.ActionStart ->
                TrackingServiceDecision(
                    commands = listOf(
                        TrackingServiceCommand.StopLocationUpdates,
                        TrackingServiceCommand.StopForeground,
                        TrackingServiceCommand.StopSelf,
                    ),
                )
            else -> TrackingServiceDecision(
                commands = listOf(
                    TrackingServiceCommand.StopLocationUpdates,
                    TrackingServiceCommand.StopForeground,
                    TrackingServiceCommand.StopSelf,
                ),
            )
        }
}

data class TrackingNotificationContent(
    val channelId: String,
    val channelName: String,
    val title: String,
    val text: String,
) {
    companion object {
        fun active(): TrackingNotificationContent =
            TrackingNotificationContent(
                channelId = "trailmate_tracking",
                channelName = "轨迹导航",
                title = "TrailMate 导航进行中",
                text = "正在保持轨迹导航，锁屏时也会显示运行状态。",
            )
    }
}
