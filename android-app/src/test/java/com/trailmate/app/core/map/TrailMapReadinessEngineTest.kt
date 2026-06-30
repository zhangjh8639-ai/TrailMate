package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMapReadinessEngineTest {
    @Test
    fun prefersMapLibrePmtilesWhenOfflineBasemapPackIsReady() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            mapLibreRuntimeAvailable = true,
            pmTilesBasemapPackReady = true,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = true,
            routePointCount = 1858
        )

        assertEquals(TrailMapProvider.MAPLIBRE_PMTILES, readiness.provider)
        assertEquals("离线底图", readiness.title)
        assertEquals("查看路线辅助", readiness.actionLabel)
        assertTrue(readiness.caption.contains("本地离线底图"))
        assertTrue(readiness.layerChips.contains("PMTiles 底图"))
        assertEquals("离线底图已就绪", readiness.setupHint.title)
        assertEquals("离线可用", readiness.setupHint.statusLabel)
        assertEquals("已准备", readiness.setupSteps.first { it.label == "离线底图" }.value)
        assertTrue(readiness.isProductionMapReady)
    }

    @Test
    fun reportsMissingPmtilesStyleAssetsWithoutBlockingReadyBasemap() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            mapLibreRuntimeAvailable = true,
            pmTilesBasemapPackReady = true,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = true,
            routePointCount = 1858,
            pmTilesStyleAssetReadiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
                MapLibrePmTilesStyleAssetManifest.unavailable()
            )
        )

        assertEquals(TrailMapProvider.MAPLIBRE_PMTILES, readiness.provider)
        assertTrue(readiness.isProductionMapReady)
        assertEquals("待补齐", readiness.setupSteps.first { it.label == "地图标注" }.value)
        assertEquals(
            TrailMapReadinessStepStatus.NEEDS_ACTION,
            readiness.setupSteps.first { it.label == "地图标注" }.status
        )
    }

    @Test
    fun reportsReadyPmtilesStyleAssetsWhenGlyphsAndSpritesAreComplete() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            mapLibreRuntimeAvailable = true,
            pmTilesBasemapPackReady = true,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = true,
            routePointCount = 1858,
            pmTilesStyleAssetReadiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
                MapLibrePmTilesStyleAssetManifest(
                    glyphsUrl = "asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf",
                    spriteJsonUrl = "asset://trailmate/maplibre/protomaps/sprite.json",
                    spriteImageUrl = "asset://trailmate/maplibre/protomaps/sprite.png"
                )
            )
        )

        assertEquals(TrailMapProvider.MAPLIBRE_PMTILES, readiness.provider)
        assertEquals("已就绪", readiness.setupSteps.first { it.label == "地图标注" }.value)
        assertEquals(
            TrailMapReadinessStepStatus.READY,
            readiness.setupSteps.first { it.label == "地图标注" }.status
        )
    }

    @Test
    fun doesNotReportPmtilesStyleAssetsWhenRouteGeometryForcesLocalPreview() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            mapLibreRuntimeAvailable = true,
            pmTilesBasemapPackReady = true,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = true,
            routePointCount = 1,
            pmTilesStyleAssetReadiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
                MapLibrePmTilesStyleAssetManifest.unavailable()
            )
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertFalse(readiness.setupSteps.any { it.label == "地图标注" })
    }

    @Test
    fun fallsBackToLocalPreviewWhenPmtilesPackIsMissing() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            mapLibreRuntimeAvailable = true,
            pmTilesBasemapPackReady = false,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = true,
            routePointCount = 1858
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertEquals("定位与记录", readiness.title)
        assertTrue(readiness.caption.contains("离线路线已保存"))
        assertEquals("准备离线底图", readiness.actionLabel)
        assertEquals("离线底图待准备", readiness.setupHint.title)
        assertEquals("待准备", readiness.setupSteps.first { it.label == "离线底图" }.value)
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun fallsBackToLocalPreviewWhenMapLibreRuntimeIsMissing() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            mapLibreRuntimeAvailable = false,
            pmTilesBasemapPackReady = true,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = true,
            routePointCount = 1858
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertTrue(readiness.caption.contains("离线路线已保存"))
        assertTrue(readiness.setupHint.caption.contains("MapLibre 渲染器未就绪"))
        assertEquals("待接入", readiness.setupSteps.first { it.label == "离线底图" }.value)
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun presentsAmapLoadingBeforeFirstTileCallback() {
        val presentation = TrailMapLoadingPresentationEngine.present(
            provider = TrailMapProvider.AMAP_SDK,
            mapLoaded = false,
            elapsedMillis = 800L
        )

        assertTrue(presentation.showOverlay)
        assertEquals("在线底图加载中", presentation.title)
        assertTrue(presentation.caption.contains("路线和操作仍可使用"))
    }

    @Test
    fun presentsSlowAmapFallbackWithoutBlockingRouteActions() {
        val presentation = TrailMapLoadingPresentationEngine.present(
            provider = TrailMapProvider.AMAP_SDK,
            mapLoaded = false,
            elapsedMillis = 3_600L
        )

        assertTrue(presentation.showOverlay)
        assertEquals("底图加载较慢", presentation.title)
        assertTrue(presentation.caption.contains("可继续查看本地路线"))
        assertFalse(presentation.blocksRouteActions)
    }

    @Test
    fun hidesAmapLoadingOverlayAfterMapLoadedOrWhenUsingLocalCanvas() {
        val loaded = TrailMapLoadingPresentationEngine.present(
            provider = TrailMapProvider.AMAP_SDK,
            mapLoaded = true,
            elapsedMillis = 3_600L
        )
        val local = TrailMapLoadingPresentationEngine.present(
            provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
            mapLoaded = false,
            elapsedMillis = 3_600L
        )

        assertFalse(loaded.showOverlay)
        assertFalse(local.showOverlay)
    }

    @Test
    fun usesLocalPreviewWhenAmapIsNotConfigured() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            offlineRoutePackReady = false,
            gpsEnabled = false,
            routePointCount = 128
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertEquals("本地路线预览", readiness.title)
        assertEquals("使用本地路线", readiness.actionLabel)
        assertTrue(readiness.caption.contains("MapLibre 渲染器待接入"))
        assertEquals("当前使用本地路线", readiness.setupHint.title)
        assertEquals("本地预览", readiness.setupHint.statusLabel)
        assertFalse(readiness.setupHint.caption.contains("Package/SHA1"))
        assertFalse(readiness.setupHint.caption.contains("TRAILMATE_AMAP_API_KEY"))
        assertTrue(readiness.layerChips.contains("GPX 折线"))
        assertTrue(readiness.layerChips.contains("检查点"))
        assertEquals(
            listOf("路线", "离线路线", "GPS", "离线底图"),
            readiness.setupSteps.map { it.label }
        )
        assertEquals(TrailMapReadinessStepStatus.READY, readiness.setupSteps[0].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[1].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[2].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[3].status)
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun usesExplicitOfflineRouteAndBasemapStepLabels() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            mapLibreRuntimeAvailable = true,
            pmTilesBasemapPackReady = false,
            offlineRoutePackReady = false,
            gpsEnabled = false,
            routePointCount = 128
        )

        assertEquals(
            listOf("路线", "离线路线", "GPS", "离线底图"),
            readiness.setupSteps.map { it.label }
        )
        assertEquals("待保存", readiness.setupSteps.first { it.label == "离线路线" }.value)
        assertEquals("待准备", readiness.setupSteps.first { it.label == "离线底图" }.value)
    }

    @Test
    fun prioritizesReimportWhenRouteGeometryIsMissing() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            routePointCount = 1
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertEquals("路线几何缺失", readiness.title)
        assertEquals("重新导入 GPX", readiness.actionLabel)
        assertTrue(readiness.caption.contains("包含轨迹点"))
        assertEquals(TrailMapReadinessStepStatus.BLOCKED, readiness.setupSteps[0].status)
        assertEquals("缺少轨迹点", readiness.setupSteps[0].value)
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun marksFieldModeWhenGpsAndOfflinePackAreReady() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = true,
            routePointCount = 1858
        )

        assertEquals("定位与记录", readiness.title)
        assertEquals("查看路线辅助", readiness.actionLabel)
        assertTrue(readiness.caption.contains("离线路线"))
        assertTrue(readiness.caption.contains("当前位置"))
        assertTrue(readiness.layerChips.contains("1858 点"))
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun treatsStartedLocationRequestAsCalibrationUntilReliableFixExists() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = false,
            offlineRoutePackReady = true,
            gpsEnabled = true,
            locationReadyForFieldUse = false,
            routePointCount = 1858
        )

        assertEquals("本地路线预览", readiness.title)
        assertEquals("使用本地路线", readiness.actionLabel)
        assertTrue(readiness.caption.contains("MapLibre 渲染器待接入"))
        assertEquals("校准中", readiness.setupSteps.first { it.label == "GPS" }.value)
        assertEquals(
            TrailMapReadinessStepStatus.NEEDS_ACTION,
            readiness.setupSteps.first { it.label == "GPS" }.status
        )
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun keepsLocalPreviewWhenAmapKeyExistsButPrivacyConsentIsMissing() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = false,
            offlineRoutePackReady = false,
            gpsEnabled = false,
            routePointCount = 128
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertEquals("本地路线预览", readiness.title)
        assertEquals("使用本地路线", readiness.actionLabel)
        assertTrue(readiness.caption.contains("MapLibre 渲染器待接入"))
        assertEquals("当前使用本地路线", readiness.setupHint.title)
        assertTrue(readiness.setupHint.caption.contains("MapLibre 渲染器未就绪"))
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun keepsLocalPreviewWhenAmapSdkIsNotLinkedYet() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = true,
            amapSdkAvailable = false,
            amapPrivacyConsentAccepted = true,
            offlineRoutePackReady = false,
            gpsEnabled = false,
            routePointCount = 128
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertEquals("本地路线预览", readiness.title)
        assertTrue(readiness.caption.contains("MapLibre 渲染器待接入"))
        assertEquals("当前使用本地路线", readiness.setupHint.title)
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun keepsLocalPreviewEvenWhenAmapIsAvailableBecausePmtilesIsRequiredForBasemap() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            offlineRoutePackReady = true,
            gpsEnabled = false,
            routePointCount = 128
        )

        assertEquals(TrailMapProvider.LOCAL_GPX_PREVIEW, readiness.provider)
        assertEquals("本地路线预览", readiness.title)
        assertTrue(readiness.caption.contains("MapLibre 渲染器待接入"))
        assertEquals("当前使用本地路线", readiness.setupHint.title)
        assertEquals("本地预览", readiness.setupHint.statusLabel)
        assertFalse(readiness.setupHint.caption.contains("生产"))
        assertTrue(readiness.setupHint.caption.contains("MapLibre"))
        assertFalse(readiness.layerChips.contains("在线底图"))
        assertEquals(TrailMapReadinessStepStatus.READY, readiness.setupSteps[0].status)
        assertEquals(TrailMapReadinessStepStatus.READY, readiness.setupSteps[1].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[2].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[3].status)
        assertFalse(readiness.isProductionMapReady)
    }
}
