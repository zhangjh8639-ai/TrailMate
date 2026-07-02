package com.trailmate.app.platform.location

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.trailmate.app.core.location.LocationProviderObserver
import com.trailmate.app.core.location.LocationProviderRequest
import com.trailmate.app.core.location.LocationProviderStatus
import com.trailmate.app.core.location.LocationSubscription
import com.trailmate.app.core.location.StoppedLocationSubscription
import com.trailmate.app.core.location.SystemLocationReading
import com.trailmate.app.core.location.SystemLocationSampleMapper
import com.trailmate.app.core.location.TrailLocationProvider
import java.time.Instant

class AndroidLocationProvider(
    private val locationManager: LocationManager,
    private val clock: () -> Instant = { Instant.now() },
    private val looper: Looper = Looper.getMainLooper(),
) : TrailLocationProvider {
    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(
        request: LocationProviderRequest,
        observer: LocationProviderObserver,
    ): LocationSubscription {
        val providers = try {
            enabledProviders(request)
        } catch (_: SecurityException) {
            observer.onProviderStatus(LocationProviderStatus.PermissionDenied)
            return StoppedLocationSubscription
        }
        if (providers.isEmpty()) {
            observer.onProviderStatus(LocationProviderStatus.Disabled)
            return StoppedLocationSubscription
        }

        val state = AndroidLocationProviderState(providers.toSet())
        val listener = MappingLocationListener(
            observer = observer,
            clock = clock,
            state = state,
        )

        return try {
            providers.forEach { provider ->
                locationManager.requestLocationUpdates(
                    provider,
                    request.minTimeInterval.toMillis(),
                    request.minDistance.meters.toFloat(),
                    listener,
                    looper,
                )
            }
            observer.onProviderStatus(LocationProviderStatus.Ready)
            AndroidLocationSubscription(locationManager, listener, state)
        } catch (_: SecurityException) {
            state.stop()
            runCatching { locationManager.removeUpdates(listener) }
            observer.onProviderStatus(LocationProviderStatus.PermissionDenied)
            StoppedLocationSubscription
        }
    }

    private fun enabledProviders(request: LocationProviderRequest): List<String> {
        val orderedProviders = if (request.preferGps) {
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        } else {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        }

        return orderedProviders.filter { provider ->
            locationManager.isProviderEnabled(provider)
        }
    }
}

private class MappingLocationListener(
    private val observer: LocationProviderObserver,
    private val clock: () -> Instant,
    private val state: AndroidLocationProviderState,
) : LocationListener {
    override fun onLocationChanged(location: Location) {
        if (!state.shouldForwardLocation()) return

        val reading = location.toSystemLocationReading(clock())
        val sample = runCatching { SystemLocationSampleMapper.map(reading) }
            .getOrElse {
                observer.onProviderStatus(LocationProviderStatus.InvalidReading)
                return
            }

        observer.onLocationSample(sample)
    }

    override fun onProviderDisabled(provider: String) {
        state.markProviderDisabled(provider, observer)
    }

    override fun onProviderEnabled(provider: String) {
        state.markProviderEnabled(provider, observer)
    }
}

private class AndroidLocationSubscription(
    private val locationManager: LocationManager,
    private val listener: LocationListener,
    private val state: AndroidLocationProviderState,
) : LocationSubscription {
    override val isStopped: Boolean
        get() = state.isStopped

    override fun stop() {
        if (state.isStopped) return

        state.stop()
        runCatching { locationManager.removeUpdates(listener) }
    }
}

private fun Location.toSystemLocationReading(
    fallbackRecordedAt: Instant,
): SystemLocationReading =
    SystemLocationReading(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy.toDouble() else Double.NaN,
        recordedAt = if (time > 0L) Instant.ofEpochMilli(time) else fallbackRecordedAt,
        altitudeMeters = if (hasAltitude()) altitude else null,
        bearingDegrees = if (hasBearing()) bearing.toDouble() else null,
        speedMetersPerSecond = if (hasSpeed()) speed.toDouble() else null,
    )
