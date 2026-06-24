package com.trailmate.app.core.network

import com.trailmate.app.core.map.PmTilesLatLngBounds

object TrailMateOfflineBasemapCatalogSelectionPolicy {
    fun selectForRoute(
        routeBounds: PmTilesLatLngBounds,
        catalog: List<TrailMatePmTilesBasemapCatalogItemDto>
    ): TrailMatePmTilesBasemapCatalogItemDto? =
        catalog.firstOrNull { item ->
            val itemBounds = item.bounds()
            item.tileType.equals("MVT", ignoreCase = true) &&
                item.downloadUrl.isNotBlank() &&
                itemBounds.isValid() &&
                itemBounds.intersects(routeBounds)
        }

    private fun TrailMatePmTilesBasemapCatalogItemDto.bounds(): PmTilesLatLngBounds =
        PmTilesLatLngBounds(
            minLongitude = minLongitude,
            minLatitude = minLatitude,
            maxLongitude = maxLongitude,
            maxLatitude = maxLatitude
        )
}
