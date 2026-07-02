package com.trailmate.app.services.tracking

import android.content.Context
import androidx.core.content.ContextCompat

interface TrackingServiceLauncher {
    fun start()

    fun stop()
}

data class TrackingServiceLaunchCommand(
    val intentAction: String,
    val createsFakeLocationPoints: Boolean = false,
    val persistsTrack: Boolean = false,
) {
    companion object {
        fun start(): TrackingServiceLaunchCommand =
            TrackingServiceLaunchCommand(intentAction = TrackingServiceIntents.ActionStart)

        fun stop(): TrackingServiceLaunchCommand =
            TrackingServiceLaunchCommand(intentAction = TrackingServiceIntents.ActionStop)
    }
}

class AndroidTrackingServiceLauncher(
    context: Context,
) : TrackingServiceLauncher {
    private val appContext = context.applicationContext

    override fun start() {
        ContextCompat.startForegroundService(
            appContext,
            TrackingForegroundService.startIntent(appContext),
        )
    }

    override fun stop() {
        appContext.startService(TrackingForegroundService.stopIntent(appContext))
    }
}
