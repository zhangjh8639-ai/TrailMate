package com.trailmate.app.core.model

import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProofEngine
import com.trailmate.app.core.map.AmapTargetRouteRegion

data class OutdoorProductionReleaseEvidence(
    val emulatorMapAndRecordingRegressionPassed: Boolean,
    val releasePackageAndSha1Bound: Boolean,
    val productionAmapKeyConfigured: Boolean,
    val targetOfflineBaseMapRegionCount: Int,
    val targetOfflineBaseMapCoversRoute: Boolean = false,
    val offlineBaseMapAirplaneModeVerified: Boolean = false,
    val physicalDeviceFieldQaPassed: Boolean,
    val backgroundRecordingFieldVerified: Boolean,
    val weakSignalFieldVerified: Boolean,
    val batteryFieldVerified: Boolean,
    val safetyShareFieldVerified: Boolean
)

data class OutdoorProductionReleaseEvidenceInput(
    val emulatorMapAndRecordingRegressionPassed: Boolean,
    val releasePackageAndSha1Bound: Boolean,
    val productionAmapKeyConfigured: Boolean,
    val targetOfflineBaseMapRegionCount: Int,
    val targetOfflineBaseMapCoversRoute: Boolean,
    val routeKey: String,
    val targetRouteRegion: AmapTargetRouteRegion?,
    val offlineBaseMapTileProofs: List<AmapOfflineBaseMapTileProof>,
    val physicalDeviceFieldQaEvidence: PhysicalDeviceFieldQaEvidence?
)

data class OutdoorProductionReleaseGate(
    val statusLabel: String,
    val caption: String,
    val nextActionLabel: String,
    val canClaimOutdoorProductionReady: Boolean,
    val blockers: List<String>
)

object OutdoorProductionReleaseEvidenceBuilder {
    fun build(input: OutdoorProductionReleaseEvidenceInput): OutdoorProductionReleaseEvidence {
        val fieldQaSummary = PhysicalDeviceFieldQaEvidenceEngine.evaluate(input.physicalDeviceFieldQaEvidence)

        return OutdoorProductionReleaseEvidence(
            emulatorMapAndRecordingRegressionPassed = input.emulatorMapAndRecordingRegressionPassed,
            releasePackageAndSha1Bound = input.releasePackageAndSha1Bound,
            productionAmapKeyConfigured = input.productionAmapKeyConfigured,
            targetOfflineBaseMapRegionCount = input.targetOfflineBaseMapRegionCount,
            targetOfflineBaseMapCoversRoute = input.targetOfflineBaseMapCoversRoute,
            offlineBaseMapAirplaneModeVerified = AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = input.offlineBaseMapTileProofs,
                routeKey = input.routeKey,
                targetRegion = input.targetRouteRegion
            ),
            physicalDeviceFieldQaPassed = fieldQaSummary.physicalDeviceFieldQaPassed,
            backgroundRecordingFieldVerified = fieldQaSummary.backgroundRecordingFieldVerified,
            weakSignalFieldVerified = fieldQaSummary.weakSignalFieldVerified,
            batteryFieldVerified = fieldQaSummary.batteryFieldVerified,
            safetyShareFieldVerified = fieldQaSummary.safetyShareFieldVerified
        )
    }
}

object OutdoorProductionReleaseGateEngine {
    fun evaluate(evidence: OutdoorProductionReleaseEvidence): OutdoorProductionReleaseGate {
        val blockers = buildList {
            if (!evidence.releasePackageAndSha1Bound) {
                add("Release 包名/SHA1 未完成高德控制台核验")
            }
            if (!evidence.productionAmapKeyConfigured) {
                add("生产高德 Key 未配置")
            }
            if (evidence.targetOfflineBaseMapRegionCount <= 0) {
                add("目标区域离线底图未验证")
            }
            if (!evidence.targetOfflineBaseMapCoversRoute) {
                add("目标区域离线底图未覆盖当前路线")
            }
            if (!evidence.offlineBaseMapAirplaneModeVerified) {
                add("断网离线底图瓦片未验证")
            }
            if (!evidence.physicalDeviceFieldQaPassed) {
                add("真机户外现场 QA 未通过")
            }
            if (!evidence.backgroundRecordingFieldVerified) {
                add("锁屏/后台轨迹记录未验证")
            }
            if (!evidence.weakSignalFieldVerified) {
                add("弱信号路线可靠性未验证")
            }
            if (!evidence.batteryFieldVerified) {
                add("长时间记录耗电未验证")
            }
            if (!evidence.safetyShareFieldVerified) {
                add("安全分享现场可用性未验证")
            }
        }

        val canClaimReady = blockers.isEmpty() && evidence.emulatorMapAndRecordingRegressionPassed
        if (canClaimReady) {
            return OutdoorProductionReleaseGate(
                statusLabel = "生产候选",
                caption = "Release 身份、离线底图、真机现场 QA 和自动化回归均已具备，可进入发布复核。",
                nextActionLabel = "准备发布复核",
                canClaimOutdoorProductionReady = true,
                blockers = emptyList()
            )
        }

        return OutdoorProductionReleaseGate(
            statusLabel = "不可发布",
            caption = if (evidence.emulatorMapAndRecordingRegressionPassed) {
                "模拟器证据不能替代真机现场验证；发布前仍需补齐户外门禁。"
            } else {
                "自动化地图、导航和轨迹记录回归尚未通过，发布前需先修复基础能力。"
            },
            nextActionLabel = nextActionLabel(blockers),
            canClaimOutdoorProductionReady = false,
            blockers = blockers
        )
    }

    private fun nextActionLabel(blockers: List<String>): String =
        when {
            blockers.any { it.contains("包名/SHA1") || it.contains("Key") } -> "补齐发布身份"
            blockers.any { it.contains("离线底图") } -> "验证离线底图"
            blockers.any { it.contains("真机") } -> "执行真机 QA"
            blockers.isNotEmpty() -> "补齐现场验证"
            else -> "补齐自动化回归"
        }
}
