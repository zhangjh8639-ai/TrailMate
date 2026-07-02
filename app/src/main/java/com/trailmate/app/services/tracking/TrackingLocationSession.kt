package com.trailmate.app.services.tracking

import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.location.LocationProviderObserver
import com.trailmate.app.core.location.LocationProviderRequest
import com.trailmate.app.core.location.LocationProviderStatus
import com.trailmate.app.core.location.LocationSubscription
import com.trailmate.app.core.location.TrailLocationProvider
import com.trailmate.app.core.model.Distance
import java.time.Duration

enum class TrackingLocationSessionStatus {
    Idle,
    Listening,
    Located,
    PermissionDenied,
    Disabled,
    InvalidReading,
}

data class TrackingLocationSessionState(
    val status: TrackingLocationSessionStatus = TrackingLocationSessionStatus.Idle,
    val latestSample: LocationSample? = null,
    val sampleCount: Int = 0,
)

class TrackingLocationSession(
    private val locationProvider: TrailLocationProvider,
    private val request: LocationProviderRequest = activeTrackingLocationRequest(),
) : LocationProviderObserver {
    @Volatile
    private var currentState = TrackingLocationSessionState()
    private var subscription: LocationSubscription? = null
    @Volatile
    private var isActive = false

    val state: TrackingLocationSessionState
        get() = currentState

    fun start(): TrackingLocationSessionState {
        if (subscription?.isStopped == false) return currentState

        isActive = true
        currentState = TrackingLocationSessionState(
            status = TrackingLocationSessionStatus.Listening,
        )
        subscription = locationProvider.startLocationUpdates(
            request = request,
            observer = this,
        )
        return currentState
    }

    fun stop() {
        val activeSubscription = subscription
        subscription = null
        isActive = false
        activeSubscription?.stop()
        currentState = TrackingLocationSessionState()
    }

    override fun onLocationSample(sample: LocationSample) {
        if (!isActive) return

        val previousState = currentState
        currentState = previousState.copy(
            status = TrackingLocationSessionStatus.Located,
            latestSample = sample,
            sampleCount = previousState.sampleCount + 1,
        )
    }

    override fun onProviderStatus(status: LocationProviderStatus) {
        if (!isActive) return

        currentState = currentState.copy(
            status = status.toTrackingSessionStatus(),
        )
    }
}

fun activeTrackingLocationRequest(): LocationProviderRequest =
    LocationProviderRequest(
        minTimeInterval = Duration.ofSeconds(1),
        minDistance = Distance.ZERO,
        preferGps = true,
    )

fun TrackingLocationSessionState.requiresTrackingServiceShutdownAfterStart(): Boolean =
    status == TrackingLocationSessionStatus.PermissionDenied ||
        status == TrackingLocationSessionStatus.Disabled

private fun LocationProviderStatus.toTrackingSessionStatus(): TrackingLocationSessionStatus =
    when (this) {
        LocationProviderStatus.Ready -> TrackingLocationSessionStatus.Listening
        LocationProviderStatus.PermissionDenied -> TrackingLocationSessionStatus.PermissionDenied
        LocationProviderStatus.Disabled -> TrackingLocationSessionStatus.Disabled
        LocationProviderStatus.InvalidReading -> TrackingLocationSessionStatus.InvalidReading
    }
