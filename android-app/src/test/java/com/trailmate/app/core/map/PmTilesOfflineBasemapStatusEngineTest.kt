package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PmTilesOfflineBasemapStatusEngineTest {
    @Test
    fun marksPmtilesPackReadyWhenFileExistsAndCoversTargetRegion() {
        val status = PmTilesOfflineBasemapStatusEngine.resolve(
            manifest = PmTilesOfflineBasemapManifest(
                fileName = "hangzhou-west-lake.pmtiles",
                fileExists = true,
                fileSizeBytes = 120_000_000L,
                coveredRegionNames = setOf("杭州市"),
                archiveHeader = validHeader(),
                targetBounds = PmTilesLatLngBounds(120.15, 30.15, 120.20, 30.20),
                coversTargetBounds = true
            ),
            targetRegionName = "杭州市"
        )

        assertTrue(status.ready)
        assertEquals(PmTilesOfflineBasemapStatus.READY, status.status)
        assertEquals("PMTiles 离线地图包已导入", status.title)
        assertEquals("已导入", status.stepValue)
    }

    @Test
    fun blocksReadinessWhenPmtilesFileIsMissing() {
        val status = PmTilesOfflineBasemapStatusEngine.resolve(
            manifest = PmTilesOfflineBasemapManifest(
                fileName = "hangzhou-west-lake.pmtiles",
                fileExists = false,
                fileSizeBytes = 0L,
                coveredRegionNames = setOf("杭州市")
            ),
            targetRegionName = "杭州市"
        )

        assertFalse(status.ready)
        assertEquals(PmTilesOfflineBasemapStatus.MISSING_FILE, status.status)
        assertEquals("PMTiles 离线地图包待导入", status.title)
        assertEquals("待导入", status.stepValue)
    }

    @Test
    fun blocksReadinessWhenFileIsNotPmtiles() {
        val status = PmTilesOfflineBasemapStatusEngine.resolve(
            manifest = PmTilesOfflineBasemapManifest(
                fileName = "hangzhou-west-lake.mbtiles",
                fileExists = true,
                fileSizeBytes = 120_000_000L,
                coveredRegionNames = setOf("杭州市")
            ),
            targetRegionName = "杭州市"
        )

        assertFalse(status.ready)
        assertEquals(PmTilesOfflineBasemapStatus.INVALID_FORMAT, status.status)
        assertEquals("PMTiles 文件格式不正确", status.title)
        assertEquals("需重新导入", status.stepValue)
    }

    @Test
    fun blocksReadinessWhenPackDoesNotCoverTargetRegion() {
        val status = PmTilesOfflineBasemapStatusEngine.resolve(
            manifest = PmTilesOfflineBasemapManifest(
                fileName = "ningbo.pmtiles",
                fileExists = true,
                fileSizeBytes = 90_000_000L,
                coveredRegionNames = emptySet(),
                archiveHeader = validHeader(
                    bounds = PmTilesLatLngBounds(121.40, 29.80, 121.70, 30.00)
                ),
                targetBounds = PmTilesLatLngBounds(120.15, 30.15, 120.20, 30.20),
                coversTargetBounds = false
            ),
            targetRegionName = "杭州市"
        )

        assertFalse(status.ready)
        assertEquals(PmTilesOfflineBasemapStatus.REGION_NOT_COVERED, status.status)
        assertEquals("离线地图包未覆盖目标区域", status.title)
        assertEquals("待导入杭州市", status.stepValue)
    }

    @Test
    fun blocksReadinessWhenPackOnlyPartiallyCoversTargetBounds() {
        val status = PmTilesOfflineBasemapStatusEngine.resolve(
            manifest = PmTilesOfflineBasemapManifest(
                fileName = "partial.pmtiles",
                fileExists = true,
                fileSizeBytes = 90_000_000L,
                coveredRegionNames = emptySet(),
                archiveHeader = validHeader(
                    bounds = PmTilesLatLngBounds(120.10, 30.10, 120.18, 30.35)
                ),
                targetBounds = PmTilesLatLngBounds(120.15, 30.15, 120.20, 30.20),
                coversTargetBounds = false
            ),
            targetRegionName = "杭州市"
        )

        assertFalse(status.ready)
        assertEquals(PmTilesOfflineBasemapStatus.REGION_NOT_COVERED, status.status)
        assertEquals("离线地图包未覆盖目标区域", status.title)
    }

    @Test
    fun blocksReadinessWhenHeaderCannotBeValidated() {
        val status = PmTilesOfflineBasemapStatusEngine.resolve(
            manifest = PmTilesOfflineBasemapManifest(
                fileName = "hangzhou-west-lake.pmtiles",
                fileExists = true,
                fileSizeBytes = 32L,
                archiveHeaderError = PmTilesArchiveHeaderError.TOO_SHORT
            ),
            targetRegionName = "杭州市"
        )

        assertFalse(status.ready)
        assertEquals(PmTilesOfflineBasemapStatus.INVALID_FORMAT, status.status)
        assertEquals("PMTiles 文件结构不正确", status.title)
        assertEquals("需重新导入", status.stepValue)
    }

    private fun validHeader(
        bounds: PmTilesLatLngBounds = PmTilesLatLngBounds(120.10, 30.10, 120.30, 30.35)
    ) = PmTilesArchiveHeader(
        specVersion = 3,
        rootDirectoryOffset = 127L,
        rootDirectoryLength = 8L,
        metadataOffset = 135L,
        metadataLength = 2L,
        tileDataOffset = 137L,
        tileDataLength = 4L,
        tileType = PmTilesTileType.MVT_VECTOR_TILE,
        minZoom = 10,
        maxZoom = 14,
        bounds = bounds
    )
}
