package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmapLaunchDiagnosticsEngineTest {
    @Test
    fun blocksProductionMapWhenAndroidKeyIsMissing() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = false,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = false,
            routePointCount = 128,
            gpsEnabled = true
        )

        assertEquals("高德上线检查", diagnostics.title)
        assertEquals("待补齐", diagnostics.statusLabel)
        assertEquals("com.trailmate.app", diagnostics.packageName)
        assertFalse(diagnostics.revealsApiKey)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "Android Key" }.status
        )
        assertEquals("待配置", diagnostics.items.first { it.label == "Android Key" }.value)
        assertEquals("Package/SHA1", diagnostics.items.first { it.label == "Package/SHA1" }.label)
        assertEquals("待绑定", diagnostics.items.first { it.label == "Package/SHA1" }.value)
        assertTrue(diagnostics.caption.contains("TRAILMATE_AMAP_API_KEY"))
    }

    @Test
    fun keepsProductionStatusPendingUntilOfflineBaseMapsAreVerified() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            packageSha1 = "DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true
        )

        assertEquals("待离线底图", diagnostics.statusLabel)
        assertEquals(
            AmapLaunchDiagnosticStatus.MANUAL_CHECK,
            diagnostics.items.first { it.label == "Package/SHA1" }.status
        )
        assertEquals(
            "DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88",
            diagnostics.items.first { it.label == "Package/SHA1" }.value
        )
        assertEquals("可验证", diagnostics.items.first { it.label == "在线底图" }.value)
        assertEquals("管理器可打开", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.MANUAL_CHECK,
            diagnostics.items.first { it.label == "离线底图" }.status
        )
        assertEquals("打开高德离线底图管理", diagnostics.offlineMapActionLabel)
        assertTrue(diagnostics.caption.contains("离线底图"))
        assertFalse(diagnostics.revealsApiKey)
    }

    @Test
    fun fallsBackToManualPackageSha1CheckWhenRuntimeSha1IsUnavailable() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true
        )

        assertEquals("控制台核验", diagnostics.items.first { it.label == "Package/SHA1" }.value)
        assertFalse(diagnostics.revealsApiKey)
    }

    @Test
    fun marksReadyForDeviceQaWhenOfflineBaseMapsAreVerified() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineBaseMapDownloadedRegionCount = 2,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = true
        )

        assertEquals("可真机验证", diagnostics.statusLabel)
        assertEquals("已覆盖当前路线", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals("已断网验证", diagnostics.items.first { it.label == "断网瓦片" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.READY,
            diagnostics.items.first { it.label == "离线底图" }.status
        )
        assertTrue(diagnostics.caption.contains("在线底图"))
        assertTrue(diagnostics.caption.contains("离线底图"))
    }

    @Test
    fun reportsMissingPreciseLocationPermissionBeforeDeviceQa() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            preciseLocationPermissionGranted = false,
            gpsProviderEnabled = true,
            offlineBaseMapDownloadedRegionCount = 2,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = true
        )

        assertEquals("待定位授权", diagnostics.statusLabel)
        assertEquals("待授权", diagnostics.items.first { it.label == "精确定位" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "精确定位" }.status
        )
        assertEquals("已开启", diagnostics.items.first { it.label == "系统 GPS" }.value)
        assertTrue(diagnostics.caption.contains("精确定位"))
    }

    @Test
    fun reportsDisabledSystemLocationSeparatelyFromPrecisePermission() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            preciseLocationPermissionGranted = true,
            gpsProviderEnabled = false,
            offlineBaseMapDownloadedRegionCount = 2,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = true
        )

        assertEquals("待开启系统定位", diagnostics.statusLabel)
        assertEquals("已授权", diagnostics.items.first { it.label == "精确定位" }.value)
        assertEquals("待开启", diagnostics.items.first { it.label == "系统 GPS" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "系统 GPS" }.status
        )
        assertTrue(diagnostics.caption.contains("系统定位"))
        assertTrue(!diagnostics.statusLabel.contains("GPS"))
    }

    @Test
    fun doesNotMarkLocationCalibrationReadyUntilReliableFixExists() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            preciseLocationPermissionGranted = true,
            gpsProviderEnabled = true,
            locationReadyForFieldUse = false,
            offlineBaseMapDownloadedRegionCount = 0,
            offlineDownloadNetworkValidated = true
        )

        assertEquals("待离线底图", diagnostics.statusLabel)
        assertEquals("等待可靠位置", diagnostics.items.first { it.label == "定位校准" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "定位校准" }.status
        )
    }

    @Test
    fun keepsDeviceQaPendingUntilOfflineTilesAreVerifiedWithoutNetwork() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineBaseMapDownloadedRegionCount = 2,
            offlineBaseMapCoversTargetRoute = true
        )

        assertEquals("待断网验证", diagnostics.statusLabel)
        assertEquals("已覆盖当前路线", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.MANUAL_CHECK,
            diagnostics.items.first { it.label == "离线底图" }.status
        )
        assertEquals("待断网验证", diagnostics.items.first { it.label == "断网瓦片" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.MANUAL_CHECK,
            diagnostics.items.first { it.label == "断网瓦片" }.status
        )
        assertTrue(diagnostics.caption.contains("断网"))
    }

    @Test
    fun reportsNoDownloadedOfflineBaseMapsWithoutBlockingManagerEntry() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = true,
            offlineBaseMapDownloadedRegionCount = 0
        )

        assertEquals("待离线底图", diagnostics.statusLabel)
        assertEquals("已验证", diagnostics.items.first { it.label == "下载网络" }.value)
        assertEquals("未下载", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "离线底图" }.status
        )
        assertEquals("打开高德离线底图管理", diagnostics.offlineMapActionLabel)
    }

    @Test
    fun reportsPendingOfflineBaseMapDownloadSeparatelyFromNeverDownloaded() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = true,
            offlineBaseMapDownloadedRegionCount = 0,
            offlineBaseMapPendingRegionCount = 1,
            offlineBaseMapPendingRegionLabels = listOf("杭州市 35%")
        )

        assertEquals("待离线底图", diagnostics.statusLabel)
        assertEquals("下载中 1 个区域，待完成", diagnostics.items.first { it.label == "离线底图" }.value)
        assertTrue(diagnostics.caption.contains("杭州市 35%"))
        assertTrue(diagnostics.caption.contains("尚未完成"))
        assertEquals("打开高德离线底图管理", diagnostics.offlineMapActionLabel)
    }

    @Test
    fun showsRouteCoordinateHintWhenOfflineBaseMapRegionIsUnknown() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = true,
            offlineBaseMapDownloadedRegionCount = 0,
            targetOfflineBaseMapHint = "路线中点 30.2000, 120.2000（城市待确认）"
        )

        assertEquals("待离线底图", diagnostics.statusLabel)
        assertTrue(diagnostics.caption.contains("路线中点 30.2000, 120.2000（城市待确认）"))
        assertTrue(diagnostics.caption.contains("确认目标城市"))
        assertEquals("打开高德离线底图管理", diagnostics.offlineMapActionLabel)
    }

    @Test
    fun asksTesterToOpenRouteMapWhenOnlineBaseMapHasNotRenderedYet() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = false,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = true,
            offlineBaseMapDownloadedRegionCount = 0
        )

        assertEquals("待打开路线地图验证", diagnostics.items.first { it.label == "在线底图" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "在线底图" }.status
        )
    }

    @Test
    fun includesTargetRegionInOfflineMapActionWhenKnown() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = true,
            offlineBaseMapDownloadedRegionCount = 0,
            targetOfflineBaseMapRegionLabel = "杭州市"
        )

        assertEquals("待离线底图", diagnostics.statusLabel)
        assertEquals("打开高德离线底图管理（杭州市）", diagnostics.offlineMapActionLabel)
    }

    @Test
    fun reportsUnvalidatedNetworkBeforeOfflineBaseMapDownload() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = false,
            offlineBaseMapDownloadedRegionCount = 0
        )

        assertEquals("待下载网络", diagnostics.statusLabel)
        assertEquals("未验证", diagnostics.items.first { it.label == "下载网络" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "下载网络" }.status
        )
        assertTrue(diagnostics.caption.contains("系统网络验证"))
        assertEquals(null, diagnostics.offlineMapActionLabel)
        assertEquals("打开网络设置", diagnostics.networkSettingsActionLabel)
    }

    @Test
    fun hidesOfflineManagerWhenNetworkIsUnvalidatedAndDownloadedRegionStatusIsUnknown() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = false,
            offlineBaseMapDownloadedRegionCount = null
        )

        assertEquals("待下载网络", diagnostics.statusLabel)
        assertEquals("未验证", diagnostics.items.first { it.label == "下载网络" }.value)
        assertEquals("待网络", diagnostics.items.first { it.label == "离线底图" }.value)
        assertTrue(diagnostics.caption.contains("系统网络验证"))
        assertEquals(null, diagnostics.offlineMapActionLabel)
        assertEquals("打开网络设置", diagnostics.networkSettingsActionLabel)
    }

    @Test
    fun repairsNetworkBeforeDownloadingMissingTargetOfflineBaseMap() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = false,
            offlineBaseMapDownloadedRegionCount = 2,
            offlineBaseMapCoversTargetRoute = false
        )

        assertEquals("待下载网络", diagnostics.statusLabel)
        assertEquals("待网络", diagnostics.items.first { it.label == "离线底图" }.value)
        assertTrue(diagnostics.caption.contains("系统网络验证"))
        assertEquals(null, diagnostics.offlineMapActionLabel)
        assertEquals("打开网络设置", diagnostics.networkSettingsActionLabel)
    }

    @Test
    fun doesNotPromptNetworkRepairWhenCoveredOfflineBaseMapNeedsNetworkDisabledTileProof() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineDownloadNetworkValidated = false,
            offlineBaseMapDownloadedRegionCount = 2,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = false
        )

        assertEquals("待断网验证", diagnostics.statusLabel)
        assertEquals("已覆盖当前路线", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals("无需下载", diagnostics.items.first { it.label == "下载网络" }.value)
        assertEquals("待断网验证", diagnostics.items.first { it.label == "断网瓦片" }.value)
        assertTrue(diagnostics.caption.contains("断网"))
        assertEquals(null, diagnostics.networkSettingsActionLabel)
    }

    @Test
    fun keepsDeviceQaPendingWhenDownloadedOfflineBaseMapsDoNotCoverTargetRoute() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true,
            offlineBaseMapDownloadedRegionCount = 2,
            offlineBaseMapCoversTargetRoute = false
        )

        assertEquals("待离线底图", diagnostics.statusLabel)
        assertEquals("已下载 2 个区域，待匹配当前路线", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "离线底图" }.status
        )
        assertTrue(diagnostics.caption.contains("当前路线"))
        assertEquals("打开高德离线底图管理", diagnostics.offlineMapActionLabel)
    }

    @Test
    fun blocksOfflineMapEntryUntilPrivacyConsentIsAccepted() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = false,
            productionMapReady = false,
            routePointCount = 128,
            gpsEnabled = false
        )

        assertEquals("待授权", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals(null, diagnostics.offlineMapActionLabel)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "离线底图" }.status
        )
    }

    @Test
    fun blocksOfflineMapEntryUntilOfflineActivityIsRegistered() {
        val diagnostics = AmapLaunchDiagnosticsEngine.build(
            packageName = "com.trailmate.app",
            hasAmapKey = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true,
            offlineMapActivityRegistered = false,
            productionMapReady = true,
            routePointCount = 128,
            gpsEnabled = true
        )

        assertEquals("待注册", diagnostics.items.first { it.label == "离线底图" }.value)
        assertEquals(null, diagnostics.offlineMapActionLabel)
        assertEquals(
            AmapLaunchDiagnosticStatus.NEEDS_ACTION,
            diagnostics.items.first { it.label == "离线底图" }.status
        )
        assertTrue(diagnostics.caption.contains("OfflineMapActivity"))
    }
}
