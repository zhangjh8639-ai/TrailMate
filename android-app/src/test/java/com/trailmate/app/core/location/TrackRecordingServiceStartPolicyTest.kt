package com.trailmate.app.core.location

import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecordingServiceStartPolicyTest {
    @Test
    fun startsRecordingWhenRoutePermissionAndProviderAreReady() {
        val decision = TrackRecordingServiceStartPolicy.resolve(
            requestedRouteName = "龙井山脊",
            current = TrackRecordingState(),
            hasPreciseLocationPermission = true,
            hasEnabledProvider = true,
            nowEpochMillis = NOW
        )

        assertEquals(TrackRecordingServiceStartAction.PUBLISH_AND_START_UPDATES, decision.action)
        assertEquals(TrackRecordingStatus.RECORDING, decision.trackRecording.status)
        assertEquals("龙井山脊", decision.trackRecording.routeName)
        assertEquals("正在获取定位", decision.notificationCaption)
        assertFalse(decision.shouldStopSelf)
    }

    @Test
    fun doesNotStartRecordingWithoutPreciseLocationPermission() {
        val decision = TrackRecordingServiceStartPolicy.resolve(
            requestedRouteName = "龙井山脊",
            current = TrackRecordingState(),
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true,
            nowEpochMillis = NOW
        )

        assertEquals(TrackRecordingServiceStartAction.PUBLISH_ONLY, decision.action)
        assertEquals(TrackRecordingStatus.IDLE, decision.trackRecording.status)
        assertEquals("需要精确定位权限后才能记录轨迹", decision.notificationCaption)
        assertTrue(decision.shouldStopSelf)
    }

    @Test
    fun doesNotStartRecordingWhenSystemLocationProviderIsDisabled() {
        val decision = TrackRecordingServiceStartPolicy.resolve(
            requestedRouteName = "龙井山脊",
            current = TrackRecordingState(),
            hasPreciseLocationPermission = true,
            hasEnabledProvider = false,
            nowEpochMillis = NOW
        )

        assertEquals(TrackRecordingServiceStartAction.PUBLISH_ONLY, decision.action)
        assertEquals(TrackRecordingStatus.IDLE, decision.trackRecording.status)
        assertEquals("系统定位未开启，无法记录轨迹", decision.notificationCaption)
        assertTrue(decision.shouldStopSelf)
    }

    @Test
    fun stopsWhenNoRouteNameIsAvailable() {
        val decision = TrackRecordingServiceStartPolicy.resolve(
            requestedRouteName = "",
            current = TrackRecordingState(),
            hasPreciseLocationPermission = true,
            hasEnabledProvider = true,
            nowEpochMillis = NOW
        )

        assertEquals(TrackRecordingServiceStartAction.STOP_SELF, decision.action)
        assertEquals(TrackRecordingStatus.IDLE, decision.trackRecording.status)
        assertTrue(decision.shouldStopSelf)
    }

    @Test
    fun pausesActiveRecordingWhenPrecisePermissionIsRevoked() {
        val current = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊",
            startedAtEpochMillis = NOW - 60_000L,
            recordingActiveSinceEpochMillis = NOW - 60_000L
        )

        val decision = TrackRecordingServiceStartPolicy.resolve(
            requestedRouteName = "龙井山脊",
            current = current,
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true,
            nowEpochMillis = NOW
        )

        assertEquals(TrackRecordingServiceStartAction.PUBLISH_ONLY, decision.action)
        assertEquals(TrackRecordingStatus.PAUSED, decision.trackRecording.status)
        assertEquals(NOW, decision.trackRecording.pausedAtEpochMillis)
        assertEquals("需要精确定位权限后才能记录轨迹", decision.notificationCaption)
        assertTrue(decision.shouldStopSelf)
    }

    @Test
    fun keepsIdleWhenLocationUpdatesFailBeforeRecordingStarts() {
        val decision = TrackRecordingServiceStartPolicy.resolveRuntimeBlock(
            currentBeforeStart = TrackRecordingState(),
            notificationCaption = "定位服务暂不可用",
            nowEpochMillis = NOW
        )

        assertEquals(TrackRecordingServiceStartAction.PUBLISH_ONLY, decision.action)
        assertEquals(TrackRecordingStatus.IDLE, decision.trackRecording.status)
        assertEquals("定位服务暂不可用", decision.notificationCaption)
        assertTrue(decision.shouldStopSelf)
    }

    @Test
    fun pausesActiveRecordingWhenProviderIsDisabledDuringRecording() {
        val current = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊",
            startedAtEpochMillis = NOW - 60_000L,
            recordingActiveSinceEpochMillis = NOW - 60_000L
        )

        val decision = TrackRecordingServiceStartPolicy.resolveRuntimeBlock(
            currentBeforeStart = current,
            notificationCaption = "系统定位未开启，无法记录轨迹",
            nowEpochMillis = NOW
        )

        assertEquals(TrackRecordingServiceStartAction.PUBLISH_ONLY, decision.action)
        assertEquals(TrackRecordingStatus.PAUSED, decision.trackRecording.status)
        assertEquals(NOW, decision.trackRecording.pausedAtEpochMillis)
        assertEquals("系统定位未开启，无法记录轨迹", decision.notificationCaption)
        assertTrue(decision.shouldStopSelf)
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
