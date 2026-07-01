package com.trailmate.app.feature.routes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteImportFileReaderTest {
    @Test
    fun supportedImportFileRecognizesGpxKmlAndGenericMimeFallback() {
        assertTrue(RouteImportFileReader.isSupportedFile("track.gpx", null))
        assertTrue(RouteImportFileReader.isSupportedFile("track.kml", "application/octet-stream"))
        assertTrue(RouteImportFileReader.isSupportedFile("route", "application/gpx+xml"))
        assertTrue(RouteImportFileReader.isSupportedFile("route", "application/vnd.google-earth.kml+xml"))
        assertFalse(RouteImportFileReader.isSupportedFile("route", "application/xml"))
        assertFalse(RouteImportFileReader.isSupportedFile("route", "application/octet-stream"))
        assertFalse(RouteImportFileReader.isSupportedFile("notes.txt", "text/plain"))
    }

    @Test
    fun mimeOnlyRouteFileGetsParsingExtension() {
        assertEquals(
            "route.gpx",
            RouteImportFileReader.fileNameForParsing("route", "application/gpx+xml"),
        )
        assertEquals(
            "route.kml",
            RouteImportFileReader.fileNameForParsing("route", "application/vnd.google-earth.kml+xml"),
        )
        assertEquals(
            "track.gpx",
            RouteImportFileReader.fileNameForParsing("track.gpx", "application/vnd.google-earth.kml+xml"),
        )
    }

    @Test
    fun displayNameFallsBackToUriPathSegment() {
        assertEquals(
            "longjing-loop.gpx",
            RouteImportFileReader.displayNameFromPath("/storage/emulated/0/Download/longjing-loop.gpx"),
        )
        assertEquals(
            "未知路线文件",
            RouteImportFileReader.displayNameFromPath(null),
        )
    }

    @Test
    fun sizeGuardRejectsOversizedText() {
        val result = RouteImportFileReader.textFromBytes(
            fileName = "huge.gpx",
            bytes = ByteArray(RouteImportFileReader.MaxImportBytes + 1) { 'x'.code.toByte() },
        )

        assertTrue(result is RouteImportFileReadResult.Failed)
        assertEquals("huge.gpx", (result as RouteImportFileReadResult.Failed).fileName)
        assertEquals("文件过大，暂不支持直接导入", result.reason)
    }

    @Test
    fun textFromBytesReturnsUtf8ContentForSupportedSize() {
        val result = RouteImportFileReader.textFromBytes(
            fileName = "small.gpx",
            bytes = "<gpx>路线</gpx>".toByteArray(Charsets.UTF_8),
        )

        assertTrue(result is RouteImportFileReadResult.Success)
        assertEquals("small.gpx", (result as RouteImportFileReadResult.Success).fileName)
        assertEquals("<gpx>路线</gpx>", result.content)
    }
}
