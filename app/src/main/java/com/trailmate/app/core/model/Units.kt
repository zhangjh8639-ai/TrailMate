package com.trailmate.app.core.model

import java.time.Instant

@JvmInline
value class RouteId(val value: String) {
    init {
        require(value.isNotBlank()) { "Route id must not be blank." }
    }
}

data class Distance(val meters: Double) {
    init {
        require(meters >= 0.0) { "Distance must be non-negative." }
    }

    companion object {
        val ZERO = Distance(0.0)

        fun meters(value: Double): Distance = Distance(value)
    }
}

data class Elevation(val meters: Double) {
    companion object {
        fun meters(value: Double): Elevation = Elevation(value)
    }
}

data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
    val elevation: Elevation? = null,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180." }
    }
}

data class BatteryLevel(val percent: Int) {
    init {
        require(percent in 0..100) { "Battery percent must be between 0 and 100." }
    }
}

data class GpsAccuracy(val meters: Double) {
    init {
        require(meters >= 0.0) { "GPS accuracy must be non-negative." }
    }
}
