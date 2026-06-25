package com.trailmate.app.core.network

import com.trailmate.app.core.map.PmTilesArchiveHeaderParser
import com.trailmate.app.core.map.PmTilesArchiveHeaderParserTest
import com.trailmate.app.core.map.PmTilesLatLngBounds
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMatePmTilesBasemapRemoteImportCoordinatorTest {
    @Test
    fun downloadsValidCatalogPackIntoRoutePmTilesFile() {
        val directory = Files.createTempDirectory("pmtiles-remote-import").toFile()
        val routeBounds = PmTilesLatLngBounds(120.05, 30.10, 120.25, 30.35)
        val sourceFile = PmTilesArchiveHeaderParserTest.validPmTilesFile(
            minLon = 120.00,
            minLat = 30.05,
            maxLon = 120.30,
            maxLat = 30.40
        )
        val coordinator = TrailMatePmTilesBasemapRemoteImportCoordinator(
            catalogApi = FakeCatalogApi(listOf(catalogItem())),
            downloader = FakeDownloader(sourceFile)
        )

        val result = coordinator.importForRoute(
            routeBounds = routeBounds,
            routePackKey = "longjing-ridge",
            targetDirectory = directory
        )

        val targetFile = directory.resolve("longjing-ridge.pmtiles")
        assertEquals(TrailMatePmTilesRemoteImportAction.IMPORTED, result.action)
        assertEquals("已导入杭州市 · 西湖区离线地图包，正在刷新地图。", result.message)
        assertTrue(targetFile.isFile)
        assertEquals(null, PmTilesArchiveHeaderParser.inspect(targetFile).error)
    }

    @Test
    fun asksForLocalPickerWhenCatalogHasNoCoveringPack() {
        val directory = Files.createTempDirectory("pmtiles-remote-import-empty").toFile()
        val coordinator = TrailMatePmTilesBasemapRemoteImportCoordinator(
            catalogApi = FakeCatalogApi(emptyList()),
            downloader = FakeDownloader(null)
        )

        val result = coordinator.importForRoute(
            routeBounds = PmTilesLatLngBounds(116.30, 39.80, 116.50, 40.00),
            routePackKey = "beijing-route",
            targetDirectory = directory
        )

        assertEquals(TrailMatePmTilesRemoteImportAction.OPEN_LOCAL_PICKER, result.action)
        assertEquals("未找到适配当前路线的服务端地图包，可选择本地 PMTiles 文件。", result.message)
        assertFalse(directory.resolve("beijing-route.pmtiles").exists())
    }

    @Test
    fun asksForLocalPickerWhenDownloadedPackFailsValidation() {
        val directory = Files.createTempDirectory("pmtiles-remote-import-invalid").toFile()
        val invalidFile = Files.createTempFile("not-pmtiles", ".pmtiles").toFile().apply {
            writeText("not a pmtiles archive")
        }
        val coordinator = TrailMatePmTilesBasemapRemoteImportCoordinator(
            catalogApi = FakeCatalogApi(listOf(catalogItem())),
            downloader = FakeDownloader(invalidFile)
        )

        val result = coordinator.importForRoute(
            routeBounds = PmTilesLatLngBounds(120.05, 30.10, 120.25, 30.35),
            routePackKey = "longjing-ridge",
            targetDirectory = directory
        )

        assertEquals(TrailMatePmTilesRemoteImportAction.OPEN_LOCAL_PICKER, result.action)
        assertEquals("服务端离线地图包校验未通过，可选择本地 PMTiles 文件。", result.message)
        assertFalse(directory.resolve("longjing-ridge.pmtiles").exists())
    }

    private class FakeCatalogApi(
        private val items: List<TrailMatePmTilesBasemapCatalogItemDto>
    ) : TrailMateOfflineBasemapCatalogApi {
        override fun listPmTilesBasemaps(
            routeBounds: PmTilesLatLngBounds
        ): TrailMateApiResult<List<TrailMatePmTilesBasemapCatalogItemDto>> =
            TrailMateApiResult.Success(items)
    }

    private class FakeDownloader(
        private val sourceFile: File?
    ) : TrailMatePmTilesBasemapFileDownloader {
        override fun downloadToFile(
            downloadUrl: String,
            targetFile: File
        ): TrailMateApiResult<File> {
            val source = sourceFile ?: return TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 404,
                    code = "HTTP_404",
                    message = "Not found.",
                    traceId = null
                )
            )
            source.copyTo(targetFile, overwrite = true)
            return TrailMateApiResult.Success(targetFile)
        }
    }

    private fun catalogItem(): TrailMatePmTilesBasemapCatalogItemDto =
        TrailMatePmTilesBasemapCatalogItemDto(
            packId = "pmtiles_hangzhou_westlake_osm_v1",
            regionName = "杭州市 · 西湖区",
            downloadUrl = "https://cdn.trailmate.local/offline-basemaps/hangzhou-westlake.pmtiles",
            sizeBytes = 120_000_000L,
            sha256 = null,
            tileType = "MVT",
            minZoom = 10,
            maxZoom = 14,
            minLongitude = 120.00,
            minLatitude = 30.05,
            maxLongitude = 120.30,
            maxLatitude = 30.40,
            attribution = "OpenStreetMap contributors",
            source = "OSM / Protomaps"
        )
}
