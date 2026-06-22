package com.trailmate.app.core.map

enum class TrailMapSurfaceMode {
    LOCAL_CANVAS,
    AMAP_MAP_VIEW
}

object TrailMapSurfaceSelector {
    fun select(readiness: TrailMapReadiness): TrailMapSurfaceMode =
        if (readiness.provider == TrailMapProvider.AMAP_SDK) {
            TrailMapSurfaceMode.AMAP_MAP_VIEW
        } else {
            TrailMapSurfaceMode.LOCAL_CANVAS
        }
}
