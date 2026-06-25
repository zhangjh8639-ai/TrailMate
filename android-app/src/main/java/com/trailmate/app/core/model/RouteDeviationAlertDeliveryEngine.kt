package com.trailmate.app.core.model

enum class RouteDeviationAlertDeliveryTone {
    NONE,
    URGENT,
    REJOINED
}

enum class RouteDeviationAlertDeliveryChannel {
    NONE,
    URGENT_ALERT,
    ROUTE_STATUS
}

data class RouteDeviationAlertDeliveryPlan(
    val shouldPostNotification: Boolean,
    val shouldVibrate: Boolean,
    val tone: RouteDeviationAlertDeliveryTone,
    val channel: RouteDeviationAlertDeliveryChannel,
    val notificationTitle: String,
    val notificationText: String,
    val inAppOnlyReason: String?
)

object RouteDeviationAlertDeliveryEngine {
    fun resolve(
        decision: RouteDeviationAlertDecision,
        notificationPermissionGranted: Boolean,
        deviceCanVibrate: Boolean
    ): RouteDeviationAlertDeliveryPlan {
        val tone = when (decision.kind) {
            RouteDeviationAlertKind.OFF_ROUTE,
            RouteDeviationAlertKind.OFF_ROUTE_ESCALATED -> RouteDeviationAlertDeliveryTone.URGENT
            RouteDeviationAlertKind.REJOINED_ROUTE -> RouteDeviationAlertDeliveryTone.REJOINED
            RouteDeviationAlertKind.NONE,
            RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX,
            RouteDeviationAlertKind.OFF_ROUTE_SILENT -> RouteDeviationAlertDeliveryTone.NONE
        }
        if (tone == RouteDeviationAlertDeliveryTone.NONE) {
            return none()
        }

        return RouteDeviationAlertDeliveryPlan(
            shouldPostNotification = decision.shouldNotify && notificationPermissionGranted,
            shouldVibrate = tone == RouteDeviationAlertDeliveryTone.URGENT &&
                decision.shouldVibrate &&
                deviceCanVibrate,
            tone = tone,
            channel = when (tone) {
                RouteDeviationAlertDeliveryTone.URGENT -> RouteDeviationAlertDeliveryChannel.URGENT_ALERT
                RouteDeviationAlertDeliveryTone.REJOINED -> RouteDeviationAlertDeliveryChannel.ROUTE_STATUS
                RouteDeviationAlertDeliveryTone.NONE -> RouteDeviationAlertDeliveryChannel.NONE
            },
            notificationTitle = when (tone) {
                RouteDeviationAlertDeliveryTone.URGENT -> "TrailMate 偏航提醒"
                RouteDeviationAlertDeliveryTone.REJOINED -> "TrailMate 路线提醒"
                RouteDeviationAlertDeliveryTone.NONE -> ""
            },
            notificationText = "${decision.title}：${decision.caption}",
            inAppOnlyReason = if (
                tone == RouteDeviationAlertDeliveryTone.URGENT &&
                decision.shouldNotify &&
                !notificationPermissionGranted
            ) {
                "通知权限未开启，TrailMate 只能在路线页内显示偏航提醒。"
            } else {
                null
            }
        )
    }

    private fun none(): RouteDeviationAlertDeliveryPlan =
        RouteDeviationAlertDeliveryPlan(
            shouldPostNotification = false,
            shouldVibrate = false,
            tone = RouteDeviationAlertDeliveryTone.NONE,
            channel = RouteDeviationAlertDeliveryChannel.NONE,
            notificationTitle = "",
            notificationText = "",
            inAppOnlyReason = null
        )
}
