package com.trailmate.app.core.map

import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanCheckpointType
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMapCheckpointProjectorTest {
    @Test
    fun projectsPlanCheckpointsOntoRouteGeometryByDistance() {
        val route = ImportedRoute(
            routeName = "测试路线",
            fileName = "test.gpx",
            distanceKm = 10.0,
            ascentMeters = 400,
            status = RouteImportStatus.PARSED,
            pointCount = 3,
            routePoints = listOf(
                RoutePoint(latitude = 30.0, longitude = 120.0, elevationMeters = null, distanceAlongRouteKm = 0.0),
                RoutePoint(latitude = 30.0, longitude = 121.0, elevationMeters = null, distanceAlongRouteKm = 5.0),
                RoutePoint(latitude = 31.0, longitude = 121.0, elevationMeters = null, distanceAlongRouteKm = 10.0)
            )
        )
        val plan = HikePlanSummary(
            checkpoints = listOf(
                HikePlanCheckpoint(
                    type = HikePlanCheckpointType.START,
                    title = "起点",
                    distanceKm = 0.0,
                    timeFromStart = "0:00",
                    note = "出发"
                ),
                HikePlanCheckpoint(
                    type = HikePlanCheckpointType.ENERGY_CHECK,
                    title = "补给检查",
                    distanceKm = 2.5,
                    timeFromStart = "1:30",
                    note = "吃东西"
                ),
                HikePlanCheckpoint(
                    type = HikePlanCheckpointType.REST_CHECK,
                    title = "休息判断",
                    distanceKm = 7.5,
                    timeFromStart = "3:00",
                    note = "短休"
                )
            )
        )

        val markers = TrailMapCheckpointProjector.project(route = route, plan = plan)

        assertEquals(3, markers.size)
        assertEquals("起点", markers[0].title)
        assertEquals(HikePlanCheckpointType.ENERGY_CHECK, markers[1].type)
        assertEquals(30.0, markers[1].latitude, 0.0001)
        assertEquals(120.5, markers[1].longitude, 0.0001)
        assertEquals(HikePlanCheckpointType.REST_CHECK, markers[2].type)
        assertEquals(30.5, markers[2].latitude, 0.0001)
        assertEquals(121.0, markers[2].longitude, 0.0001)
    }
}
