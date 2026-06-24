package com.trailmate.app.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateOfflineBasemapCatalogApiContractTest {
    @Test
    fun exposesPmTilesCatalogEndpoint() {
        assertEquals(
            "/offline-basemaps/pmtiles/catalog",
            TrailMateServerApiContract.Endpoints.offlineBasemapPmTilesCatalog
        )
    }

    @Test
    fun catalogDtoCarriesServerOwnedPmTilesMetadata() {
        val dto = TrailMatePmTilesBasemapCatalogItemDto(
            packId = "pmtiles_hangzhou_westlake_osm_v1",
            regionName = "杭州市 · 西湖区",
            downloadUrl = "https://cdn.trailmate.local/offline-basemaps/hangzhou-westlake.pmtiles",
            sizeBytes = 120_000_000L,
            sha256 = "abc123",
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

        assertEquals("pmtiles_hangzhou_westlake_osm_v1", dto.packId)
        assertEquals("杭州市 · 西湖区", dto.regionName)
        assertEquals("MVT", dto.tileType)
        assertEquals(120_000_000L, dto.sizeBytes)
    }
}
