package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.RouteImportStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetRouteGpxParserTest {
    @Test
    fun parsesTrackPointRouteSummary() {
        val route = TargetRouteGpxParser.parse(
            fileName = "longjing-ridge-target.gpx",
            content = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gpx version="1.1" creator="TrailMateTest">
                  <trk>
                    <name>Longjing Ridge</name>
                    <trkseg>
                      <trkpt lat="30.250000" lon="120.120000"><ele>100</ele></trkpt>
                      <trkpt lat="30.317000" lon="120.120000"><ele>420</ele></trkpt>
                      <trkpt lat="30.386700" lon="120.120000"><ele>960</ele></trkpt>
                    </trkseg>
                  </trk>
                </gpx>
            """.trimIndent()
        )

        assertEquals("Longjing Ridge", route.routeName)
        assertEquals("longjing-ridge-target.gpx", route.fileName)
        assertEquals(RouteImportStatus.PARSED, route.status)
        assertEquals(3, route.pointCount)
        assertEquals(860, route.ascentMeters)
        assertEquals(3, route.routePoints.size)
        assertEquals(0.0, route.routePoints.first().distanceAlongRouteKm, 0.001)
        assertEquals(route.distanceKm, route.routePoints.last().distanceAlongRouteKm, 0.1)
        assertTrue(route.readyForAssessment())
        assertEquals("15.2 km / +860 m", route.summaryLabel())
    }

    @Test
    fun parsesRoutePointGpx() {
        val route = TargetRouteGpxParser.parse(
            fileName = "river-loop.gpx",
            content = """
                <gpx version="1.1">
                  <rte>
                    <name>River Loop</name>
                    <rtept lat="30.0000" lon="120.0000"><ele>50</ele></rtept>
                    <rtept lat="30.0100" lon="120.0000"><ele>80</ele></rtept>
                  </rte>
                </gpx>
            """.trimIndent()
        )

        assertEquals("River Loop", route.routeName)
        assertEquals(2, route.pointCount)
        assertEquals(30, route.ascentMeters)
    }

    @Test
    fun prefersExporterExtensionMetadataWhenTrackHasNoName() {
        val route = TargetRouteGpxParser.parse(
            fileName = "2026-05-16 0834杭州上城区.gpx",
            content = """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <gpx version="1.1"
                  creator="GPSBabel"
                  xmlns="http://www.topografix.com/GPX/1/1">
                  <metadata>
                    <time>2026-05-16T00:34:42Z</time>
                  </metadata>
                  <extensions>
                    <name><![CDATA[2026-05-16 08:34杭州上城区]]></name>
                    <Distance>21610.625132127225</Distance>
                    <ElevationGain>767.2122065862956</ElevationGain>
                  </extensions>
                  <trk>
                    <trkseg>
                      <trkpt lat="30.25544589" lon="120.15835535">
                        <ele>56.0</ele>
                        <time>2026-05-16T00:34:44Z</time>
                      </trkpt>
                      <trkpt lat="30.25555388" lon="120.15835964">
                        <ele>57.0</ele>
                        <time>2026-05-16T00:34:54Z</time>
                      </trkpt>
                      <trkpt lat="30.25565361" lon="120.15832435">
                        <ele>58.0</ele>
                        <time>2026-05-16T00:35:04Z</time>
                      </trkpt>
                    </trkseg>
                  </trk>
                </gpx>
            """.trimIndent()
        )

        assertEquals("2026-05-16 08:34杭州上城区", route.routeName)
        assertEquals(21.6, route.distanceKm, 0.0)
        assertEquals(767, route.ascentMeters)
        assertEquals(3, route.pointCount)
        assertEquals(0.0, route.routePoints.first().distanceAlongRouteKm, 0.001)
    }

    @Test
    fun prefersTrackPointsWhenTrackAndRoutePointsAreBothPresent() {
        val route = TargetRouteGpxParser.parse(
            fileName = "mixed.gpx",
            content = """
                <gpx version="1.1">
                  <trk>
                    <name>Track Choice</name>
                    <trkseg>
                      <trkpt lat="30.0000" lon="120.0000"><ele>100</ele></trkpt>
                      <trkpt lat="30.0100" lon="120.0000"><ele>140</ele></trkpt>
                    </trkseg>
                  </trk>
                  <rte>
                    <name>Route Choice</name>
                    <rtept lat="31.0000" lon="121.0000"><ele>10</ele></rtept>
                    <rtept lat="32.0000" lon="122.0000"><ele>20</ele></rtept>
                  </rte>
                </gpx>
            """.trimIndent()
        )

        assertEquals("Track Choice", route.routeName)
        assertEquals(2, route.pointCount)
        assertEquals(40, route.ascentMeters)
    }

    @Test
    fun rejectsGpxWithoutRoutePoints() {
        assertThrows(IllegalArgumentException::class.java) {
            TargetRouteGpxParser.parse(
                fileName = "empty.gpx",
                content = "<gpx version=\"1.1\"><metadata><name>Empty</name></metadata></gpx>"
            )
        }
    }

    @Test
    fun rejectsDoctypeDeclarations() {
        assertThrows(Exception::class.java) {
            TargetRouteGpxParser.parse(
                fileName = "unsafe.gpx",
                content = """
                    <!DOCTYPE gpx [
                      <!ENTITY xxe SYSTEM "file:///etc/passwd">
                    ]>
                    <gpx version="1.1">
                      <rte>
                        <name>&xxe;</name>
                        <rtept lat="30.0000" lon="120.0000" />
                        <rtept lat="30.0100" lon="120.0000" />
                      </rte>
                    </gpx>
                """.trimIndent()
            )
        }
    }
}
