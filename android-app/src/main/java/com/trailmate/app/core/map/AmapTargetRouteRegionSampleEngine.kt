package com.trailmate.app.core.map

import com.trailmate.app.core.model.RoutePoint

object AmapTargetRouteRegionSampleEngine {
    fun representativePoint(routePoints: List<RoutePoint>): RoutePoint? {
        val usablePoints = routePoints.filter { point ->
            point.latitude in MIN_LATITUDE..MAX_LATITUDE &&
                point.longitude in MIN_LONGITUDE..MAX_LONGITUDE
        }

        return usablePoints.getOrNull(usablePoints.size / 2)
    }

    private const val MIN_LATITUDE = -90.0
    private const val MAX_LATITUDE = 90.0
    private const val MIN_LONGITUDE = -180.0
    private const val MAX_LONGITUDE = 180.0
}
