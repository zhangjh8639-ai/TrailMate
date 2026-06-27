package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PmTilesOfflineBasemapImportPolicyTest {
    @Test
    fun acceptsNonEmptyPmtilesFileAndTargetsCurrentRoutePack() {
        val result = PmTilesOfflineBasemapImportPolicy.resolve(
            candidate = PmTilesOfflineBasemapImportCandidate(
                displayName = "杭州西湖.pmtiles",
                sizeBytes = 12_048L,
                archiveInspection = PmTilesArchiveInspection(
                    header = validHeader(),
                    error = null
                )
            ),
            routePackKey = "longjing-ridge",
            targetBounds = PmTilesLatLngBounds(120.15, 30.15, 120.20, 30.20)
        )

        assertTrue(result.canImport)
        assertEquals(PmTilesOfflineBasemapImportStatus.READY_TO_IMPORT, result.status)
        assertEquals("longjing-ridge.pmtiles", result.targetFileName)
        assertEquals("准备导入 PMTiles 离线地图包", result.title)
    }

    @Test
    fun rejectsNonPmtilesFile() {
        val result = PmTilesOfflineBasemapImportPolicy.resolve(
            candidate = PmTilesOfflineBasemapImportCandidate(
                displayName = "hangzhou.mbtiles",
                sizeBytes = 12_048L
            ),
            routePackKey = "longjing-ridge"
        )

        assertFalse(result.canImport)
        assertEquals(PmTilesOfflineBasemapImportStatus.INVALID_EXTENSION, result.status)
        assertEquals("请选择 .pmtiles 文件", result.title)
    }

    @Test
    fun rejectsEmptyFile() {
        val result = PmTilesOfflineBasemapImportPolicy.resolve(
            candidate = PmTilesOfflineBasemapImportCandidate(
                displayName = "hangzhou.pmtiles",
                sizeBytes = 0L,
                archiveInspection = null
            ),
            routePackKey = "longjing-ridge"
        )

        assertFalse(result.canImport)
        assertEquals(PmTilesOfflineBasemapImportStatus.EMPTY_FILE, result.status)
        assertEquals("PMTiles 文件为空", result.title)
    }

    @Test
    fun rejectsPmtilesFileWhenHeaderCannotBeValidated() {
        val result = PmTilesOfflineBasemapImportPolicy.resolve(
            candidate = PmTilesOfflineBasemapImportCandidate(
                displayName = "hangzhou.pmtiles",
                sizeBytes = 12_048L,
                archiveInspection = PmTilesArchiveInspection(
                    header = null,
                    error = PmTilesArchiveHeaderError.BAD_MAGIC
                )
            ),
            routePackKey = "longjing-ridge"
        )

        assertFalse(result.canImport)
        assertEquals(PmTilesOfflineBasemapImportStatus.INVALID_ARCHIVE, result.status)
        assertEquals("PMTiles 文件结构不正确", result.title)
    }

    @Test
    fun rejectsPmtilesFileWhenItDoesNotCoverTheRouteBounds() {
        val result = PmTilesOfflineBasemapImportPolicy.resolve(
            candidate = PmTilesOfflineBasemapImportCandidate(
                displayName = "ningbo.pmtiles",
                sizeBytes = 12_048L,
                archiveInspection = PmTilesArchiveInspection(
                    header = validHeader(PmTilesLatLngBounds(121.40, 29.80, 121.70, 30.00)),
                    error = null
                )
            ),
            routePackKey = "longjing-ridge",
            targetBounds = PmTilesLatLngBounds(120.15, 30.15, 120.20, 30.20)
        )

        assertFalse(result.canImport)
        assertEquals(PmTilesOfflineBasemapImportStatus.REGION_NOT_COVERED, result.status)
        assertEquals("PMTiles 未覆盖当前路线", result.title)
    }

    @Test
    fun rejectsPmtilesFileWhenItOnlyPartiallyCoversTheRouteBounds() {
        val result = PmTilesOfflineBasemapImportPolicy.resolve(
            candidate = PmTilesOfflineBasemapImportCandidate(
                displayName = "partial.pmtiles",
                sizeBytes = 12_048L,
                archiveInspection = PmTilesArchiveInspection(
                    header = validHeader(PmTilesLatLngBounds(120.10, 30.10, 120.18, 30.35)),
                    error = null
                )
            ),
            routePackKey = "longjing-ridge",
            targetBounds = PmTilesLatLngBounds(120.15, 30.15, 120.20, 30.20)
        )

        assertFalse(result.canImport)
        assertEquals(PmTilesOfflineBasemapImportStatus.REGION_NOT_COVERED, result.status)
        assertEquals("PMTiles 未覆盖当前路线", result.title)
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
