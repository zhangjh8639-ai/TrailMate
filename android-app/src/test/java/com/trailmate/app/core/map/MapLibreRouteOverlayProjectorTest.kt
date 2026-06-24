package com.trailmate.app.core.map

import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanCheckpointType
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.TrackRecordingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapLibreRouteOverlayProjectorTest {
    @Test
    fun projectsDrawableRouteTrackAndCheckpointOverlays() {
        val route = ImportedRoute(
            routeName = "龙井山脊",
            fileName = "longjing.gpx",
            distanceKm = 10.0,
            ascentMeters = 400,
            status = RouteImportStatus.PARSED,
            pointCount = 4,
            routePoints = listOf(
                RoutePoint(latitude = 30.0, longitude = 120.0, elevationMeters = null, distanceAlongRouteKm = 0.0),
                RoutePoint(latitude = Double.NaN, longitude = 120.2, elevationMeters = null, distanceAlongRouteKm = 2.0),
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
                    type = HikePlanCheckpointType.REST_CHECK,
                    title = "CP1",
                    distanceKm = 5.0,
                    timeFromStart = "2:10",
                    note = "短休"
                )
            )
        )
        val trackRecording = TrackRecordingState(
            points = listOf(
                RecordedTrackPoint(
                    latitude = 30.0,
                    longitude = 120.1,
                    elevationMeters = null,
                    horizontalAccuracyMeters = 5.0,
                    timestampEpochMillis = 1_000L
                ),
                RecordedTrackPoint(
                    latitude = 30.1,
                    longitude = 120.2,
                    elevationMeters = null,
                    horizontalAccuracyMeters = 5.0,
                    timestampEpochMillis = 2_000L
                ),
                RecordedTrackPoint(
                    latitude = 30.2,
                    longitude = Double.POSITIVE_INFINITY,
                    elevationMeters = null,
                    horizontalAccuracyMeters = 5.0,
                    timestampEpochMillis = 3_000L
                )
            )
        )

        val overlay = MapLibreRouteOverlayProjector.project(
            route = route,
            plan = plan,
            trackRecording = trackRecording
        )

        assertTrue(overlay.hasDrawableRoute)
        assertEquals(3, overlay.routePoints.size)
        assertEquals(2, overlay.trackPoints.size)
        assertEquals(2, overlay.checkpoints.size)
        assertEquals("CP1", overlay.checkpoints[1].title)
        assertEquals(30.0, overlay.checkpoints[1].latitude, 0.0001)
        assertEquals(121.0, overlay.checkpoints[1].longitude, 0.0001)
    }

    @Test
    fun marksRouteAsNotDrawableWhenGeometryHasLessThanTwoValidPoints() {
        val route = ImportedRoute(
            routeName = "空路线",
            fileName = "empty.gpx",
            distanceKm = 0.0,
            ascentMeters = 0,
            status = RouteImportStatus.PARSED,
            pointCount = 1,
            routePoints = listOf(
                RoutePoint(latitude = 30.0, longitude = 120.0, elevationMeters = null, distanceAlongRouteKm = 0.0)
            )
        )

        val overlay = MapLibreRouteOverlayProjector.project(
            route = route,
            plan = HikePlanSummary(checkpoints = emptyList()),
            trackRecording = TrackRecordingState()
        )

        assertFalse(overlay.hasDrawableRoute)
        assertEquals(1, overlay.routePoints.size)
        assertEquals(0, overlay.checkpoints.size)
    }
}
