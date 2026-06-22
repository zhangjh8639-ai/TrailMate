package com.trailmate.app.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhysicalDeviceFieldQaEvidenceEngineTest {
    @Test
    fun passesOnlyWhenFullOutdoorProtocolEvidenceIsPresent() {
        val summary = PhysicalDeviceFieldQaEvidenceEngine.evaluate(completeEvidence)

        assertTrue(summary.physicalDeviceFieldQaPassed)
        assertTrue(summary.backgroundRecordingFieldVerified)
        assertTrue(summary.weakSignalFieldVerified)
        assertTrue(summary.batteryFieldVerified)
        assertTrue(summary.safetyShareFieldVerified)
        assertTrue(summary.blockers.isEmpty())
    }

    @Test
    fun rejectsEmulatorEvidenceEvenWhenChecksLookGreen() {
        val summary = PhysicalDeviceFieldQaEvidenceEngine.evaluate(
            completeEvidence.copy(isPhysicalDevice = false)
        )

        assertFalse(summary.physicalDeviceFieldQaPassed)
        assertTrue(summary.blockers.contains("未使用真实 Android 手机完成户外 QA"))
    }

    @Test
    fun rejectsShortWalkAndWeakBackgroundEvidence() {
        val summary = PhysicalDeviceFieldQaEvidenceEngine.evaluate(
            completeEvidence.copy(
                durationMinutes = 18,
                screenLockedMinutes = 2,
                trackContinuedInBackground = false
            )
        )

        assertFalse(summary.physicalDeviceFieldQaPassed)
        assertFalse(summary.backgroundRecordingFieldVerified)
        assertTrue(summary.blockers.contains("户外行走时长不足 30 分钟"))
        assertTrue(summary.blockers.contains("锁屏或后台轨迹记录证据不足"))
    }

    private companion object {
        val completeEvidence = PhysicalDeviceFieldQaEvidence(
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
