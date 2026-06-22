package com.trailmate.app.core.model

data class PhysicalDeviceFieldQaEvidence(
    val isPhysicalDevice: Boolean,
    val deviceModel: String,
    val androidVersion: String,
    val routeDistanceKm: Double,
    val importedRoutePointCount: Int,
    val durationMinutes: Int,
    val screenLockedMinutes: Int,
    val backgroundMinutes: Int,
    val batteryStartPercent: Int,
    val batteryEndPercent: Int,
    val foregroundNotificationVisible: Boolean,
    val notificationPauseResumeFinishVerified: Boolean,
    val trackContinuedWithScreenLocked: Boolean,
    val trackContinuedInBackground: Boolean,
    val trackPointCountIncreasedDuringMovement: Boolean,
    val trackPointCountStoppedWhilePaused: Boolean,
    val recordedTrackPointCount: Int,
    val weakSignalStateVerified: Boolean,
    val safetyShareReliableLocationVerified: Boolean,
    val safetyShareBlockedLowAccuracy: Boolean,
    val crashOrAnrObserved: Boolean,
    val recordingLostObserved: Boolean
)

data class PhysicalDeviceFieldQaSummary(
    val physicalDeviceFieldQaPassed: Boolean,
    val backgroundRecordingFieldVerified: Boolean,
    val weakSignalFieldVerified: Boolean,
    val batteryFieldVerified: Boolean,
    val safetyShareFieldVerified: Boolean,
    val blockers: List<String>
)

object PhysicalDeviceFieldQaEvidenceEngine {
    fun evaluate(evidence: PhysicalDeviceFieldQaEvidence?): PhysicalDeviceFieldQaSummary {
        if (evidence == null) {
            return PhysicalDeviceFieldQaSummary(
                physicalDeviceFieldQaPassed = false,
                backgroundRecordingFieldVerified = false,
                weakSignalFieldVerified = false,
                batteryFieldVerified = false,
                safetyShareFieldVerified = false,
                blockers = listOf("真机户外现场 QA 证据缺失")
            )
        }

        val noFatalRuntimeIssue = !evidence.crashOrAnrObserved && !evidence.recordingLostObserved
        val backgroundRecordingVerified =
            evidence.foregroundNotificationVisible &&
                evidence.notificationPauseResumeFinishVerified &&
                evidence.trackContinuedWithScreenLocked &&
                evidence.trackContinuedInBackground &&
                evidence.trackPointCountIncreasedDuringMovement &&
                evidence.trackPointCountStoppedWhilePaused &&
                evidence.screenLockedMinutes >= MIN_SCREEN_LOCKED_MINUTES &&
                evidence.backgroundMinutes >= MIN_BACKGROUND_MINUTES &&
                evidence.recordedTrackPointCount > 0 &&
                noFatalRuntimeIssue
        val weakSignalVerified = evidence.weakSignalStateVerified
        val batteryVerified =
            evidence.batteryStartPercent >= MIN_START_BATTERY_PERCENT &&
                evidence.batteryStartPercent <= MAX_BATTERY_PERCENT &&
                evidence.batteryEndPercent in MIN_BATTERY_PERCENT..evidence.batteryStartPercent &&
                (evidence.batteryStartPercent - evidence.batteryEndPercent) <= MAX_BATTERY_DRAIN_PERCENT
        val safetyShareVerified =
            evidence.safetyShareReliableLocationVerified &&
                evidence.safetyShareBlockedLowAccuracy

        val blockers = buildList {
            if (!evidence.isPhysicalDevice || evidence.deviceModel.isBlank() || evidence.androidVersion.isBlank()) {
                add("未使用真实 Android 手机完成户外 QA")
            }
            if (evidence.routeDistanceKm < MIN_ROUTE_DISTANCE_KM || evidence.importedRoutePointCount < MIN_ROUTE_POINT_COUNT) {
                add("测试路线距离或点数不足")
            }
            if (evidence.durationMinutes < MIN_DURATION_MINUTES) {
                add("户外行走时长不足 30 分钟")
            }
            if (!backgroundRecordingVerified) {
                add("锁屏或后台轨迹记录证据不足")
            }
            if (!weakSignalVerified) {
                add("弱信号路线可靠性未验证")
            }
            if (!batteryVerified) {
                add("长时间记录耗电未验证")
            }
            if (!safetyShareVerified) {
                add("安全分享现场可用性未验证")
            }
            if (evidence.crashOrAnrObserved) {
                add("户外 QA 期间出现崩溃或 ANR")
            }
            if (evidence.recordingLostObserved) {
                add("户外 QA 期间轨迹记录丢失")
            }
        }

        return PhysicalDeviceFieldQaSummary(
            physicalDeviceFieldQaPassed = blockers.isEmpty(),
            backgroundRecordingFieldVerified = backgroundRecordingVerified,
            weakSignalFieldVerified = weakSignalVerified,
            batteryFieldVerified = batteryVerified,
            safetyShareFieldVerified = safetyShareVerified,
            blockers = blockers
        )
    }

    private const val MIN_ROUTE_DISTANCE_KM = 2.0
    private const val MIN_ROUTE_POINT_COUNT = 100
    private const val MIN_DURATION_MINUTES = 30
    private const val MIN_SCREEN_LOCKED_MINUTES = 5
    private const val MIN_BACKGROUND_MINUTES = 8
    private const val MIN_START_BATTERY_PERCENT = 80
    private const val MIN_BATTERY_PERCENT = 0
    private const val MAX_BATTERY_PERCENT = 100
    private const val MAX_BATTERY_DRAIN_PERCENT = 15
}
