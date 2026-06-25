package com.trailmate.app.core.map

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PmTilesArchiveHeaderParserTest {
    @Test
    fun readsVersion3VectorHeaderWithBoundsAndTileData() {
        val file = validPmTilesFile(
            minLon = 120.10,
            minLat = 30.10,
            maxLon = 120.30,
            maxLat = 30.35
        )

        val inspection = PmTilesArchiveHeaderParser.inspect(file)

        assertNotNull(inspection.header)
        val header = inspection.header ?: error("Expected PMTiles header")
        assertNull(inspection.error)
        assertEquals(3, header.specVersion)
        assertEquals(PmTilesTileType.MVT_VECTOR_TILE, header.tileType)
        assertEquals(10, header.minZoom)
        assertEquals(14, header.maxZoom)
        assertEquals(120.10, header.bounds.minLongitude, 0.000001)
        assertEquals(30.35, header.bounds.maxLatitude, 0.000001)
        assertTrue(header.bounds.intersects(PmTilesLatLngBounds(120.12, 30.12, 120.18, 30.20)))
    }

    @Test
    fun rejectsShortOrNonPmtilesFile() {
        val file = Files.createTempFile("not-pmtiles", ".pmtiles").toFile()
        file.writeText("not a pmtiles archive")

        val inspection = PmTilesArchiveHeaderParser.inspect(file)

        assertNull(inspection.header)
        assertEquals(PmTilesArchiveHeaderError.TOO_SHORT, inspection.error)
    }

    @Test
    fun rejectsHeaderWhoseRouteDirectoryCannotFitInFirst16Kib() {
        val file = validPmTilesFile(rootOffset = 16_000L, rootLength = 600L)

        val inspection = PmTilesArchiveHeaderParser.inspect(file)

        assertNull(inspection.header)
        assertEquals(PmTilesArchiveHeaderError.ROOT_DIRECTORY_OUT_OF_FIRST_16KIB, inspection.error)
    }

    @Test
    fun boundsDoNotIntersectWhenRouteIsOutsideArchive() {
        val archiveBounds = PmTilesLatLngBounds(120.10, 30.10, 120.30, 30.35)
        val ningboRoute = PmTilesLatLngBounds(121.40, 29.80, 121.70, 30.00)

        assertFalse(archiveBounds.intersects(ningboRoute))
    }

    companion object {
        fun validPmTilesFile(
            rootOffset: Long = 127L,
            rootLength: Long = 8L,
            metadataOffset: Long = 135L,
            metadataLength: Long = 2L,
            tileDataOffset: Long = 137L,
            tileDataLength: Long = 4L,
            tileType: Int = 1,
            minZoom: Int = 10,
            maxZoom: Int = 14,
            minLon: Double = 120.0,
            minLat: Double = 30.0,
            maxLon: Double = 121.0,
            maxLat: Double = 31.0,
            centerLon: Double = 120.5,
            centerLat: Double = 30.5
        ): File {
            val size = (tileDataOffset + tileDataLength).coerceAtLeast(141L).toInt()
            val bytes = ByteArray(size)
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            buffer.put("PMTiles".toByteArray(Charsets.UTF_8))
            buffer.put(3)
            buffer.putLong(rootOffset)
            buffer.putLong(rootLength)
            buffer.putLong(metadataOffset)
            buffer.putLong(metadataLength)
            buffer.putLong(0L)
            buffer.putLong(0L)
            buffer.putLong(tileDataOffset)
            buffer.putLong(tileDataLength)
            buffer.putLong(1L)
            buffer.putLong(1L)
            buffer.putLong(1L)
            buffer.put(1)
            buffer.put(1)
            buffer.put(2)
            buffer.put(tileType.toByte())
            buffer.put(minZoom.toByte())
            buffer.put(maxZoom.toByte())
            buffer.putPosition(minLon, minLat)
            buffer.putPosition(maxLon, maxLat)
            buffer.put(12)
            buffer.putPosition(centerLon, centerLat)

            val file = Files.createTempFile("valid-pmtiles", ".pmtiles").toFile()
            file.writeBytes(bytes)
            return file
        }

        private fun ByteBuffer.putPosition(longitude: Double, latitude: Double) {
            putInt((longitude * 10_000_000).toInt())
            putInt((latitude * 10_000_000).toInt())
        }
    }
}
