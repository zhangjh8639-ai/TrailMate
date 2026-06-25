package com.trailmate.app.core.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.trailmate.app.MainActivity
import com.trailmate.app.core.model.RouteDeviationAlertDecision
import com.trailmate.app.core.model.RouteDeviationAlertDeliveryChannel
import com.trailmate.app.core.model.RouteDeviationAlertDeliveryEngine
import com.trailmate.app.core.model.RouteDeviationAlertDeliveryPlan

object RouteDeviationAlertAndroidDelivery {
    fun deliver(
        context: Context,
        decision: RouteDeviationAlertDecision,
        notificationPermissionGranted: Boolean
    ): RouteDeviationAlertDeliveryPlan {
        val vibrator = context.alertVibrator()
        val deviceCanVibrate = vibrator?.hasVibrator() == true
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision,
            notificationPermissionGranted = notificationPermissionGranted,
            deviceCanVibrate = deviceCanVibrate
        )

        if (plan.shouldPostNotification) {
            context.postRouteAlertNotification(plan)
        }
        if (plan.shouldVibrate && deviceCanVibrate) {
            vibrator.vibrateRouteAlert()
        }

        return plan
    }

    @SuppressLint("MissingPermission")
    private fun Context.postRouteAlertNotification(plan: RouteDeviationAlertDeliveryPlan) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureRouteAlertChannel(manager, plan.channel)
        runCatching {
            manager.notify(NOTIFICATION_ID, buildRouteAlertNotification(plan))
        }
    }

    private fun Context.buildRouteAlertNotification(plan: RouteDeviationAlertDeliveryPlan): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java),
            pendingIntentFlags()
        )

        return Notification.Builder(this, plan.channel.notificationChannelId())
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(plan.notificationTitle)
            .setContentText(plan.notificationText)
            .setStyle(Notification.BigTextStyle().bigText(plan.notificationText))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setShowWhen(true)
            .build()
    }

    private fun ensureRouteAlertChannel(
        manager: NotificationManager,
        channel: RouteDeviationAlertDeliveryChannel
    ) {
        val channelId = channel.notificationChannelId()
        if (channelId.isBlank() || manager.getNotificationChannel(channelId) != null) {
            return
        }
        val notificationChannel = NotificationChannel(
            channelId,
            channel.notificationChannelName(),
            channel.notificationImportance()
        ).apply {
            description = channel.notificationChannelDescription()
            enableVibration(false)
        }
        manager.createNotificationChannel(notificationChannel)
    }

    private fun Context.alertVibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    private fun Vibrator.vibrateRouteAlert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate(VibrationEffect.createOneShot(VIBRATION_MILLIS, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrate(VIBRATION_MILLIS)
        }
    }

    private fun pendingIntentFlags(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    private fun RouteDeviationAlertDeliveryChannel.notificationChannelId(): String =
        when (this) {
            RouteDeviationAlertDeliveryChannel.URGENT_ALERT -> "trailmate_route_alerts"
            RouteDeviationAlertDeliveryChannel.ROUTE_STATUS -> "trailmate_route_status"
            RouteDeviationAlertDeliveryChannel.NONE -> ""
        }

    private fun RouteDeviationAlertDeliveryChannel.notificationChannelName(): String =
        when (this) {
            RouteDeviationAlertDeliveryChannel.URGENT_ALERT -> "路线偏航提醒"
            RouteDeviationAlertDeliveryChannel.ROUTE_STATUS -> "路线状态提醒"
            RouteDeviationAlertDeliveryChannel.NONE -> ""
        }

    private fun RouteDeviationAlertDeliveryChannel.notificationChannelDescription(): String =
        when (this) {
            RouteDeviationAlertDeliveryChannel.URGENT_ALERT -> "路线页偏航提醒"
            RouteDeviationAlertDeliveryChannel.ROUTE_STATUS -> "路线页状态确认"
            RouteDeviationAlertDeliveryChannel.NONE -> ""
        }

    private fun RouteDeviationAlertDeliveryChannel.notificationImportance(): Int =
        when (this) {
            RouteDeviationAlertDeliveryChannel.URGENT_ALERT -> NotificationManager.IMPORTANCE_HIGH
            RouteDeviationAlertDeliveryChannel.ROUTE_STATUS -> NotificationManager.IMPORTANCE_DEFAULT
            RouteDeviationAlertDeliveryChannel.NONE -> NotificationManager.IMPORTANCE_LOW
        }

    private const val NOTIFICATION_ID = 1201
    private const val REQUEST_OPEN_APP = 2201
    private const val VIBRATION_MILLIS = 650L
}
