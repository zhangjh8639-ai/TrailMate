package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMapSurfaceSelectorTest {
    @Test
    fun selectsAmapMapViewForAmapProviderWithoutProductionClaim() {
        val readiness = TrailMapReadiness(
            provider = TrailMapProvider.AMAP_SDK,
            title = "在线底图",
            caption = "在线底图可用，建议出发前保存路线包。",
            layerChips = listOf("GPX 折线", "检查点", "在线底图"),
            actionLabel = "保存路线包",
            isProductionMapReady = false
        )

        assertEquals(TrailMapSurfaceMode.AMAP_MAP_VIEW, TrailMapSurfaceSelector.select(readiness))
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
