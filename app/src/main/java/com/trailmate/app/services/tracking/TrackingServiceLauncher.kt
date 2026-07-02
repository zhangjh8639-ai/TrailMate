package com.trailmate.app.services.tracking

import android.content.Context
import androidx.core.content.ContextCompat
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.RouteId

interface TrackingServiceLauncher {
    fun start(request: TrackingServiceStartRequest)

    fun stop()
}

data class TrackingServiceLaunchCommand(
    val intentAction: String,
    val sessionId: NavigationSessionId? = null,
    val routeId: RouteId? = null,
    val startedAtEpochMillis: Long? = null,
    val direction: NavigationDirection? = null,
    val createsFakeLocationPoints: Boolean = false,
    val persistsTrack: Boolean = false,
) {
    companion object {
        fun start(request: TrackingServiceStartRequest): TrackingServiceLaunchCommand =
            TrackingServiceLaunchCommand(
                intentAction = TrackingServiceIntents.ActionStart,
                sessionId = request.sessionId,
                routeId = request.routeId,
                startedAtEpochMillis = request.startedAtEpochMillis,
                direction = request.direction,
                persistsTrack = true,
            )

        fun stop(): TrackingServiceLaunchCommand =
            TrackingServiceLaunchCommand(intentAction = TrackingServiceIntents.ActionStop)
    }
}

class AndroidTrackingServiceLauncher(
    context: Context,
) : TrackingServiceLauncher {
    private val appContext = context.applicationContext

    override fun start(request: TrackingServiceStartRequest) {
        ContextCompat.startForegroundService(
            appContext,
            TrackingForegroundService.startIntent(appContext, request),
        )
    }

    override fun stop() {
        appContext.startService(TrackingForegroundService.stopIntent(appContext))
    }
}
