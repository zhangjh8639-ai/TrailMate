package com.trailmate.app.core.location

import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import java.time.Duration
import java.time.Instant

data class LocationProviderRequest(
    val minTimeInterval: Duration = Duration.ofSeconds(1),
    val minDistance: Distance = Distance.ZERO,
    val preferGps: Boolean = true,
) {
    init {
        require(!minTimeInterval.isNegative) {
            "Minimum location interval must be non-negative."
        }
        require(minDistance.meters.isFinite()) {
            "Minimum location distance must be finite."
        }
    }
}

enum class LocationProviderStatus {
    Ready,
    PermissionDenied,
    Disabled,
    InvalidReading,
}

interface LocationProviderObserver {
    fun onLocationSample(sample: LocationSample)

    fun onProviderStatus(status: LocationProviderStatus) = Unit
}

interface LocationSubscription {
    val isStopped: Boolean

    fun stop()
}

object StoppedLocationSubscription : LocationSubscription {
    override val isStopped: Boolean = true

    override fun stop() = Unit
}

interface TrailLocationProvider {
    fun startLocationUpdates(
        request: LocationProviderRequest,
        observer: LocationProviderObserver,
    ): LocationSubscription
}

data class SystemLocationReading(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double,
    val recordedAt: Instant,
    val altitudeMeters: Double? = null,
    val bearingDegrees: Double? = null,
    val speedMetersPerSecond: Double? = null,
)

object SystemLocationSampleMapper {
    fun map(reading: SystemLocationReading): LocationSample {
        require(reading.latitude.isFinite()) { "Latitude must be finite." }
        require(reading.longitude.isFinite()) { "Longitude must be finite." }
        require(reading.accuracyMeters.isFinite() && reading.accuracyMeters >= 0.0) {
            "Location accuracy must be finite and non-negative."
        }
        val altitude = reading.altitudeMeters?.also {
            require(it.isFinite()) { "Altitude must be finite." }
        }
        val bearing = reading.bearingDegrees?.also {
            require(it.isFinite()) { "Bearing must be finite." }
        }?.let { normalizeBearing(it) }
        val speed = reading.speedMetersPerSecond?.also {
            require(it.isFinite() && it >= 0.0) { "Speed must be finite and non-negative." }
        }

        return LocationSample(
            coordinate = GeoCoordinate(
                latitude = reading.latitude,
                longitude = reading.longitude,
                elevation = altitude?.let { Elevation.meters(it) },
            ),
            accuracy = GpsAccuracy(reading.accuracyMeters),
            recordedAt = reading.recordedAt,
            bearingDegrees = bearing,
            speedMetersPerSecond = speed,
        )
    }

    private fun normalizeBearing(value: Double): Double =
        ((value % 360.0) + 360.0) % 360.0
}
