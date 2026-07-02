package com.trailmate.app.services.tracking

import com.trailmate.app.core.database.TrackingSessionRecord
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
    private val recordingContext: TrackingRecordingContext? = null,
) : LocationProviderObserver {
    @Volatile
    private var currentState = TrackingLocationSessionState()
    private var subscription: LocationSubscription? = null
    @Volatile
    private var isActive = false
    private var isRecordingStarted = false

    val state: TrackingLocationSessionState
        get() = currentState

    fun start(): TrackingLocationSessionState {
        if (subscription?.isStopped == false) return currentState

        isActive = true
        currentState = TrackingLocationSessionState(
            status = TrackingLocationSessionStatus.Listening,
        )
        isRecordingStarted = false
        subscription = locationProvider.startLocationUpdates(
            request = request,
            observer = this,
        )
        return currentState
    }

    fun stop(markRecordingEnded: Boolean = true) {
        val activeSubscription = subscription
        val shouldEndRecording = isRecordingStarted && markRecordingEnded
        subscription = null
        isActive = false
        isRecordingStarted = false
        activeSubscription?.stop()
        if (shouldEndRecording) {
            recordingContext?.store?.markSessionEnded(
                sessionId = recordingContext.session.id,
                endedAt = recordingContext.clock(),
            )
        }
        currentState = TrackingLocationSessionState()
    }

    override fun onLocationSample(sample: LocationSample) {
        if (!isActive) return

        val previousState = currentState
        ensureRecordingStarted()
        val persistedPoint = recordingContext?.store?.appendSample(
            sessionId = recordingContext.session.id,
            sample = sample,
        )
        currentState = previousState.copy(
            status = TrackingLocationSessionStatus.Located,
            latestSample = sample,
            sampleCount = persistedPoint?.pointIndex?.plus(1) ?: (previousState.sampleCount + 1),
        )
    }

    override fun onProviderStatus(status: LocationProviderStatus) {
        if (!isActive) return

        if (status == LocationProviderStatus.Ready) {
            ensureRecordingStarted()
        }
        currentState = currentState.copy(
            status = status.toTrackingSessionStatus(),
        )
    }

    private fun ensureRecordingStarted() {
        if (isRecordingStarted) return

        val context = recordingContext ?: return
        context.store.upsertSession(
            TrackingSessionRecord.fromSession(context.session),
        )
        isRecordingStarted = true
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
