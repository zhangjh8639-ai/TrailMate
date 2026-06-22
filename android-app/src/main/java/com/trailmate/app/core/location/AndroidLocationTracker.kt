package com.trailmate.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper

enum class TrailMateLocationStatus {
    DISABLED,
    PERMISSION_REQUIRED,
    SEARCHING,
    LOCATED,
    LOW_ACCURACY,
    PROVIDER_DISABLED,
    UNAVAILABLE
}

data class TrailMateLocationSnapshot(
    val status: TrailMateLocationStatus,
    val latitude: Double?,
    val longitude: Double?,
    val elevationMeters: Double?,
    val horizontalAccuracyMeters: Double?,
    val timestampEpochMillis: Long
) {
    companion object {
        fun disabled(): TrailMateLocationSnapshot =
            empty(TrailMateLocationStatus.DISABLED)

        fun permissionRequired(): TrailMateLocationSnapshot =
            empty(TrailMateLocationStatus.PERMISSION_REQUIRED)

        fun searching(): TrailMateLocationSnapshot =
            empty(TrailMateLocationStatus.SEARCHING)

        fun providerDisabled(): TrailMateLocationSnapshot =
            empty(TrailMateLocationStatus.PROVIDER_DISABLED)

        fun unavailable(): TrailMateLocationSnapshot =
            empty(TrailMateLocationStatus.UNAVAILABLE)

        private fun empty(status: TrailMateLocationStatus): TrailMateLocationSnapshot =
            TrailMateLocationSnapshot(
                status = status,
                latitude = null,
                longitude = null,
                elevationMeters = null,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = System.currentTimeMillis()
            )
    }
}

class AndroidLocationTracker(context: Context) {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var activeListener: LocationListener? = null

    fun hasForegroundPermission(): Boolean =
        hasFineLocationPermission() || hasCoarseLocationPermission()

    fun hasPrecisePermission(): Boolean =
        hasFineLocationPermission()

    fun hasEnabledProvider(): Boolean =
        enabledProvider() != null

    @SuppressLint("MissingPermission")
    fun start(onSnapshot: (TrailMateLocationSnapshot) -> Unit) {
        stop()
        if (!hasPrecisePermission()) {
            onSnapshot(TrailMateLocationSnapshot.permissionRequired())
            return
        }

        val provider = enabledProvider()
        if (provider == null) {
            onSnapshot(TrailMateLocationSnapshot.providerDisabled())
            return
        }

        onSnapshot(TrailMateLocationSnapshot.searching())
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onSnapshot(location.toTrailMateSnapshot())
            }

            @Deprecated("Deprecated in Android framework")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) {
                onSnapshot(TrailMateLocationSnapshot.providerDisabled())
            }
        }
        activeListener = listener
        runCatching {
            locationManager.requestLocationUpdates(
                provider,
                MIN_TIME_MILLIS,
                MIN_DISTANCE_METERS,
                listener,
                Looper.getMainLooper()
            )
            locationManager.getLastKnownLocation(provider)?.let { location ->
                onSnapshot(location.toTrailMateSnapshot())
            }
        }.onFailure {
            onSnapshot(TrailMateLocationSnapshot.unavailable())
        }
    }

    fun stop() {
        activeListener?.let(locationManager::removeUpdates)
        activeListener = null
    }

    private fun enabledProvider(): String? =
        TrailMateLocationProviderPolicy.resolve(
            hasFineLocationPermission = hasFineLocationPermission(),
            hasCoarseLocationPermission = hasCoarseLocationPermission(),
            gpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER),
            networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        )?.toAndroidProviderName()

    private fun hasFineLocationPermission(): Boolean =
        appContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocationPermission(): Boolean =
        appContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun Location.toTrailMateSnapshot(): TrailMateLocationSnapshot {
        val accuracy = if (hasAccuracy()) accuracy.toDouble() else null
        return TrailMateLocationSnapshot(
            status = if (accuracy != null && accuracy > LOW_ACCURACY_METERS) {
                TrailMateLocationStatus.LOW_ACCURACY
            } else {
                TrailMateLocationStatus.LOCATED
            },
            latitude = latitude,
            longitude = longitude,
            elevationMeters = if (hasAltitude()) altitude else null,
            horizontalAccuracyMeters = accuracy,
            timestampEpochMillis = time.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
    }

    private companion object {
        const val MIN_TIME_MILLIS = 3_000L
        const val MIN_DISTANCE_METERS = 5f
        const val LOW_ACCURACY_METERS = 50.0
    }
}
