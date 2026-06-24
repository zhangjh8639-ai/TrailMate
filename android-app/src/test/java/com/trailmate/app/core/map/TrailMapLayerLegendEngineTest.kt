package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMapLayerLegendEngineTest {
    @Test
    fun summarizesLocalPreviewLayersBeforeRecording() {
        val legend = TrailMapLayerLegendEngine.build(
            readiness = TrailMapReadiness(
                provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
                title = "本地路线预览",
                caption = "在线底图暂不可用，当前使用本地 GPX 路线预览。",
                layerChips = listOf("GPX 折线", "检查点", "7 点"),
                actionLabel = "使用本地路线",
                isProductionMapReady = false
            ),
            routePointCount = 7,
            checkpointCount = 5,
            recordedTrackPointCount = 0,
            showUserLocation = false
        )

        assertEquals("地图图层", legend.title)
        assertEquals(
            listOf("计划路线", "路线提示点", "实走轨迹", "当前位置", "底图"),
            legend.items.map { it.label }
        )
        assertEquals("7 点", legend.items[0].value)
        assertEquals(TrailMapLayerLegendItemStatus.READY, legend.items[0].status)
        assertEquals("5 个", legend.items[1].value)
        assertEquals("未记录", legend.items[2].value)
        assertEquals(TrailMapLayerLegendItemStatus.INACTIVE, legend.items[2].status)
        assertEquals("未开启", legend.items[3].value)
        assertEquals("本地预览", legend.items[4].value)
    }

    @Test
    fun marksRecordedTrackAndAmapBaseMapWhenAvailable() {
        val legend = TrailMapLayerLegendEngine.build(
            readiness = TrailMapReadiness(
                provider = TrailMapProvider.AMAP_SDK,
                title = "在线底图",
                caption = "在线底图可用，离线路线已保存。",
                layerChips = listOf("GPX 折线", "检查点", "7 点", "在线底图"),
                actionLabel = "查看路线辅助",
                isProductionMapReady = false
            ),
            routePointCount = 7,
            checkpointCount = 5,
            recordedTrackPointCount = 2,
            showUserLocation = true
        )

        assertEquals("2 点", legend.items.first { it.label == "实走轨迹" }.value)
        assertEquals(TrailMapLayerLegendItemStatus.READY, legend.items.first { it.label == "实走轨迹" }.status)
        assertEquals("已开启", legend.items.first { it.label == "当前位置" }.value)
        assertEquals("在线底图", legend.items.first { it.label == "底图" }.value)
        assertEquals(TrailMapLayerLegendItemStatus.READY, legend.items.first { it.label == "底图" }.status)
    }

    @Test
    fun blocksRouteDependentLayersWhenGeometryIsMissing() {
        val legend = TrailMapLayerLegendEngine.build(
            readiness = TrailMapReadiness(
                provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
                title = "路线几何缺失",
                caption = "当前 GPX 缺少可绘制轨迹点，请重新导入包含轨迹点的 GPX。",
                layerChips = listOf("GPX 折线", "检查点", "无路线点"),
                actionLabel = "重新导入 GPX",
                isProductionMapReady = false
            ),
            routePointCount = 1,
            checkpointCount = 5,
            recordedTrackPointCount = 0,
            showUserLocation = false
        )

        assertEquals("不可用", legend.items.first { it.label == "计划路线" }.value)
        assertEquals(TrailMapLayerLegendItemStatus.BLOCKED, legend.items.first { it.label == "计划路线" }.status)
        assertEquals("等待路线", legend.items.first { it.label == "路线提示点" }.value)
        assertEquals(TrailMapLayerLegendItemStatus.BLOCKED, legend.items.first { it.label == "路线提示点" }.status)
    }
}
