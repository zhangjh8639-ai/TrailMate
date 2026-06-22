package com.trailmate.app.core.map

import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateDeviceDiagnosticsReportFormatterTest {
    @Test
    fun formatsLaunchLocationAndOfflineDownloadEvidenceWithoutRevealingAmapKey() {
        val launchDiagnostics = AmapLaunchDiagnostics(
            title = "高德上线检查",
            statusLabel = "待下载网络",
            caption = "当前设备网络未通过 Android 系统网络验证。",
            packageName = "com.trailmate.app",
            revealsApiKey = false,
            offlineMapActionLabel = "打开高德离线底图管理",
            networkSettingsActionLabel = "打开网络设置",
            items = listOf(
                AmapLaunchDiagnosticItem(
                    label = "Package/SHA1",
                    value = "DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88",
                    status = AmapLaunchDiagnosticStatus.MANUAL_CHECK
                ),
                AmapLaunchDiagnosticItem(
                    label = "精确定位",
                    value = "待授权",
                    status = AmapLaunchDiagnosticStatus.NEEDS_ACTION
                ),
                AmapLaunchDiagnosticItem(
                    label = "下载网络",
                    value = "未验证",
                    status = AmapLaunchDiagnosticStatus.NEEDS_ACTION
                )
            )
        )
        val offlineDownloadDiagnostic = AmapOfflineDownloadQaDiagnostic(
            passed = false,
            statusLabel = "离线底图未完成",
            summary = "target=杭州市, package=com.trailmate.app, sha1=DF:CB, networkValidated=false",
            blockers = listOf("设备网络未通过系统验证"),
            nextActionLabel = "打开网络设置后重试下载",
            recoveryAction = AmapOfflineDownloadRecoveryAction.OPEN_NETWORK_SETTINGS,
            recoverySteps = listOf("切换到稳定 Wi-Fi 或蜂窝网络，关闭需要网页登录的热点。")
        )

        val report = TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = launchDiagnostics,
            deviceIdentity = TrailMateDeviceIdentity(
                androidSdkInt = 36,
                manufacturer = "Google",
                model = "Pixel 8",
                device = "shiba",
                appVersionName = "1.0-debug",
                appVersionCode = 12
            ),
            locationSnapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.PERMISSION_REQUIRED,
                latitude = null,
                longitude = null,
                elevationMeters = null,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = 1_000L
            ),
            offlineDownloadDiagnostic = offlineDownloadDiagnostic
        )

        assertTrue(report.contains("TrailMate 真机诊断"))
        assertTrue(report.contains("package=com.trailmate.app"))
        assertTrue(report.contains("androidSdk=36"))
        assertTrue(report.contains("manufacturer=Google"))
        assertTrue(report.contains("model=Pixel 8"))
        assertTrue(report.contains("device=shiba"))
        assertTrue(report.contains("appVersion=1.0-debug(12)"))
        assertTrue(report.contains("revealsApiKey=false"))
        assertTrue(report.contains("Package/SHA1=DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88 [MANUAL_CHECK]"))
        assertTrue(report.contains("精确定位=待授权 [NEEDS_ACTION]"))
        assertTrue(report.contains("locationStatus=PERMISSION_REQUIRED"))
        assertTrue(report.contains("locationRecoveryAction=REQUEST_PRECISE_LOCATION_PERMISSION"))
        assertTrue(report.contains("locationRecoveryStep=授予 Android 精确定位权限；仅大致位置不能用于户外导航和轨迹记录。"))
        assertTrue(report.contains("launchNextAction=打开网络设置"))
        assertTrue(report.contains("networkSettingsAction=打开网络设置"))
        assertTrue(report.contains("offlineBaseMapReason=GPX 只保存路线折线与检查点；目标区域离线底图用于在弱网或无网时保留道路、地名、水系、岔路等地图上下文，帮助判断撤退参照。"))
        assertTrue(report.contains("offlineDownload=离线底图未完成"))
        assertTrue(report.contains("blocker=设备网络未通过系统验证"))
        assertTrue(report.contains("nextAction=打开网络设置后重试下载"))
        assertTrue(report.contains("recoveryAction=OPEN_NETWORK_SETTINGS"))
        assertTrue(report.contains("recoveryStep=切换到稳定 Wi-Fi 或蜂窝网络，关闭需要网页登录的热点。"))
        assertFalse(report.contains("fake-amap-api-key-for-test"))
    }

    @Test
    fun formatsLocationRecoveryWhenSystemGpsProviderIsDisabled() {
        val report = TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = baseLaunchDiagnostics(
                locationItems = listOf(
                    AmapLaunchDiagnosticItem(
                        label = "精确定位",
                        value = "已授权",
                        status = AmapLaunchDiagnosticStatus.READY
                    ),
                    AmapLaunchDiagnosticItem(
                        label = "系统 GPS",
                        value = "待开启",
                        status = AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    )
                )
            ),
            locationSnapshot = emptyLocationSnapshot(TrailMateLocationStatus.PROVIDER_DISABLED)
        )

        assertTrue(report.contains("locationStatus=PROVIDER_DISABLED"))
        assertTrue(report.contains("locationRecoveryAction=OPEN_SYSTEM_LOCATION_SETTINGS"))
        assertTrue(report.contains("locationRecoveryStep=打开 Android 系统定位/GPS 后返回 TrailMate；应用会继续定位校准。"))
    }

    @Test
    fun formatsTargetOfflineBaseMapRegionAndVerificationStep() {
        val report = TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = AmapLaunchDiagnostics(
                title = "高德上线检查",
                statusLabel = "待离线底图",
                caption = "尚未检测到已下载的高德离线底图；出发前请下载目标区域离线底图。",
                packageName = "com.trailmate.app",
                revealsApiKey = false,
                offlineMapActionLabel = "打开高德离线底图管理（杭州市）",
                items = emptyList(),
                targetOfflineBaseMapRegionLabel = "杭州市"
            ),
            locationSnapshot = emptyLocationSnapshot(TrailMateLocationStatus.LOCATED)
        )

        assertTrue(report.contains("targetOfflineBaseMapRegion=杭州市"))
        assertTrue(report.contains("offlineBaseMapNextStep=下载目标区域离线底图后，开启飞行模式确认底图瓦片可见。"))
        assertTrue(report.contains("offlineMapAction=打开高德离线底图管理（杭州市）"))
    }

    @Test
    fun formatsFallbackTargetHintWhenOfflineBaseMapRegionIsUnknown() {
        val report = TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = AmapLaunchDiagnostics(
                title = "高德上线检查",
                statusLabel = "待离线底图",
                caption = "尚未检测到已下载的高德离线底图。",
                packageName = "com.trailmate.app",
                revealsApiKey = false,
                offlineMapActionLabel = "打开高德离线底图管理",
                items = emptyList(),
                targetOfflineBaseMapHint = "路线中点 30.2000, 120.2000（城市待确认）"
            ),
            locationSnapshot = emptyLocationSnapshot(TrailMateLocationStatus.LOCATED)
        )

        assertTrue(report.contains("targetOfflineBaseMapHint=路线中点 30.2000, 120.2000（城市待确认）"))
        assertTrue(report.contains("offlineBaseMapNextStep=先用路线中点确认目标城市，再下载覆盖整条路线的离线底图；下载后开启飞行模式确认底图瓦片可见。"))
        assertTrue(report.contains("offlineMapAction=打开高德离线底图管理"))
    }

    @Test
    fun formatsPendingOfflineBaseMapDownloadsForPhysicalDeviceDiagnostics() {
        val report = TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = AmapLaunchDiagnosticsEngine.build(
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
            ),
            locationSnapshot = emptyLocationSnapshot(TrailMateLocationStatus.LOCATED)
        )

        assertTrue(report.contains("offlineBaseMapPendingRegionCount=1"))
        assertTrue(report.contains("offlineBaseMapPendingRegion=杭州市 35%"))
        assertTrue(report.contains("离线底图=下载中 1 个区域，待完成 [MANUAL_CHECK]"))
    }

    @Test
    fun formatsOnlineBaseMapNextStepWhenRouteMapHasNotRenderedYet() {
        val report = TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = AmapLaunchDiagnostics(
                title = "高德上线检查",
                statusLabel = "待离线底图",
                caption = "请先打开路线地图验证高德在线底图。",
                packageName = "com.trailmate.app",
                revealsApiKey = false,
                offlineMapActionLabel = "打开高德离线底图管理",
                items = listOf(
                    AmapLaunchDiagnosticItem(
                        label = "在线底图",
                        value = "待打开路线地图验证",
                        status = AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    )
                )
            ),
            locationSnapshot = emptyLocationSnapshot(TrailMateLocationStatus.LOCATED)
        )

        assertTrue(report.contains("onlineBaseMapNextStep=打开路线页地图，确认高德在线底图、道路与路线同时可见。"))
    }

    @Test
    fun formatsLocationRecoveryWhenFirstGpsFixIsStillSearching() {
        val report = TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = baseLaunchDiagnostics(
                locationItems = listOf(
                    AmapLaunchDiagnosticItem(
                        label = "精确定位",
                        value = "已授权",
                        status = AmapLaunchDiagnosticStatus.READY
                    ),
                    AmapLaunchDiagnosticItem(
                        label = "系统 GPS",
                        value = "已开启",
                        status = AmapLaunchDiagnosticStatus.READY
                    ),
                    AmapLaunchDiagnosticItem(
                        label = "定位校准",
                        value = "待开始",
                        status = AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    )
                )
            ),
            locationSnapshot = emptyLocationSnapshot(TrailMateLocationStatus.SEARCHING)
        )

        assertTrue(report.contains("locationStatus=SEARCHING"))
        assertTrue(report.contains("locationRecoveryAction=WAIT_FOR_FIRST_FIX"))
        assertTrue(report.contains("locationRecoveryStep=移动到开阔处并保持 TrailMate 在前台，等待首个 GPS fix。"))
    }

    @Test
    fun presentsCopyActionStateForDeviceDiagnosticsReport() {
        val idle = TrailMateDeviceDiagnosticsReportActionPresenter.present(copied = false)
        val copied = TrailMateDeviceDiagnosticsReportActionPresenter.present(copied = true)

        assertEquals("复制诊断报告", idle.label)
        assertEquals("复制 TrailMate 真机诊断报告", idle.contentDescription)
        assertEquals("诊断报告已复制", copied.label)
        assertEquals("TrailMate 真机诊断报告已复制", copied.contentDescription)
    }

    private fun baseLaunchDiagnostics(
        locationItems: List<AmapLaunchDiagnosticItem>
    ): AmapLaunchDiagnostics =
        AmapLaunchDiagnostics(
            title = "高德上线检查",
            statusLabel = "待定位",
            caption = "定位未就绪。",
            packageName = "com.trailmate.app",
            revealsApiKey = false,
            offlineMapActionLabel = null,
            items = locationItems
        )

    private fun emptyLocationSnapshot(status: TrailMateLocationStatus): TrailMateLocationSnapshot =
        TrailMateLocationSnapshot(
            status = status,
            latitude = null,
            longitude = null,
            elevationMeters = null,
            horizontalAccuracyMeters = null,
            timestampEpochMillis = 1_000L
        )
}
