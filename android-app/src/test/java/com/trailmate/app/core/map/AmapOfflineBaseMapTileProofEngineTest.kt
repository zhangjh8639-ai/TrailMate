package com.trailmate.app.core.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmapOfflineBaseMapTileProofEngineTest {
    @Test
    fun acceptsProofOnlyForSameRouteAndTargetRegion() {
        val proof = AmapOfflineBaseMapTileProof(
            routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
            targetAdcode = "330100",
            targetCityName = "杭州市",
            verifiedAtEpochMillis = 1_700_000_000_000L,
            networkDisabled = true,
            tileVisible = true
        )

        assertTrue(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(proof),
                routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
                targetRegion = hangzhouRegion
            )
        )
        assertFalse(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(proof),
                routeKey = "other.gpx|龙井山脊|15.2|860|128",
                targetRegion = hangzhouRegion
            )
        )
        assertFalse(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(proof),
                routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
                targetRegion = hangzhouRegion.copy(adcode = "330200")
            )
        )
    }

    @Test
    fun matchesCityWhenAdcodeIsMissing() {
        val proof = AmapOfflineBaseMapTileProof(
            routeKey = "mountain.gpx|山脊线|12.0|600|100",
            targetAdcode = null,
            targetCityName = "杭州市",
            verifiedAtEpochMillis = 1_700_000_000_000L,
            networkDisabled = true,
            tileVisible = true
        )
        val routeKey = "mountain.gpx|山脊线|12.0|600|100"

        assertTrue(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(proof),
                routeKey = routeKey,
                targetRegion = hangzhouRegion.copy(adcode = null)
            )
        )
        assertFalse(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(proof),
                routeKey = routeKey,
                targetRegion = hangzhouRegion.copy(cityName = "绍兴市", adcode = null)
            )
        )
    }

    @Test
    fun rejectsProofWhenNetworkWasNotDisabledOrTilesWereNotVisible() {
        val baseProof = AmapOfflineBaseMapTileProof(
            routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
            targetAdcode = "330100",
            targetCityName = "杭州市",
            verifiedAtEpochMillis = 1_700_000_000_000L,
            networkDisabled = true,
            tileVisible = true
        )

        assertFalse(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(baseProof.copy(networkDisabled = false)),
                routeKey = baseProof.routeKey,
                targetRegion = hangzhouRegion
            )
        )
        assertFalse(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(baseProof.copy(tileVisible = false)),
                routeKey = baseProof.routeKey,
                targetRegion = hangzhouRegion
            )
        )
    }

    @Test
    fun recordsRouteScopedProofFromTargetRegion() {
        val proof = AmapOfflineBaseMapTileProofEngine.recordProofOrNull(
            routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
            targetRegion = hangzhouRegion,
            nowEpochMillis = 1_700_000_000_000L,
            networkDisabled = true,
            tileVisible = true
        )!!

        assertTrue(proof.networkDisabled)
        assertTrue(proof.tileVisible)
        assertTrue(
            AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
                proofs = listOf(proof),
                routeKey = proof.routeKey,
                targetRegion = hangzhouRegion
            )
        )
    }

    @Test
    fun doesNotRecordProofWhenNetworkIsAvailableOrTilesAreNotVisible() {
        assertFalse(
            AmapOfflineBaseMapTileProofEngine.recordProofOrNull(
                routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
                targetRegion = hangzhouRegion,
                nowEpochMillis = 1_700_000_000_000L,
                networkDisabled = false,
                tileVisible = true
            ) != null
        )
        assertFalse(
            AmapOfflineBaseMapTileProofEngine.recordProofOrNull(
                routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
                targetRegion = hangzhouRegion,
                nowEpochMillis = 1_700_000_000_000L,
                networkDisabled = true,
                tileVisible = false
            ) != null
        )
    }

    @Test
    fun captureGateRequiresCoverageNetworkDisabledAndRenderedBaseMap() {
        val ready = AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
            targetRegionKnown = true,
            offlineBaseMapCoversTargetRoute = true,
            networkUnavailable = true,
            amapBaseMapRenderedInCurrentSession = true
        )

        assertTrue(ready.canRecordProof)
        assertEquals(null, ready.failureMessage)
        assertEquals("我已断网并看到底图", ready.actionLabel)

        val notRendered = AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
            targetRegionKnown = true,
            offlineBaseMapCoversTargetRoute = true,
            networkUnavailable = true,
            amapBaseMapRenderedInCurrentSession = false
        )

        assertFalse(notRendered.canRecordProof)
        assertEquals("请先在断网状态下确认高德底图已加载并可见。", notRendered.failureMessage)
        assertEquals("确认底图可见后记录", notRendered.actionLabel)
    }

    @Test
    fun captureGateReportsFirstRepairAction() {
        val unknownRegion = AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
            targetRegionKnown = false,
            offlineBaseMapCoversTargetRoute = false,
            networkUnavailable = false,
            amapBaseMapRenderedInCurrentSession = false
        )
        assertEquals("正在确认路线所属区域，稍后再试。", unknownRegion.failureMessage)
        assertEquals("等待路线区域", unknownRegion.actionLabel)

        val missingCoverage = AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
            targetRegionKnown = true,
            offlineBaseMapCoversTargetRoute = false,
            networkUnavailable = false,
            amapBaseMapRenderedInCurrentSession = false
        )
        assertEquals("请先下载覆盖当前路线的高德离线底图。", missingCoverage.failureMessage)
        assertEquals("先下载离线底图", missingCoverage.actionLabel)

        val networkAvailable = AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
            targetRegionKnown = true,
            offlineBaseMapCoversTargetRoute = true,
            networkUnavailable = false,
            amapBaseMapRenderedInCurrentSession = false
        )
        assertEquals("请先关闭蜂窝网络和 Wi-Fi，再确认底图是否仍可显示。", networkAvailable.failureMessage)
        assertEquals("关闭网络后验证", networkAvailable.actionLabel)
    }

    @Test
    fun captureGateDisablesProofActionUntilEveryConditionIsSatisfied() {
        val notReadyStates = listOf(
            AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
                targetRegionKnown = false,
                offlineBaseMapCoversTargetRoute = false,
                networkUnavailable = false,
                amapBaseMapRenderedInCurrentSession = false
            ),
            AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
                targetRegionKnown = true,
                offlineBaseMapCoversTargetRoute = false,
                networkUnavailable = false,
                amapBaseMapRenderedInCurrentSession = false
            ),
            AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
                targetRegionKnown = true,
                offlineBaseMapCoversTargetRoute = true,
                networkUnavailable = false,
                amapBaseMapRenderedInCurrentSession = false
            ),
            AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
                targetRegionKnown = true,
                offlineBaseMapCoversTargetRoute = true,
                networkUnavailable = true,
                amapBaseMapRenderedInCurrentSession = false
            )
        )

        assertTrue(notReadyStates.all { !it.canRecordProof })
        assertTrue(notReadyStates.all { it.actionLabel != "我已断网并看到底图" })
    }

    private companion object {
        val hangzhouRegion = AmapTargetRouteRegion(
            provinceName = "浙江省",
            cityName = "杭州市",
            cityCode = "0571",
            adcode = "330100"
        )
    }
}
