package com.trailmate.app.core.map

import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RoutePoint
import java.util.Locale

object AmapCameraRouteKeyFactory {
    fun build(route: ImportedRoute): String {
        val usablePoints = route.routePoints.filter { it.hasUsableCoordinate() }
        val geometryKey = if (usablePoints.size >= 2) {
            val first = usablePoints.first().cameraKeyCoordinate()
            val last = usablePoints.last().cameraKeyCoordinate()
            "$first>$last"
        } else {
            "no-geometry"
        }

        return "${route.fileName}|${route.routeName}|${route.distanceKm}|${route.ascentMeters}|${route.pointCount}|$geometryKey"
    }

    private fun RoutePoint.hasUsableCoordinate(): Boolean =
        latitude.isFinite() && longitude.isFinite()

    private fun RoutePoint.cameraKeyCoordinate(): String =
        String.format(Locale.US, "%.6f,%.6f", latitude, longitude)
}
