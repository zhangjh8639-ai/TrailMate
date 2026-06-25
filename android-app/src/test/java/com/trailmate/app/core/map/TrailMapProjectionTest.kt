package com.trailmate.app.core.map

import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.RecordedTrackPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMapProjectionTest {
    @Test
    fun projectsRoutePointsInsidePaddedViewport() {
        val projected = TrailMapProjection.project(
            routePoints = listOf(
                RoutePoint(latitude = 30.10, longitude = 120.10, elevationMeters = 80.0, distanceAlongRouteKm = 0.0),
                RoutePoint(latitude = 30.15, longitude = 120.16, elevationMeters = 240.0, distanceAlongRouteKm = 4.8),
                RoutePoint(latitude = 30.21, longitude = 120.12, elevationMeters = 520.0, distanceAlongRouteKm = 9.6)
            ),
            width = 300f,
            height = 600f,
            paddingFraction = 0.12f
        )

        assertEquals(3, projected.size)
        projected.forEach { point ->
            assertTrue(point.x in 36f..264f)
            assertTrue(point.y in 72f..528f)
        }
        assertEquals(9.6, projected.last().distanceAlongRouteKm, 0.001)
    }

    @Test
    fun returnsEmptyProjectionForRouteWithoutGeometry() {
        val projected = TrailMapProjection.project(
            routePoints = emptyList(),
            width = 300f,
            height = 600f
        )

        assertTrue(projected.isEmpty())
    }

    @Test
    fun projectsRouteAndRecordedTrackInSharedViewport() {
        val layers = TrailMapProjection.projectLayers(
            routePoints = listOf(
                RoutePoint(latitude = 30.10, longitude = 120.10, elevationMeters = 80.0, distanceAlongRouteKm = 0.0),
                RoutePoint(latitude = 30.20, longitude = 120.20, elevationMeters = 220.0, distanceAlongRouteKm = 8.0)
            ),
            recordedTrackPoints = listOf(
                RecordedTrackPoint(
                    latitude = 30.11,
                    longitude = 120.05,
                    elevationMeters = 90.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 1_000L
                ),
                RecordedTrackPoint(
                    latitude = 30.18,
                    longitude = 120.12,
                    elevationMeters = 170.0,
                    horizontalAccuracyMeters = 9.0,
                    timestampEpochMillis = 2_000L
                )
            ),
            width = 300f,
            height = 600f,
            paddingFraction = 0.12f
        )

        assertEquals(2, layers.route.size)
        assertEquals(2, layers.recordedTrack.size)
        assertTrue(layers.recordedTrack.first().x < layers.route.first().x)
        (layers.route + layers.recordedTrack).forEach { point ->
            assertTrue(point.x in 36f..264f)
            assertTrue(point.y in 72f..528f)
        }
    }
}
