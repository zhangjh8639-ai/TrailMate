package com.trailmate.app.services.tracking

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.trailmate.app.MainActivity
import com.trailmate.app.R

class TrackingForegroundService : Service() {
    private val controller = TrackingServiceController()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val decision = controller.handle(
            action = intent?.action,
            canStartLocationForeground = hasForegroundLocationPermission(),
        )

        decision.commands.forEach { command ->
            when (command) {
                TrackingServiceCommand.StartForeground -> startTrackingForeground()
                TrackingServiceCommand.StopForeground -> stopForeground(STOP_FOREGROUND_REMOVE)
                TrackingServiceCommand.StopSelf -> stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    private fun startTrackingForeground() {
        val content = TrackingNotificationContent.active()
        createNotificationChannel(content)
        startForeground(
            TrackingServiceIntents.ForegroundNotificationId,
            buildNotification(content),
        )
    }

    private fun createNotificationChannel(content: TrackingNotificationContent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            content.channelId,
            content.channelName,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = content.text
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(content: TrackingNotificationContent): Notification =
        NotificationCompat.Builder(this, content.channelId)
            .setSmallIcon(R.drawable.ic_tracking_notification)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(mainActivityPendingIntent())
            .addAction(
                R.drawable.ic_tracking_notification,
                "结束",
                servicePendingIntent(TrackingServiceIntents.ActionStop),
            )
            .build()

    private fun mainActivityPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun servicePendingIntent(action: String): PendingIntent =
        PendingIntent.getService(
            this,
            action.hashCode(),
            intentFor(this, action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun hasForegroundLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        fun startIntent(context: Context): Intent =
            intentFor(context, TrackingServiceIntents.ActionStart)

        fun stopIntent(context: Context): Intent =
            intentFor(context, TrackingServiceIntents.ActionStop)

        private fun intentFor(
            context: Context,
            action: String,
        ): Intent =
            Intent(context, TrackingForegroundService::class.java).setAction(action)
    }
}
