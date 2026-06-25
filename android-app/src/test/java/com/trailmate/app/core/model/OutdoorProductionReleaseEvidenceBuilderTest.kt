package com.trailmate.app.core.model

import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapTargetRouteRegion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OutdoorProductionReleaseEvidenceBuilderTest {
    @Test
    fun derivesAirplaneModeEvidenceFromMatchingOfflineTileProof() {
        val routeKey = "longjing.gpx|龙井山脊|15.2|860|128"
        val evidence = OutdoorProductionReleaseEvidenceBuilder.build(
            input = completeInput(
                routeKey = routeKey,
                offlineBaseMapTileProofs = listOf(
                    AmapOfflineBaseMapTileProof(
                        routeKey = routeKey,
                        targetAdcode = "330100",
                        targetCityName = "杭州市",
                        verifiedAtEpochMillis = 1_700_000_000_000L,
                        networkDisabled = true,
                        tileVisible = true
                    )
                )
            )
        )

        assertTrue(evidence.offlineBaseMapAirplaneModeVerified)
    }

    @Test
    fun ignoresOfflineTileProofFromAnotherRouteOrRegion() {
        val evidence = OutdoorProductionReleaseEvidenceBuilder.build(
            input = completeInput(
                routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
                offlineBaseMapTileProofs = listOf(
                    AmapOfflineBaseMapTileProof(
                        routeKey = "other.gpx|龙井山脊|15.2|860|128",
                        targetAdcode = "330100",
                        targetCityName = "杭州市",
                        verifiedAtEpochMillis = 1_700_000_000_000L,
                        networkDisabled = true,
                        tileVisible = true
                    ),
                    AmapOfflineBaseMapTileProof(
                        routeKey = "longjing.gpx|龙井山脊|15.2|860|128",
                        targetAdcode = "330200",
                        targetCityName = "宁波市",
                        verifiedAtEpochMillis = 1_700_000_000_000L,
                        networkDisabled = true,
                        tileVisible = true
                    )
                )
            )
        )

        assertFalse(evidence.offlineBaseMapAirplaneModeVerified)
    }

    @Test
    fun releaseGateUsesDerivedTileProofEvidence() {
        val routeKey = "longjing.gpx|龙井山脊|15.2|860|128"
        val evidence = OutdoorProductionReleaseEvidenceBuilder.build(
            input = completeInput(
                routeKey = routeKey,
                offlineBaseMapTileProofs = listOf(
                    AmapOfflineBaseMapTileProof(
                        routeKey = routeKey,
                        targetAdcode = "330100",
                        targetCityName = "杭州市",
                        verifiedAtEpochMillis = 1_700_000_000_000L,
                        networkDisabled = true,
                        tileVisible = true
                    )
                )
            )
        )

        val gate = OutdoorProductionReleaseGateEngine.evaluate(evidence)

        assertTrue(gate.canClaimOutdoorProductionReady)
    }

    @Test
    fun releaseGateStaysBlockedWithoutStructuredPhysicalDeviceQaEvidence() {
        val routeKey = "longjing.gpx|龙井山脊|15.2|860|128"
        val evidence = OutdoorProductionReleaseEvidenceBuilder.build(
            input = completeInput(
                routeKey = routeKey,
                offlineBaseMapTileProofs = listOf(
                    AmapOfflineBaseMapTileProof(
                        routeKey = routeKey,
                        targetAdcode = "330100",
                        targetCityName = "杭州市",
                        verifiedAtEpochMillis = 1_700_000_000_000L,
                        networkDisabled = true,
                        tileVisible = true
                    )
                ),
                physicalDeviceFieldQaEvidence = null
            )
        )

        val gate = OutdoorProductionReleaseGateEngine.evaluate(evidence)

        assertFalse(evidence.physicalDeviceFieldQaPassed)
        assertFalse(evidence.backgroundRecordingFieldVerified)
        assertFalse(evidence.weakSignalFieldVerified)
        assertFalse(evidence.batteryFieldVerified)
        assertFalse(evidence.safetyShareFieldVerified)
        assertFalse(gate.canClaimOutdoorProductionReady)
    }

    private fun completeInput(
        routeKey: String,
        offlineBaseMapTileProofs: List<AmapOfflineBaseMapTileProof>,
        physicalDeviceFieldQaEvidence: PhysicalDeviceFieldQaEvidence? = completePhysicalDeviceFieldQaEvidence
    ): OutdoorProductionReleaseEvidenceInput =
        OutdoorProductionReleaseEvidenceInput(
            emulatorMapAndRecordingRegressionPassed = true,
            releasePackageAndSha1Bound = true,
            productionAmapKeyConfigured = true,
            targetOfflineBaseMapRegionCount = 1,
            targetOfflineBaseMapCoversRoute = true,
            routeKey = routeKey,
            targetRouteRegion = hangzhouRegion,
            offlineBaseMapTileProofs = offlineBaseMapTileProofs,
            physicalDeviceFieldQaEvidence = physicalDeviceFieldQaEvidence
        )

    private companion object {
        val hangzhouRegion = AmapTargetRouteRegion(
            provinceName = "浙江省",
            cityName = "杭州市",
            cityCode = "0571",
            adcode = "330100"
        )

        val completePhysicalDeviceFieldQaEvidence = PhysicalDeviceFieldQaEvidence(
            isPhysicalDevice = true,
            deviceModel = "Pixel 8",
            androidVersion = "15",
            routeDistanceKm = 3.2,
            importedRoutePointCount = 420,
            durationMinutes = 32,
            screenLockedMinutes = 6,
            backgroundMinutes = 9,
            batteryStartPercent = 88,
            batteryEndPercent = 82,
            foregroundNotificationVisible = true,
            notificationPauseResumeFinishVerified = true,
            trackContinuedWithScreenLocked = true,
            trackContinuedInBackground = true,
            trackPointCountIncreasedDuringMovement = true,
            trackPointCountStoppedWhilePaused = true,
            recordedTrackPointCount = 180,
            weakSignalStateVerified = true,
            safetyShareReliableLocationVerified = true,
            safetyShareBlockedLowAccuracy = true,
            crashOrAnrObserved = false,
            recordingLostObserved = false
        )
    }
}
