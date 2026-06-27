package com.trailmate.app.core.map

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PmTilesOfflineBasemapManifestReaderTest {
    @Test
    fun readsExistingRoutePackManifestFromPmtilesDirectory() {
        val directory = Files.createTempDirectory("trailmate-pmtiles").toFile()
        val packFile = directory.resolve("longjing.pmtiles")
        PmTilesArchiveHeaderParserTest.validPmTilesFile(
            minLon = 120.10,
            minLat = 30.10,
            maxLon = 120.30,
            maxLat = 30.35
        ).copyTo(packFile, overwrite = true)

        val manifest = PmTilesOfflineBasemapManifestReader.read(
            directory = directory,
            routePackKey = "longjing",
            targetRegionName = "杭州市",
            targetBounds = PmTilesLatLngBounds(120.12, 30.12, 120.18, 30.20)
        )

        assertEquals("longjing.pmtiles", manifest.fileName)
        assertTrue(manifest.fileExists)
        assertTrue(manifest.fileSizeBytes > 127L)
        assertNotNull(manifest.archiveHeader)
        assertTrue(manifest.coversTargetBounds)
    }

    @Test
    fun returnsMissingManifestWhenRoutePackFileDoesNotExist() {
        val directory = Files.createTempDirectory("trailmate-pmtiles-missing").toFile()

        val manifest = PmTilesOfflineBasemapManifestReader.read(
            directory = directory,
            routePackKey = "longjing",
            targetRegionName = "杭州市"
        )

        assertEquals("longjing.pmtiles", manifest.fileName)
        assertFalse(manifest.fileExists)
        assertEquals(0L, manifest.fileSizeBytes)
        assertNull(manifest.archiveHeader)
        assertFalse(manifest.coversTargetBounds)
    }

    @Test
    fun rejectsExistingRoutePackWhenHeaderIsMissing() {
        val directory = Files.createTempDirectory("trailmate-pmtiles-invalid").toFile()
        directory.resolve("longjing.pmtiles").writeBytes(ByteArray(32) { it.toByte() })

        val manifest = PmTilesOfflineBasemapManifestReader.read(
            directory = directory,
            routePackKey = "longjing",
            targetRegionName = "杭州市",
            targetBounds = PmTilesLatLngBounds(120.12, 30.12, 120.18, 30.20)
        )

        assertTrue(manifest.fileExists)
        assertEquals(PmTilesArchiveHeaderError.TOO_SHORT, manifest.archiveHeaderError)
        assertFalse(manifest.coversTargetBounds)
    }

    @Test
    fun marksRoutePackNotCoveredWhenPmtilesOnlyPartiallyCoversTargetBounds() {
        val directory = Files.createTempDirectory("trailmate-pmtiles-partial").toFile()
        val packFile = directory.resolve("longjing.pmtiles")
        PmTilesArchiveHeaderParserTest.validPmTilesFile(
            minLon = 120.10,
            minLat = 30.10,
            maxLon = 120.18,
            maxLat = 30.35
        ).copyTo(packFile, overwrite = true)

        val manifest = PmTilesOfflineBasemapManifestReader.read(
            directory = directory,
            routePackKey = "longjing",
            targetRegionName = "杭州市",
            targetBounds = PmTilesLatLngBounds(120.15, 30.15, 120.20, 30.20)
        )

        assertTrue(manifest.fileExists)
        assertNotNull(manifest.archiveHeader)
        assertFalse(manifest.coversTargetBounds)
    }
}
