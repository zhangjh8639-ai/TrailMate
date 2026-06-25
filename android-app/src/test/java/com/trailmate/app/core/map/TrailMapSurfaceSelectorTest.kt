package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMapSurfaceSelectorTest {
    @Test
    fun selectsMapLibreSurfaceForPmtilesProvider() {
        val readiness = TrailMapReadiness(
            provider = TrailMapProvider.MAPLIBRE_PMTILES,
            title = "离线地图包",
            caption = "PMTiles 本地离线地图包已就绪。",
            layerChips = listOf("GPX 折线", "检查点", "PMTiles 底图"),
            actionLabel = "查看路线辅助",
            isProductionMapReady = true
        )

        assertEquals(TrailMapSurfaceMode.MAPLIBRE_PMTILES, TrailMapSurfaceSelector.select(readiness))
    }

    @Test
    fun keepsLocalCanvasForAmapProviderBecausePmtilesOwnsOfflineBasemap() {
        val readiness = TrailMapReadiness(
            provider = TrailMapProvider.AMAP_SDK,
            title = "本地路线预览",
            caption = "PMTiles 离线地图包待导入，当前使用本地 GPX 路线预览。",
            layerChips = listOf("GPX 折线", "检查点"),
            actionLabel = "使用本地路线",
            isProductionMapReady = false
        )

        assertEquals(TrailMapSurfaceMode.LOCAL_CANVAS, TrailMapSurfaceSelector.select(readiness))
    }

    @Test
    fun keepsLocalCanvasWhenProviderIsLocalPreview() {
        val readiness = TrailMapReadiness(
            provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
            title = "本地路线预览",
            caption = "在线底图未在首次设置中启用，当前使用本地 GPX 路线预览。",
            layerChips = listOf("GPX 折线", "检查点"),
            actionLabel = "使用本地路线",
            isProductionMapReady = false
        )

        assertEquals(TrailMapSurfaceMode.LOCAL_CANVAS, TrailMapSurfaceSelector.select(readiness))
    }
}
