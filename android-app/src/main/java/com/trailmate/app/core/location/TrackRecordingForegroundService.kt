package com.trailmate.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import com.trailmate.app.MainActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.RouteDeviationAlertState
import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TrackRecordingRouteIdentityPolicy
import com.trailmate.app.core.model.TrackRecordingRouteMonitorEngine
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus
import com.trailmate.app.core.model.offlineRoutePackKey
import com.trailmate.app.core.persistence.LocalTrailMateSessionRepository
import com.trailmate.app.core.persistence.SharedPreferencesTrailMateSessionStore
import java.util.Locale

class TrackRecordingForegroundService : Service() {
    private val repository by lazy {
        LocalTrailMateSessionRepository(SharedPreferencesTrailMateSessionStore(this))
    }
    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private var activeListener: LocationListener? = null
    private var routeDeviationAlertState = RouteDeviationAlertState()
    private var monitoredRoute: ImportedRoute? = null
    private var monitoredRouteKey: String? = null

    private data class LocationUpdatesStartResult(
        val provider: String?,
        val failureCaption: String?
    ) {
        companion object {
            fun started(provider: String): LocationUpdatesStartResult =
                LocationUpdatesStartResult(provider = provider, failureCaption = null)

            fun failed(caption: String): LocationUpdatesStartResult =
                LocationUpdatesStartResult(provider = null, failureCaption = caption)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val snapshot = repository.loadSnapshot()
        val current = snapshot.latestTrackRecording
        when (action) {
            ACTION_START -> startOrKeepRecording(
                routeName = intent?.getStringExtra(EXTRA_ROUTE_NAME).orEmpty(),
                routeKey = intent?.getStringExtra(EXTRA_ROUTE_KEY),
                importedRoute = snapshot.importedRoute,
                current = current
            )
            ACTION_PAUSE -> pauseRecording(current)
            ACTION_RESUME -> resumeRecording(current, snapshot.importedRoute)
            ACTION_FINISH -> finishRecording(current)
            else -> startOrKeepRecording(
                routeName = current.routeName.orEmpty(),
                routeKey = current.routeKey,
                importedRoute = snapshot.importedRoute,
                current = current
            )
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun startOrKeepRecording(
        routeName: String,
        routeKey: String?,
        importedRoute: ImportedRoute?,
        current: TrackRecordingState
    ) {
        val decision = TrackRecordingServiceStartPolicy.resolve(
            requestedRouteName = routeName,
            current = current,
            hasPreciseLocationPermission = hasForegroundLocationPermission(),
            hasEnabledProvider = enabledProvider() != null,
            nowEpochMillis = System.currentTimeMillis(),
            requestedRouteKey = routeKey
        )

        when (decision.action) {
            TrackRecordingServiceStartAction.STOP_SELF -> {
                resetRouteDeviationMonitor()
                stopSelf()
            }

            TrackRecordingServiceStartAction.PUBLISH_ONLY -> {
                stopLocationUpdates()
                resetRouteDeviationMonitor()
                savePublishAndNotify(
                    trackRecording = decision.trackRecording,
                    caption = decision.notificationCaption,
                    useLocationForegroundType = false
                )
                if (decision.shouldStopSelf) {
                    stopSelf()
                }
            }

            TrackRecordingServiceStartAction.PUBLISH_AND_START_UPDATES -> {
                val foregroundStarted = updateNotification(
                    trackRecording = decision.trackRecording,
                    caption = decision.notificationCaption,
                    useLocationForegroundType = true
                )
                if (!foregroundStarted) {
                    publishRuntimeBlock(current, "定位服务暂不可用")
                    return
                }

                val updatesStart = startLocationUpdates()
                val provider = updatesStart.provider
                if (provider == null) {
                    publishRuntimeBlock(
                        currentBeforeStart = current,
                        caption = updatesStart.failureCaption ?: "定位服务暂不可用"
                    )
                    return
                }

                captureMonitoredRoute(importedRoute, decision.trackRecording)
                repository.saveTrackRecording(decision.trackRecording)
                publishRecording(decision.trackRecording)
                appendLastKnownLocation(provider)
            }
        }
    }

    private fun publishRuntimeBlock(currentBeforeStart: TrackRecordingState, caption: String) {
        val decision = TrackRecordingServiceStartPolicy.resolveRuntimeBlock(
            currentBeforeStart = currentBeforeStart,
            notificationCaption = caption,
            nowEpochMillis = System.currentTimeMillis()
        )
        stopLocationUpdates()
        resetRouteDeviationMonitor()
        savePublishAndNotify(
            trackRecording = decision.trackRecording,
            caption = decision.notificationCaption,
            useLocationForegroundType = false
        )
        if (decision.shouldStopSelf) {
            stopLocationUpdates()
            stopSelf()
        }
    }

    private fun pauseRecording(current: TrackRecordingState) {
        val updated = TrackRecordingEngine.pause(
            state = current,
            nowEpochMillis = System.currentTimeMillis()
        )
        stopLocationUpdates()
        resetRouteDeviationMonitor()
        savePublishAndNotify(
            trackRecording = updated,
            caption = "轨迹记录已暂停",
            useLocationForegroundType = false
        )
    }

    private fun resumeRecording(current: TrackRecordingState, importedRoute: ImportedRoute?) {
        startOrKeepRecording(
            routeName = current.routeName.orEmpty(),
            routeKey = current.routeKey,
            importedRoute = importedRoute,
            current = current
        )
    }

    private fun finishRecording(current: TrackRecordingState) {
        val updated = TrackRecordingEngine.finish(
            state = current,
            nowEpochMillis = System.currentTimeMillis()
        )
        stopLocationUpdates()
        resetRouteDeviationMonitor()
        repository.saveTrackRecording(updated)
        publishRecording(updated)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(): LocationUpdatesStartResult {
        stopLocationUpdates()
        if (!hasForegroundLocationPermission()) {
            return LocationUpdatesStartResult.failed("需要精确定位权限后才能记录轨迹")
        }

        val provider = enabledProvider()
        if (provider == null) {
            return LocationUpdatesStartResult.failed("系统定位未开启，无法记录轨迹")
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                appendLocation(location)
            }

            @Deprecated("Deprecated in Android framework")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) {
                val current = repository.loadSnapshot().latestTrackRecording
                publishRuntimeBlock(current, "系统定位未开启，无法记录轨迹")
            }
        }
        activeListener = listener
        return runCatching {
            locationManager.requestLocationUpdates(
                provider,
                MIN_TIME_MILLIS,
                MIN_DISTANCE_METERS,
                listener,
                Looper.getMainLooper()
            )
            LocationUpdatesStartResult.started(provider)
        }.getOrElse {
            stopLocationUpdates()
            LocationUpdatesStartResult.failed("定位服务暂不可用")
        }
    }

    @SuppressLint("MissingPermission")
    private fun appendLastKnownLocation(provider: String) {
        if (!hasForegroundLocationPermission()) {
            publishRuntimeBlock(
                currentBeforeStart = repository.loadSnapshot().latestTrackRecording,
                caption = "需要精确定位权限后才能记录轨迹"
            )
            return
        }

        runCatching {
            locationManager.getLastKnownLocation(provider)?.let(::appendLocation)
        }.onFailure {
            publishRuntimeBlock(
                currentBeforeStart = repository.loadSnapshot().latestTrackRecording,
                caption = "定位服务暂不可用"
            )
        }
    }

    private fun stopLocationUpdates() {
        activeListener?.let(locationManager::removeUpdates)
        activeListener = null
    }

    private fun appendLocation(location: Location) {
        val snapshot = repository.loadSnapshot()
        val current = snapshot.latestTrackRecording
        val point = location.toRecordedTrackPoint()
        val updated = TrackRecordingEngine.appendLocation(
            state = current,
            point = point
        )
        if (updated != current) {
            deliverRouteDeviationAlertFromService(
                route = monitoredRoute?.takeIf { route -> route.readyForMonitoring(updated) }
                    ?: snapshot.importedRoute?.takeIf { route -> route.readyForMonitoring(updated) },
                trackRecording = updated,
                point = point
            )
            savePublishAndNotify(updated, updated.notificationCaption())
        }
    }

    private fun deliverRouteDeviationAlertFromService(
        route: ImportedRoute?,
        trackRecording: TrackRecordingState,
        point: RecordedTrackPoint
    ) {
        val nextRouteKey = route?.takeIf { candidate ->
            candidate.readyForMonitoring(trackRecording)
        }?.offlineRoutePackKey()
        if (nextRouteKey != monitoredRouteKey) {
            routeDeviationAlertState = RouteDeviationAlertState()
            monitoredRouteKey = nextRouteKey
        }

        val decision = TrackRecordingRouteMonitorEngine.evaluate(
            route = route,
            recordingRouteName = trackRecording.routeName,
            recordingRouteKey = trackRecording.routeKey,
            point = point,
            state = routeDeviationAlertState,
            nowEpochMillis = System.currentTimeMillis()
        )
        routeDeviationAlertState = decision.nextState
        RouteDeviationAlertAndroidDelivery.deliver(
            context = this,
            decision = decision,
            notificationPermissionGranted = hasRouteAlertNotificationPermission()
        )
    }

    private fun resetRouteDeviationMonitor() {
        routeDeviationAlertState = RouteDeviationAlertState()
        monitoredRoute = null
        monitoredRouteKey = null
    }

    private fun captureMonitoredRoute(route: ImportedRoute?, trackRecording: TrackRecordingState) {
        val nextRoute = monitoredRoute?.takeIf { currentRoute -> currentRoute.readyForMonitoring(trackRecording) }
            ?: route?.takeIf { candidate -> candidate.readyForMonitoring(trackRecording) }
        val nextRouteKey = nextRoute?.offlineRoutePackKey()
        if (nextRouteKey != monitoredRouteKey) {
            routeDeviationAlertState = RouteDeviationAlertState()
        }
        monitoredRoute = nextRoute
        monitoredRouteKey = nextRouteKey
    }

    private fun ImportedRoute.readyForMonitoring(trackRecording: TrackRecordingState): Boolean {
        if (routePoints.size < 2) {
            return false
        }

        return TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
            trackRecording = trackRecording,
            routeName = routeName,
            routeKey = offlineRoutePackKey()
        )
    }

    private fun savePublishAndNotify(
        trackRecording: TrackRecordingState,
        caption: String,
        useLocationForegroundType: Boolean = trackRecording.status == TrackRecordingStatus.RECORDING &&
            hasForegroundLocationPermission()
    ) {
        repository.saveTrackRecording(trackRecording)
        publishRecording(trackRecording)
        updateNotification(trackRecording, caption, useLocationForegroundType)
    }

    private fun updateNotification(
        trackRecording: TrackRecordingState,
        caption: String,
        useLocationForegroundType: Boolean
    ): Boolean {
        ensureNotificationChannel()
        val notification = buildNotification(trackRecording, caption)
        if (useLocationForegroundType) {
            return runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                true
            }.getOrElse {
                notifyStatus(notification)
                false
            }
        } else {
            notifyStatus(notification)
            return true
        }
    }

    private fun notifyStatus(notification: Notification) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        trackRecording: TrackRecordingState,
        caption: String
    ): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java),
            pendingIntentFlags()
        )
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(trackRecording.notificationTitle())
            .setContentText(caption)
            .setContentIntent(openIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(trackRecording.status == TrackRecordingStatus.RECORDING)

        if (trackRecording.status == TrackRecordingStatus.RECORDING) {
            builder.addAction(
                notificationAction(
                    android.R.drawable.ic_media_pause,
                    "暂停",
                    ACTION_PAUSE,
                    REQUEST_PAUSE
                )
            )
        }
        if (trackRecording.status == TrackRecordingStatus.PAUSED) {
            builder.addAction(
                notificationAction(
                    android.R.drawable.ic_media_play,
                    "继续",
                    ACTION_RESUME,
                    REQUEST_RESUME
                )
            )
        }
        if (trackRecording.status == TrackRecordingStatus.RECORDING ||
            trackRecording.status == TrackRecordingStatus.PAUSED
        ) {
            builder.addAction(
                notificationAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "结束",
                    ACTION_FINISH,
                    REQUEST_FINISH
                )
            )
        }

        return builder.build()
    }

    private fun notificationAction(
        iconResId: Int,
        title: String,
        action: String,
        requestCode: Int
    ): Notification.Action =
        Notification.Action.Builder(
            Icon.createWithResource(this, iconResId),
            title,
            serviceActionIntent(action, requestCode)
        ).build()

    private fun serviceActionIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, TrackRecordingForegroundService::class.java).setAction(action),
            pendingIntentFlags()
        )

    private fun publishRecording(trackRecording: TrackRecordingState) {
        val payload = TrackRecordingBroadcastCodec.encode(trackRecording)
        sendBroadcast(
            Intent(ACTION_RECORDING_CHANGED)
                .setPackage(packageName)
                .putExtra(EXTRA_RECORDING_PAYLOAD, payload)
        )
    }

    private fun enabledProvider(): String? =
        TrailMateLocationProviderPolicy.resolve(
            hasFineLocationPermission = hasFineLocationPermission(),
            hasCoarseLocationPermission = hasCoarseLocationPermission(),
            gpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER),
            networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        )?.toAndroidProviderName()

    private fun hasForegroundLocationPermission(): Boolean =
        hasFineLocationPermission()

    private fun hasRouteAlertNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun hasFineLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun ensureNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "轨迹记录",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun Location.toRecordedTrackPoint(): RecordedTrackPoint =
        RecordedTrackPoint(
            latitude = latitude,
            longitude = longitude,
            elevationMeters = if (hasAltitude()) altitude else null,
            horizontalAccuracyMeters = if (hasAccuracy()) accuracy.toDouble() else Double.POSITIVE_INFINITY,
            timestampEpochMillis = time.takeIf { it > 0L } ?: System.currentTimeMillis()
        )

    private fun TrackRecordingState.notificationCaption(): String {
        val route = routeName ?: "当前路线"
        val distance = String.format(Locale.US, "%.1f", totalDistanceKm)
        return "$route · 已记录 ${distance} km · $pointCount 个点"
    }

    private fun TrackRecordingState.notificationTitle(): String =
        when (status) {
            TrackRecordingStatus.RECORDING -> "TrailMate 正在记录轨迹"
            TrackRecordingStatus.PAUSED -> "TrailMate 轨迹已暂停"
            TrackRecordingStatus.FINISHED -> "TrailMate 轨迹已完成"
            TrackRecordingStatus.IDLE -> "TrailMate 未开始记录"
        }

    companion object {
        const val ACTION_RECORDING_CHANGED = "com.trailmate.app.action.TRACK_RECORDING_CHANGED"
        const val EXTRA_RECORDING_PAYLOAD = "com.trailmate.app.extra.TRACK_RECORDING_PAYLOAD"

        private const val ACTION_START = "com.trailmate.app.action.START_TRACK_RECORDING"
        private const val ACTION_PAUSE = "com.trailmate.app.action.PAUSE_TRACK_RECORDING"
        private const val ACTION_RESUME = "com.trailmate.app.action.RESUME_TRACK_RECORDING"
        private const val ACTION_FINISH = "com.trailmate.app.action.FINISH_TRACK_RECORDING"
        private const val EXTRA_ROUTE_NAME = "com.trailmate.app.extra.ROUTE_NAME"
        private const val EXTRA_ROUTE_KEY = "com.trailmate.app.extra.ROUTE_KEY"
        private const val CHANNEL_ID = "trailmate_track_recording"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_OPEN_APP = 2001
        private const val REQUEST_PAUSE = 2002
        private const val REQUEST_RESUME = 2003
        private const val REQUEST_FINISH = 2004
        private const val MIN_TIME_MILLIS = 3_000L
        private const val MIN_DISTANCE_METERS = 5f

        fun startRecording(context: Context, routeName: String, routeKey: String? = null) {
            val intent = Intent(context, TrackRecordingForegroundService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_ROUTE_NAME, routeName)
                .apply {
                    routeKey?.takeIf { it.isNotBlank() }?.let { key -> putExtra(EXTRA_ROUTE_KEY, key) }
                }
            when (context.trackRecordingServiceLaunchMode()) {
                TrackRecordingServiceLaunchMode.FOREGROUND_LOCATION_SERVICE ->
                    context.startForegroundTrackService(intent)
                TrackRecordingServiceLaunchMode.SHORT_SERVICE ->
                    context.startService(intent)
            }
        }

        fun pauseRecording(context: Context) {
            context.startService(
                Intent(context, TrackRecordingForegroundService::class.java)
                    .setAction(ACTION_PAUSE)
            )
        }

        fun resumeRecording(context: Context) {
            context.startService(
                Intent(context, TrackRecordingForegroundService::class.java)
                    .setAction(ACTION_RESUME)
            )
        }

        fun finishRecording(context: Context) {
            context.startService(
                Intent(context, TrackRecordingForegroundService::class.java)
                    .setAction(ACTION_FINISH)
            )
        }

        fun trackRecordingFrom(intent: Intent?): TrackRecordingState? =
            TrackRecordingBroadcastCodec.decode(
                intent?.getStringExtra(EXTRA_RECORDING_PAYLOAD)
            )

        private fun Context.startForegroundTrackService(intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        private fun Context.trackRecordingServiceLaunchMode(): TrackRecordingServiceLaunchMode =
            TrackRecordingServiceLaunchPolicy.resolve(
                hasPreciseLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED,
                gpsProviderEnabled = runCatching {
                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                }.getOrDefault(false)
            )

        private fun pendingIntentFlags(): Int =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
