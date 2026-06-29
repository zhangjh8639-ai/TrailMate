package com.trailmate.app.core.map

import java.io.File

object MapLibrePmTilesRouteStylePolicy {
    fun buildStyleJson(
        pmTilesFile: File,
        styleAssetManifest: MapLibrePmTilesStyleAssetManifest = MapLibrePmTilesStyleAssetManifest.unavailable()
    ): String =
        MapLibrePmTilesStyleFactory.buildStyleJson(
            file = pmTilesFile,
            styleAssets = MapLibrePmTilesStyleAssetReadinessEngine.resolve(styleAssetManifest)
        )
}
