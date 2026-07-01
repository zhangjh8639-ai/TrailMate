package com.trailmate.app.core.routeimport

import com.trailmate.app.core.model.RouteSourceType
import com.trailmate.app.core.model.WaypointType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteImportParserTest {
    @Test
    fun parsesGpxTrackIntoNavigationGeometry() {
        val result = RouteImportParser.parse(
            fileName = "longjing.gpx",
            content = gpxTrack(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertEquals(RouteImportFormat.Gpx, result.format)
        assertEquals(RouteSourceType.ImportedGpx, result.sourceType)
        assertEquals("龙井测试线", result.routeName)
        assertEquals(4, result.trackPointCount)
        assertEquals(1, result.waypointCount)
        assertEquals(true, result.hasElevation)
        assertTrue(result.geometry!!.totalDistance.meters > 300.0)
        assertEquals(70.0, result.geometry.elevationGain.meters, 0.1)
        assertEquals(WaypointType.Checkpoint, result.geometry.waypoints.single().type)
    }

    @Test
    fun parsesGpxRoutePointsWhenTrackPointsAreAbsent() {
        val result = RouteImportParser.parse(
            fileName = "route-only.gpx",
            content = gpxRouteOnly(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertEquals(RouteImportFormat.Gpx, result.format)
        assertEquals(3, result.trackPointCount)
        assertNotNull(result.geometry)
    }

    @Test
    fun fallsBackToGpxRoutePointsWhenTrackGeometryIsInsufficient() {
        val result = RouteImportParser.parse(
            fileName = "short-track-route.gpx",
            content = gpxShortTrackWithRoute(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertEquals(3, result.trackPointCount)
        assertTrue(result.geometry!!.totalDistance.meters > 200.0)
    }

    @Test
    fun parsesKmlLineStringIntoNavigationGeometry() {
        val result = RouteImportParser.parse(
            fileName = "meijiawu.kml",
            content = kmlLineString(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertEquals(RouteImportFormat.Kml, result.format)
        assertEquals(RouteSourceType.ImportedKml, result.sourceType)
        assertEquals("梅家坞测试线", result.routeName)
        assertEquals(3, result.trackPointCount)
        assertEquals(true, result.hasElevation)
        assertTrue(result.geometry!!.totalDistance.meters > 200.0)
    }

    @Test
    fun parsesKmlPointPlacemarksAsWaypoints() {
        val result = RouteImportParser.parse(
            fileName = "kml-waypoint.kml",
            content = kmlLineString(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertEquals(1, result.waypointCount)
        assertEquals("观景台", result.geometry!!.waypoints.single().title)
        assertTrue(result.geometry.waypoints.single().distanceFromStart.meters > 0.0)
    }

    private fun gpxTrack(): String =
        """
        <gpx version="1.1" creator="TrailMate test" xmlns="http://www.topografix.com/GPX/1/1">
          <metadata><name>龙井测试线</name></metadata>
          <wpt lat="30.0020" lon="120.0010"><name>补水点</name></wpt>
          <trk>
            <name>备用轨迹名</name>
            <trkseg>
              <trkpt lat="30.0000" lon="120.0000"><ele>100</ele></trkpt>
              <trkpt lat="30.0010" lon="120.0000"><ele>130</ele></trkpt>
              <trkpt lat="30.0020" lon="120.0010"><ele>120</ele></trkpt>
              <trkpt lat="30.0030" lon="120.0020"><ele>160</ele></trkpt>
            </trkseg>
          </trk>
        </gpx>
        """.trimIndent()

    private fun gpxRouteOnly(): String =
        """
        <gpx version="1.1" creator="TrailMate test">
          <rte>
            <name>route-only</name>
            <rtept lat="30.0000" lon="120.0000"><ele>10</ele></rtept>
            <rtept lat="30.0010" lon="120.0000"><ele>20</ele></rtept>
            <rtept lat="30.0020" lon="120.0010"><ele>30</ele></rtept>
          </rte>
        </gpx>
        """.trimIndent()

    private fun gpxShortTrackWithRoute(): String =
        """
        <gpx version="1.1" creator="TrailMate test">
          <trk>
            <trkseg>
              <trkpt lat="30.0000" lon="120.0000"><ele>10</ele></trkpt>
            </trkseg>
          </trk>
          <rte>
            <name>fallback-route</name>
            <rtept lat="30.0000" lon="120.0000"><ele>10</ele></rtept>
            <rtept lat="30.0010" lon="120.0000"><ele>20</ele></rtept>
            <rtept lat="30.0020" lon="120.0010"><ele>30</ele></rtept>
          </rte>
        </gpx>
        """.trimIndent()

    private fun kmlLineString(): String =
        """
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Document>
            <name>梅家坞测试线</name>
            <Placemark>
              <name>主路线</name>
              <LineString>
                <coordinates>
                  120.0000,30.0000,100
                  120.0010,30.0010,130
                  120.0020,30.0020,120
                </coordinates>
              </LineString>
            </Placemark>
            <Placemark>
              <name>观景台</name>
              <Point><coordinates>120.0010,30.0010,130</coordinates></Point>
            </Placemark>
          </Document>
        </kml>
        """.trimIndent()
}
