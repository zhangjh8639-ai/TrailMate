package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class RouteModelsTest {
    @Test
    fun importedRouteDefaultsToPrivateTrackOnlyVisibility() {
        val route = TrailRoute.imported(
            id = RouteId("r-gpx"),
            name = "龙井环线导入轨迹",
            region = "杭州 · 西湖群山",
            geometry = sampleGeometry(),
            importedAt = Instant.parse("2026-07-01T00:00:00Z"),
        )

        assertEquals(PrivacyVisibility.Private, route.visibility)
        assertEquals(RouteSourceType.ImportedGpx, route.sourceType)
        assertEquals(RouteOfflineStatus.TrackOnly, route.offlineStatus)
        assertEquals(RouteConfidence.Unverified, route.confidence)
    }

    @Test
    fun platformRouteCarriesCredibilityAndNavigationMetadata() {
        val route = TrailRoute.platform(
            id = RouteId("longjing"),
            name = "九溪十八涧 · 龙井环线",
            region = "杭州 · 西湖群山",
            routeType = "环线 · 山野步道",
            geometry = sampleGeometry(),
            estimatedDuration = Duration.ofHours(3).plusMinutes(20),
            difficulty = RouteDifficulty.Moderate,
            confidence = RouteConfidence.A,
            routeVersion = "v3.2",
            lastUpdated = Instant.parse("2026-06-24T00:00:00Z"),
            riskTags = listOf("雨后湿滑", "岔路较多"),
            recentFeedback = listOf("近 7 天无封路反馈"),
        )

        assertEquals(RouteSourceType.Platform, route.sourceType)
        assertEquals(RouteOfflineStatus.Verified, route.offlineStatus)
        assertEquals(PrivacyVisibility.Private, route.visibility)
        assertEquals(Distance.meters(1250.0), route.distance)
        assertEquals(Elevation.meters(46.0), route.elevationGain)
    }

    @Test
    fun routeGeometryKeepsNavigationInputsSeparateFromUiCopy() {
        val geometry = sampleGeometry()

        assertEquals(2, geometry.coordinates.size)
        assertEquals(Distance.meters(1250.0), geometry.cumulativeDistances.last())
        assertTrue(geometry.hasElevation)
        assertEquals(WaypointType.Water, geometry.waypoints.single().type)
        assertEquals(RiskPointType.Slippery, geometry.riskPoints.single().type)
        assertEquals(ExitPointType.RoadAccess, geometry.exitPoints.single().type)
    }

    @Test
    fun routeGeometryRejectsMismatchedCumulativeDistanceInputs() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            RouteGeometry(
                coordinates = listOf(
                    GeoCoordinate(30.245, 120.116),
                    GeoCoordinate(30.248, 120.121),
                ),
                cumulativeDistances = emptyList(),
            )
        }

        assertEquals("Cumulative distances must match coordinate count.", error.message)
    }

    @Test
    fun routeGeometryRejectsNonMonotonicOrOutOfBoundsNavigationInputs() {
        assertThrows(IllegalArgumentException::class.java) {
            RouteGeometry(
                coordinates = listOf(
                    GeoCoordinate(30.245, 120.116),
                    GeoCoordinate(30.248, 120.121),
                ),
                cumulativeDistances = listOf(Distance.ZERO, Distance.meters(900.0)),
                waypoints = listOf(
                    RouteWaypoint(
                        id = "wp-out",
                        title = "越界航点",
                        type = WaypointType.Checkpoint,
                        distanceFromStart = Distance.meters(1200.0),
                    ),
                ),
            )
        }

        val error = assertThrows(IllegalArgumentException::class.java) {
            RouteGeometry(
                coordinates = listOf(
                    GeoCoordinate(30.245, 120.116),
                    GeoCoordinate(30.248, 120.121),
                    GeoCoordinate(30.250, 120.125),
                ),
                cumulativeDistances = listOf(
                    Distance.ZERO,
                    Distance.meters(900.0),
                    Distance.meters(850.0),
                ),
            )
        }

        assertEquals("Cumulative distances must be monotonic.", error.message)
    }

    private fun sampleGeometry(): RouteGeometry =
        RouteGeometry(
            coordinates = listOf(
                GeoCoordinate(
                    latitude = 30.245,
                    longitude = 120.116,
                    elevation = Elevation.meters(72.0),
                ),
                GeoCoordinate(
                    latitude = 30.248,
                    longitude = 120.121,
                    elevation = Elevation.meters(118.0),
                ),
            ),
            cumulativeDistances = listOf(Distance.ZERO, Distance.meters(1250.0)),
            waypoints = listOf(
                RouteWaypoint(
                    id = "wp-water",
                    title = "补水点",
                    type = WaypointType.Water,
                    distanceFromStart = Distance.meters(800.0),
                ),
            ),
            riskPoints = listOf(
                RouteRiskPoint(
                    id = "risk-slip",
                    title = "雨后湿滑",
                    type = RiskPointType.Slippery,
                    distanceFromStart = Distance.meters(420.0),
                ),
            ),
            exitPoints = listOf(
                RouteExitPoint(
                    id = "exit-road",
                    title = "最近公路",
                    type = ExitPointType.RoadAccess,
                    distanceFromStart = Distance.meters(1100.0),
                ),
            ),
        )
}
