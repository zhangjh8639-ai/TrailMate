package com.trailmate.app.core.routeimport

import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.core.model.RouteOfflineStatus
import com.trailmate.app.core.model.RouteSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RouteImportValidationTest {
    @Test
    fun missingElevationAddsQualityWarning() {
        val result = RouteImportParser.parse(
            fileName = "missing-ele.gpx",
            content = gpxWithoutElevation(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertEquals(false, result.hasElevation)
        assertTrue(RouteImportWarning.MissingElevation in result.warnings)
    }

    @Test
    fun nonFiniteElevationIsIgnoredAsMissingElevation() {
        val result = RouteImportParser.parse(
            fileName = "nan-ele.gpx",
            content = """
            <gpx version="1.1">
              <trk><trkseg>
                <trkpt lat="30.0000" lon="120.0000"><ele>NaN</ele></trkpt>
                <trkpt lat="30.0010" lon="120.0000"><ele>Infinity</ele></trkpt>
              </trkseg></trk>
            </gpx>
            """.trimIndent(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertEquals(false, result.hasElevation)
        assertTrue(RouteImportWarning.MissingElevation in result.warnings)
    }

    @Test
    fun sparseTrackAddsQualityWarning() {
        val result = RouteImportParser.parse(
            fileName = "sparse.gpx",
            content = gpxWithoutElevation(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertTrue(RouteImportWarning.SparseTrack in result.warnings)
    }

    @Test
    fun largePointGapAddsQualityWarning() {
        val result = RouteImportParser.parse(
            fileName = "large-gap.gpx",
            content = largeGapGpx(),
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertTrue(RouteImportWarning.LargePointGap in result.warnings)
    }

    @Test
    fun unsupportedExtensionReturnsRejectedResult() {
        val result = RouteImportParser.parse(
            fileName = "notes.txt",
            content = "not a route",
        )

        assertEquals(RouteImportStatus.UnsupportedFormat, result.status)
        assertTrue(RouteImportWarning.UnsupportedFormat in result.warnings)
        assertNull(result.geometry)
    }

    @Test
    fun doctypeXmlReturnsInvalidXmlWithoutParsingExternalEntities() {
        val result = RouteImportParser.parse(
            fileName = "unsafe.gpx",
            content = """
            <!DOCTYPE gpx [
              <!ENTITY xxe SYSTEM "file:///etc/passwd">
            ]>
            <gpx version="1.1">
              <metadata><name>&xxe;</name></metadata>
              <trk><trkseg>
                <trkpt lat="30.0000" lon="120.0000" />
                <trkpt lat="30.0010" lon="120.0000" />
              </trkseg></trk>
            </gpx>
            """.trimIndent(),
        )

        assertEquals(RouteImportStatus.InvalidXml, result.status)
        assertTrue(RouteImportWarning.InvalidXml in result.warnings)
        assertNull(result.geometry)
    }

    @Test
    fun lowercaseDoctypeXmlIsRejectedBeforePlatformParserConfiguration() {
        val result = RouteImportParser.parse(
            fileName = "unsafe-lowercase.gpx",
            content = """
            <!doctype gpx [
              <!entity xxe SYSTEM "file:///etc/passwd">
            ]>
            <gpx version="1.1">
              <trk><trkseg>
                <trkpt lat="30.0000" lon="120.0000" />
                <trkpt lat="30.0010" lon="120.0000" />
              </trkseg></trk>
            </gpx>
            """.trimIndent(),
        )

        assertEquals(RouteImportStatus.InvalidXml, result.status)
        assertTrue(RouteImportWarning.InvalidXml in result.warnings)
        assertNull(result.geometry)
    }

    @Test
    fun missingTrackGeometryReturnsRejectedResult() {
        val result = RouteImportParser.parse(
            fileName = "empty.gpx",
            content = "<gpx version=\"1.1\"><metadata><name>空文件</name></metadata></gpx>",
        )

        assertEquals(RouteImportStatus.MissingTrackGeometry, result.status)
        assertTrue(RouteImportWarning.MissingTrackGeometry in result.warnings)
        assertNull(result.geometry)
    }

    @Test
    fun utf8BomPrefixedGpxStillParses() {
        val result = RouteImportParser.parse(
            fileName = "bom.gpx",
            content = "\uFEFF<gpx version=\"1.1\"><trk><trkseg>" +
                "<trkpt lat=\"30.0000\" lon=\"120.0000\" />" +
                "<trkpt lat=\"30.0010\" lon=\"120.0010\" />" +
                "</trkseg></trk></gpx>",
        )

        assertEquals(RouteImportStatus.Parsed, result.status)
        assertTrue(result.trackPointCount >= 2)
    }

    @Test
    fun parsedImportCreatesPrivateTrackOnlyRoute() {
        val result = RouteImportParser.parse(
            fileName = "private.gpx",
            content = largeGapGpx(),
        )

        val route = result.toImportedRoute(
            id = RouteId("import-private"),
            region = "杭州",
            importedAt = Instant.parse("2026-07-01T00:00:00Z"),
        )

        assertEquals(PrivacyVisibility.Private, route.visibility)
        assertEquals(RouteOfflineStatus.TrackOnly, route.offlineStatus)
        assertEquals(RouteSourceType.ImportedGpx, route.sourceType)
    }

    private fun gpxWithoutElevation(): String =
        """
        <gpx version="1.1">
          <trk>
            <name>缺少海拔</name>
            <trkseg>
              <trkpt lat="30.0000" lon="120.0000" />
              <trkpt lat="30.0005" lon="120.0005" />
            </trkseg>
          </trk>
        </gpx>
        """.trimIndent()

    private fun largeGapGpx(): String =
        """
        <gpx version="1.1">
          <metadata><name>大间距轨迹</name></metadata>
          <trk>
            <trkseg>
              <trkpt lat="30.0000" lon="120.0000"><ele>100</ele></trkpt>
              <trkpt lat="30.0200" lon="120.0000"><ele>120</ele></trkpt>
              <trkpt lat="30.0210" lon="120.0010"><ele>125</ele></trkpt>
            </trkseg>
          </trk>
        </gpx>
        """.trimIndent()
}
