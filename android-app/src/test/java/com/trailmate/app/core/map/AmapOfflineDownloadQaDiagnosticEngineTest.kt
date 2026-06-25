package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmapOfflineDownloadQaDiagnosticEngineTest {
    @Test
    fun passesWhenDownloadedStateOrRegionCountConfirmsTargetCity() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(downloadedStateVerified = true)
        )

        assertTrue(diagnostic.passed)
        assertEquals("离线底图已下载", diagnostic.statusLabel)
        assertTrue(diagnostic.blockers.isEmpty())
    }

    @Test
    fun blocksWhenTargetCityCannotBeResolvedFromCatalog() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                catalogLoaded = true,
                targetCityResolved = false,
                resolvedCityName = "",
                resolvedCityCode = null,
                resolvedAdcode = null,
                downloadedStateVerified = false
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德离线目录中未找到目标城市"))
        assertEquals("检查目标城市名称", diagnostic.nextActionLabel)
    }

    @Test
    fun explainsCheckingStateWithValidatedNetworkAsSdkOrServiceGate() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                lastStatusLabel = "CHECKUPDATES(6)",
                lastCompletePercent = 0,
                networkValidated = true
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德 SDK 停留在检查更新状态"))
        assertEquals(AmapOfflineDownloadRecoveryAction.VERIFY_AMAP_KEY_BINDING, diagnostic.recoveryAction)
        assertEquals("核对高德 Key 权限", diagnostic.nextActionLabel)
        assertTrue(diagnostic.summary.contains("CHECKUPDATES(6)"))
        assertTrue(diagnostic.summary.contains("request=CITY_CODE:0571"))
        assertTrue(diagnostic.summary.contains("package=com.trailmate.app"))
        assertTrue(diagnostic.summary.contains("sha1=DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88"))
        assertTrue(diagnostic.summary.contains("/Android/data/com.trailmate.app/files/amap"))
    }

    @Test
    fun explainsMissingDownloadCallbackAsForegroundManagerVerification() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                lastStatusLabel = "NO_CALLBACK",
                lastCompletePercent = 0,
                lastCallbackCityName = "",
                networkValidated = true,
                amapStorageWritable = true
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德 SDK 未返回离线下载回调"))
        assertEquals(AmapOfflineDownloadRecoveryAction.VERIFY_AMAP_KEY_BINDING, diagnostic.recoveryAction)
        assertEquals("核对高德 Key 权限", diagnostic.nextActionLabel)
        assertTrue(diagnostic.recoverySteps.any { it.contains("com.trailmate.app") })
        assertTrue(diagnostic.recoverySteps.any { it.contains("DF:CB:37") })
        assertTrue(diagnostic.recoverySteps.any { it.contains("infocode=10012") })
    }

    @Test
    fun explainsNetworkAndStoragePrerequisites() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                networkValidated = false,
                amapStorageWritable = false
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("设备网络未通过系统验证"))
        assertTrue(diagnostic.blockers.contains("高德离线目录不可写"))
        assertEquals("修复网络或存储环境", diagnostic.nextActionLabel)
    }

    @Test
    fun routesUnvalidatedNetworkToNetworkSettingsRecovery() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                networkValidated = false,
                amapStorageWritable = true
            )
        )

        assertFalse(diagnostic.passed)
        assertEquals(AmapOfflineDownloadRecoveryAction.OPEN_NETWORK_SETTINGS, diagnostic.recoveryAction)
        assertEquals("打开网络设置后重试下载", diagnostic.nextActionLabel)
        assertTrue(diagnostic.recoverySteps.any { it.contains("Wi-Fi") || it.contains("蜂窝") })
    }

    @Test
    fun routesStorageFailureToStorageRecovery() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                networkValidated = true,
                amapStorageWritable = false
            )
        )

        assertFalse(diagnostic.passed)
        assertEquals(AmapOfflineDownloadRecoveryAction.FIX_STORAGE, diagnostic.recoveryAction)
        assertEquals("清理存储后重试下载", diagnostic.nextActionLabel)
        assertTrue(diagnostic.recoverySteps.any { it.contains("存储空间") })
    }

    @Test
    fun routesAmapAuthFailureToKeyBindingRecoveryWithoutSecretMaterial() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                lastStatusLabel = "EXCEPTION_AMAP(102)"
            )
        )

        assertFalse(diagnostic.passed)
        assertEquals(AmapOfflineDownloadRecoveryAction.VERIFY_AMAP_KEY_BINDING, diagnostic.recoveryAction)
        assertEquals("核对高德 Key 绑定", diagnostic.nextActionLabel)
        assertTrue(diagnostic.recoverySteps.any { it.contains("com.trailmate.app") })
        assertTrue(diagnostic.recoverySteps.any { it.contains("DF:CB:37") })
        assertFalse(diagnostic.recoverySteps.joinToString("\n").contains("ae0d80"))
    }

    @Test
    fun explainsAmapNetworkExceptionStatus() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                lastStatusLabel = "EXCEPTION_NETWORK_LOADING(101)",
                lastCompletePercent = 0
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德 SDK 报告下载网络异常"))
        assertEquals("修复网络或存储环境", diagnostic.nextActionLabel)
    }

    @Test
    fun explainsAmapServiceExceptionStatusAsKeyOrServiceGate() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                lastStatusLabel = "EXCEPTION_AMAP(102)",
                lastCompletePercent = 0
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德 SDK 报告服务或鉴权异常"))
        assertEquals("核对高德 Key 绑定", diagnostic.nextActionLabel)
    }

    @Test
    fun explainsAmapStorageExceptionStatus() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                lastStatusLabel = "EXCEPTION_SDCARD(103)",
                lastCompletePercent = 0
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德 SDK 报告离线存储异常"))
        assertEquals("修复网络或存储环境", diagnostic.nextActionLabel)
    }

    @Test
    fun explainsStartDownloadFailureStatus() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false,
                lastStatusLabel = "START_DOWNLOAD_FAILED(1002)",
                lastCompletePercent = 0
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德 SDK 启动离线下载失败"))
        assertEquals("真机重试离线下载", diagnostic.nextActionLabel)
    }

    @Test
    fun blocksWhenAmapKeyIsMissingWithoutRevealingKeyMaterial() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                amapKeyConfigured = false,
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("高德 Android Key 未注入"))
        assertEquals("核对高德 Key 绑定", diagnostic.nextActionLabel)
        assertFalse(diagnostic.summary.contains("ae0d80"))
    }

    @Test
    fun blocksWhenRuntimePackageSha1CannotBeRead() {
        val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
            successfulSnapshot.copy(
                runtimePackageSha1 = null,
                downloadedStateVerified = false,
                downloadedRegionCountIncreased = false
            )
        )

        assertFalse(diagnostic.passed)
        assertTrue(diagnostic.blockers.contains("无法读取安装包 SHA1"))
        assertEquals("核对高德 Key 绑定", diagnostic.nextActionLabel)
    }

    private companion object {
        val successfulSnapshot = AmapOfflineDownloadQaSnapshot(
            targetCityName = "杭州市",
            amapKeyConfigured = true,
            runtimePackageName = "com.trailmate.app",
            runtimePackageSha1 = "DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88",
            catalogLoaded = true,
            targetCityResolved = true,
            resolvedCityName = "杭州市",
            resolvedCityCode = "0571",
            resolvedAdcode = "330100",
            downloadRequestKind = "CITY_CODE",
            downloadRequestValue = "0571",
            amapStorageDirectory = "/storage/emulated/0/Android/data/com.trailmate.app/files/amap",
            initialDownloadedRegionCount = 0,
            downloadedRegionCount = 1,
            downloadedStateVerified = true,
            downloadedRegionCountIncreased = true,
            lastStatusLabel = "SUCCESS(4)",
            lastCompletePercent = 100,
            lastCallbackCityName = "杭州市",
            networkValidated = true,
            amapStorageWritable = true
        )
    }
}
