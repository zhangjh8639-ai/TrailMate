package com.trailmate.app.core.geo

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.GeoCoordinate
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal object GeoMath {
    private const val EarthRadiusMeters = 6_371_008.8

    fun haversineDistance(from: GeoCoordinate, to: GeoCoordinate): Distance {
        val lat1 = from.latitude.toRadians()
        val lat2 = to.latitude.toRadians()
        val deltaLat = (to.latitude - from.latitude).toRadians()
        val deltaLon = longitudeDeltaDegrees(from.longitude, to.longitude).toRadians()

        val a = sin(deltaLat / 2.0).pow(2.0) +
            cos(lat1) * cos(lat2) * sin(deltaLon / 2.0).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))

        return Distance.meters(EarthRadiusMeters * c)
    }

    fun initialBearingDegrees(from: GeoCoordinate, to: GeoCoordinate): Double {
        val lat1 = from.latitude.toRadians()
        val lat2 = to.latitude.toRadians()
        val deltaLon = longitudeDeltaDegrees(from.longitude, to.longitude).toRadians()

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
            sin(lat1) * cos(lat2) * cos(deltaLon)

        return normalizeDegrees(atan2(y, x).toDegrees())
    }

    fun interpolate(from: GeoCoordinate, to: GeoCoordinate, ratio: Double): GeoCoordinate {
        val clamped = ratio.coerceIn(0.0, 1.0)
        val longitude = normalizeLongitude(
            from.longitude + longitudeDeltaDegrees(from.longitude, to.longitude) * clamped,
        )

        return GeoCoordinate(
            latitude = from.latitude + (to.latitude - from.latitude) * clamped,
            longitude = longitude,
            elevation = null,
        )
    }

    fun toLocalMeters(coordinate: GeoCoordinate, origin: GeoCoordinate): LocalPoint {
        val latMeters = (coordinate.latitude - origin.latitude).toRadians() * EarthRadiusMeters
        val lonMeters = longitudeDeltaDegrees(origin.longitude, coordinate.longitude).toRadians() *
            EarthRadiusMeters *
            cos(origin.latitude.toRadians())
        return LocalPoint(x = lonMeters, y = latMeters)
    }

    private fun longitudeDeltaDegrees(from: Double, to: Double): Double {
        var delta = to - from
        while (delta > 180.0) delta -= 360.0
        while (delta < -180.0) delta += 360.0
        return delta
    }

    private fun normalizeLongitude(value: Double): Double {
        var longitude = value
        while (longitude > 180.0) longitude -= 360.0
        while (longitude < -180.0) longitude += 360.0
        return longitude
    }

    private fun normalizeDegrees(value: Double): Double =
        ((value % 360.0) + 360.0) % 360.0

    private fun Double.toRadians(): Double = this / 180.0 * PI

    private fun Double.toDegrees(): Double = this / PI * 180.0
}

internal data class LocalPoint(
    val x: Double,
    val y: Double,
)
