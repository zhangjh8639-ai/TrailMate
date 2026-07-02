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
import android.location.LocationManager
import android.os.Build
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.trailmate.app.MainActivity
import com.trailmate.app.R
import com.trailmate.app.core.database.SqliteTrackingRecordingStore
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.platform.location.AndroidLocationProvider

class TrackingForegroundService : Service() {
    private val controller = TrackingServiceController()
    private var trackingLocationSession: TrackingLocationSession? = null
    private var trackingStartRequest: TrackingServiceStartRequest? = null
    private var locationHandlerThread: HandlerThread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val startRequest = intent?.trackingStartRequestOrNull()
        val decision = controller.handle(
            action = intent?.action,
            canStartLocationForeground = hasForegroundLocationPermission(),
            hasTrackingStartContext = startRequest != null,
        )

        decision.commands.forEach { command ->
            when (command) {
                TrackingServiceCommand.StartForeground -> startTrackingForeground()
                TrackingServiceCommand.StartLocationUpdates -> startLocationUpdatesOrStop(startId, startRequest)
                TrackingServiceCommand.StopLocationUpdates -> stopLocationUpdates(
                    markRecordingEnded = intent?.action == TrackingServiceIntents.ActionStop,
                )
                TrackingServiceCommand.StopForeground -> stopForeground(STOP_FOREGROUND_REMOVE)
                TrackingServiceCommand.StopSelf -> stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates(markRecordingEnded = false)
        super.onDestroy()
    }

    private fun startLocationUpdatesOrStop(
        startId: Int,
        request: TrackingServiceStartRequest?,
    ) {
        if (request == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
            return
        }

        val state = locationSession(request).start()
        if (!state.requiresTrackingServiceShutdownAfterStart()) return

        stopLocationUpdates(markRecordingEnded = true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
    }

    private fun stopLocationUpdates(markRecordingEnded: Boolean) {
        trackingLocationSession?.stop(markRecordingEnded = markRecordingEnded)
        trackingLocationSession = null
        trackingStartRequest = null
        stopLocationHandlerThread()
    }

    private fun locationSession(request: TrackingServiceStartRequest): TrackingLocationSession {
        val existingSession = trackingLocationSession
        val existingRequest = trackingStartRequest
        if (existingSession != null && existingRequest == request) return existingSession
        if (existingSession != null && existingRequest != request) return existingSession

        return TrackingLocationSession(
            AndroidLocationProvider(
                getSystemService(Context.LOCATION_SERVICE) as LocationManager,
                looper = locationLooper(),
            ),
            recordingContext = TrackingRecordingContext(
                session = request.toNavigationSession(),
                store = SqliteTrackingRecordingStore(this),
            ),
        ).also {
            trackingLocationSession = it
            trackingStartRequest = request
        }
    }

    private fun locationLooper(): Looper {
        val existingThread = locationHandlerThread?.takeIf { it.isAlive }
        if (existingThread != null) return existingThread.looper

        return HandlerThread("TrailMateTrackingLocation").also { thread ->
            thread.start()
            locationHandlerThread = thread
        }.looper
    }

    private fun stopLocationHandlerThread() {
        locationHandlerThread?.quitSafely()
        locationHandlerThread = null
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
        fun startIntent(
            context: Context,
            request: TrackingServiceStartRequest,
        ): Intent =
            intentFor(context, TrackingServiceIntents.ActionStart).apply {
                putExtra(TrackingServiceIntents.ExtraSessionId, request.sessionId.value)
                putExtra(TrackingServiceIntents.ExtraRouteId, request.routeId.value)
                putExtra(TrackingServiceIntents.ExtraStartedAtEpochMillis, request.startedAtEpochMillis)
                putExtra(TrackingServiceIntents.ExtraDirection, request.direction.name)
            }

        fun stopIntent(context: Context): Intent =
            intentFor(context, TrackingServiceIntents.ActionStop)

        private fun intentFor(
            context: Context,
            action: String,
        ): Intent =
            Intent(context, TrackingForegroundService::class.java).setAction(action)
    }
}

private fun Intent.trackingStartRequestOrNull(): TrackingServiceStartRequest? {
    if (action != TrackingServiceIntents.ActionStart) return null

    val sessionId = getStringExtra(TrackingServiceIntents.ExtraSessionId)?.takeIf { it.isNotBlank() }
        ?: return null
    val routeId = getStringExtra(TrackingServiceIntents.ExtraRouteId)?.takeIf { it.isNotBlank() }
        ?: return null
    val startedAtEpochMillis = getLongExtra(TrackingServiceIntents.ExtraStartedAtEpochMillis, -1L)
    if (startedAtEpochMillis <= 0L) return null

    val direction = getStringExtra(TrackingServiceIntents.ExtraDirection)
        ?.let { enumValueOrNull<NavigationDirection>(it) }
        ?: return null

    return runCatching {
        TrackingServiceStartRequest(
            sessionId = NavigationSessionId(sessionId),
            routeId = RouteId(routeId),
            startedAtEpochMillis = startedAtEpochMillis,
            direction = direction,
        )
    }.getOrNull()
}

private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
    enumValues<T>().firstOrNull { it.name == value }
