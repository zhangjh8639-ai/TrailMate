package com.trailmate.app.core.map

enum class TrailMapSurfaceMode {
    LOCAL_CANVAS,
    MAPLIBRE_PMTILES
}

object TrailMapSurfaceSelector {
    fun select(readiness: TrailMapReadiness): TrailMapSurfaceMode =
        when (readiness.provider) {
            TrailMapProvider.MAPLIBRE_PMTILES -> TrailMapSurfaceMode.MAPLIBRE_PMTILES
            TrailMapProvider.AMAP_SDK,
            TrailMapProvider.LOCAL_GPX_PREVIEW -> TrailMapSurfaceMode.LOCAL_CANVAS
        }
}
