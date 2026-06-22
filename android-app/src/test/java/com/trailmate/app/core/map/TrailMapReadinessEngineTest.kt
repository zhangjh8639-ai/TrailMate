package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMapReadinessEngineTest {
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
        assertTrue(readiness.caption.contains("在线底图暂不可用"))
        assertEquals("当前使用本地路线", readiness.setupHint.title)
        assertEquals("本地预览", readiness.setupHint.statusLabel)
        assertFalse(readiness.setupHint.caption.contains("Package/SHA1"))
        assertFalse(readiness.setupHint.caption.contains("TRAILMATE_AMAP_API_KEY"))
        assertTrue(readiness.layerChips.contains("GPX 折线"))
        assertTrue(readiness.layerChips.contains("检查点"))
        assertEquals(
            listOf("路线", "离线", "定位", "底图"),
            readiness.setupSteps.map { it.label }
        )
        assertEquals(TrailMapReadinessStepStatus.READY, readiness.setupSteps[0].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[1].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[2].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[3].status)
        assertFalse(readiness.isProductionMapReady)
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

        assertEquals("实走轻导航", readiness.title)
        assertEquals("继续轻导航", readiness.actionLabel)
        assertTrue(readiness.caption.contains("离线路线包"))
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
        assertTrue(readiness.caption.contains("定位正在校准"))
        assertEquals("校准中", readiness.setupSteps.first { it.label == "定位" }.value)
        assertEquals(
            TrailMapReadinessStepStatus.NEEDS_ACTION,
            readiness.setupSteps.first { it.label == "定位" }.status
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
        assertTrue(readiness.caption.contains("在线底图未在首次设置中启用"))
        assertEquals("当前使用本地路线", readiness.setupHint.title)
        assertTrue(readiness.setupHint.caption.contains("避免实走中被授权流程打断"))
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
        assertTrue(readiness.caption.contains("在线底图暂不可用"))
        assertEquals("在线底图暂不可用", readiness.setupHint.title)
        assertFalse(readiness.isProductionMapReady)
    }

    @Test
    fun enablesAmapProviderWithoutClaimingOutdoorProductionReadiness() {
        val readiness = TrailMapReadinessEngine.resolve(
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            offlineRoutePackReady = true,
            gpsEnabled = false,
            routePointCount = 128
        )

        assertEquals(TrailMapProvider.AMAP_SDK, readiness.provider)
        assertEquals("在线底图", readiness.title)
        assertTrue(readiness.caption.contains("在线底图可用"))
        assertEquals("在线底图已就绪", readiness.setupHint.title)
        assertEquals("在线可用", readiness.setupHint.statusLabel)
        assertFalse(readiness.setupHint.caption.contains("生产"))
        assertTrue(readiness.setupHint.caption.contains("目标区域离线底图"))
        assertTrue(readiness.layerChips.contains("在线底图"))
        assertEquals(TrailMapReadinessStepStatus.READY, readiness.setupSteps[0].status)
        assertEquals(TrailMapReadinessStepStatus.READY, readiness.setupSteps[1].status)
        assertEquals(TrailMapReadinessStepStatus.NEEDS_ACTION, readiness.setupSteps[2].status)
        assertEquals(TrailMapReadinessStepStatus.READY, readiness.setupSteps[3].status)
        assertFalse(readiness.isProductionMapReady)
    }
}
