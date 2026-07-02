package com.trailmate.app.services.tracking

object TrackingServiceIntents {
    const val ActionStart = "com.trailmate.app.tracking.action.START"
    const val ActionStop = "com.trailmate.app.tracking.action.STOP"
    const val ForegroundNotificationId = 1001
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
    ): TrackingServiceDecision =
        when {
            action == TrackingServiceIntents.ActionStart && canStartLocationForeground ->
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
