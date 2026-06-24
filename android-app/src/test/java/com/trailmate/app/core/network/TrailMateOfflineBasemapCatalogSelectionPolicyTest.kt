package com.trailmate.app.core.network

import com.trailmate.app.core.map.PmTilesLatLngBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrailMateOfflineBasemapCatalogSelectionPolicyTest {
    @Test
    fun selectsFirstVectorPackCoveringRouteBounds() {
        val selection = TrailMateOfflineBasemapCatalogSelectionPolicy.selectForRoute(
            routeBounds = PmTilesLatLngBounds(
                minLongitude = 120.05,
                minLatitude = 30.10,
                maxLongitude = 120.25,
                maxLatitude = 30.35
            ),
            catalog = listOf(
                catalogItem(
                    packId = "raster_unsupported",
                    tileType = "RASTER",
                    maxLongitude = 120.30,
                    maxLatitude = 30.40
                ),
                catalogItem(
                    packId = "pmtiles_hangzhou_westlake_osm_v1",
                    tileType = "MVT",
                    maxLongitude = 120.30,
                    maxLatitude = 30.40
                )
            )
        )

        assertEquals("pmtiles_hangzhou_westlake_osm_v1", selection?.packId)
    }

    @Test
    fun returnsNullWhenNoPackIntersectsRouteBounds() {
        val selection = TrailMateOfflineBasemapCatalogSelectionPolicy.selectForRoute(
            routeBounds = PmTilesLatLngBounds(
                minLongitude = 116.30,
                minLatitude = 39.80,
                maxLongitude = 116.50,
                maxLatitude = 40.00
            ),
            catalog = listOf(catalogItem(packId = "pmtiles_hangzhou_westlake_osm_v1"))
        )

        assertNull(selection)
    }

    @Test
    fun returnsNullWhenCatalogPackBoundsAreInvalid() {
        val selection = TrailMateOfflineBasemapCatalogSelectionPolicy.selectForRoute(
            routeBounds = PmTilesLatLngBounds(
                minLongitude = 120.05,
                minLatitude = 30.10,
                maxLongitude = 120.25,
                maxLatitude = 30.35
            ),
            catalog = listOf(catalogItem(packId = "invalid_bounds", maxLongitude = 119.00))
        )

        assertNull(selection)
    }

    private fun catalogItem(
        packId: String,
        tileType: String = "MVT",
        maxLongitude: Double = 120.30,
        maxLatitude: Double = 30.40
    ): TrailMatePmTilesBasemapCatalogItemDto =
        TrailMatePmTilesBasemapCatalogItemDto(
            packId = packId,
            regionName = "杭州市 · 西湖区",
            downloadUrl = "https://cdn.trailmate.local/offline-basemaps/hangzhou-westlake.pmtiles",
            sizeBytes = 120_000_000L,
            sha256 = null,
            tileType = tileType,
            minZoom = 10,
            maxZoom = 14,
            minLongitude = 120.00,
            minLatitude = 30.05,
            maxLongitude = maxLongitude,
            maxLatitude = maxLatitude,
            attribution = "OpenStreetMap contributors",
            source = "OSM / Protomaps"
        )
}
