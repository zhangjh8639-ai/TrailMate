package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OutdoorProductionReleaseGateEngineTest {
    @Test
    fun blocksProductionClaimWhenOnlyEmulatorEvidenceExists() {
        val gate = OutdoorProductionReleaseGateEngine.evaluate(
            evidence = OutdoorProductionReleaseEvidence(
                emulatorMapAndRecordingRegressionPassed = true,
                releasePackageAndSha1Bound = false,
                productionAmapKeyConfigured = true,
                targetOfflineBaseMapRegionCount = 0,
                physicalDeviceFieldQaPassed = false,
                backgroundRecordingFieldVerified = false,
                weakSignalFieldVerified = false,
                batteryFieldVerified = false,
                safetyShareFieldVerified = false
            )
        )

        assertFalse(gate.canClaimOutdoorProductionReady)
        assertEquals("不可发布", gate.statusLabel)
        assertEquals("补齐发布身份", gate.nextActionLabel)
        assertTrue(gate.blockers.contains("Release 包名/SHA1 未完成高德控制台核验"))
        assertTrue(gate.blockers.contains("目标区域离线底图未验证"))
        assertTrue(gate.blockers.contains("真机户外现场 QA 未通过"))
        assertTrue(gate.caption.contains("模拟器证据不能替代真机现场验证"))
    }

    @Test
    fun allowsProductionCandidateOnlyWhenReleaseOfflineAndFieldEvidencePass() {
        val gate = OutdoorProductionReleaseGateEngine.evaluate(
            evidence = OutdoorProductionReleaseEvidence(
                emulatorMapAndRecordingRegressionPassed = true,
                releasePackageAndSha1Bound = true,
                productionAmapKeyConfigured = true,
                targetOfflineBaseMapRegionCount = 1,
                targetOfflineBaseMapCoversRoute = true,
                offlineBaseMapAirplaneModeVerified = true,
                physicalDeviceFieldQaPassed = true,
                backgroundRecordingFieldVerified = true,
                weakSignalFieldVerified = true,
                batteryFieldVerified = true,
                safetyShareFieldVerified = true
            )
        )

        assertTrue(gate.canClaimOutdoorProductionReady)
        assertEquals("生产候选", gate.statusLabel)
        assertEquals("准备发布复核", gate.nextActionLabel)
        assertTrue(gate.blockers.isEmpty())
    }

    @Test
    fun blocksProductionClaimWhenOfflineBaseMapDoesNotCoverTargetRoute() {
        val gate = OutdoorProductionReleaseGateEngine.evaluate(
            evidence = completeEvidence(
                targetOfflineBaseMapCoversRoute = false,
                offlineBaseMapAirplaneModeVerified = true
            )
        )

        assertFalse(gate.canClaimOutdoorProductionReady)
        assertEquals("不可发布", gate.statusLabel)
        assertEquals("验证离线底图", gate.nextActionLabel)
        assertTrue(gate.blockers.contains("目标区域离线底图未覆盖当前路线"))
    }

    @Test
    fun blocksProductionClaimUntilOfflineBaseMapTilesAreVerifiedWithoutNetwork() {
        val gate = OutdoorProductionReleaseGateEngine.evaluate(
            evidence = completeEvidence(
                targetOfflineBaseMapCoversRoute = true,
                offlineBaseMapAirplaneModeVerified = false
            )
        )

        assertFalse(gate.canClaimOutdoorProductionReady)
        assertEquals("不可发布", gate.statusLabel)
        assertEquals("验证离线底图", gate.nextActionLabel)
        assertTrue(gate.blockers.contains("断网离线底图瓦片未验证"))
    }

    private fun completeEvidence(
        targetOfflineBaseMapCoversRoute: Boolean,
        offlineBaseMapAirplaneModeVerified: Boolean
    ): OutdoorProductionReleaseEvidence =
        OutdoorProductionReleaseEvidence(
            emulatorMapAndRecordingRegressionPassed = true,
            releasePackageAndSha1Bound = true,
            productionAmapKeyConfigured = true,
            targetOfflineBaseMapRegionCount = 1,
            targetOfflineBaseMapCoversRoute = targetOfflineBaseMapCoversRoute,
            offlineBaseMapAirplaneModeVerified = offlineBaseMapAirplaneModeVerified,
            physicalDeviceFieldQaPassed = true,
            backgroundRecordingFieldVerified = true,
            weakSignalFieldVerified = true,
            batteryFieldVerified = true,
            safetyShareFieldVerified = true
        )
}
