package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackRecordingActionGateEngineTest {
    @Test
    fun requestsLocationBeforeStartingTrackRecording() {
        val step = TrackRecordingActionGateEngine.resolve(
            status = TrackRecordingStatus.IDLE,
            hasForegroundLocationPermission = false,
            notificationPermissionGranted = false,
            locationSnapshot = TrailMateLocationSnapshot.permissionRequired()
        )

        assertEquals(TrackRecordingActionGateStep.REQUEST_FOREGROUND_LOCATION, step)
    }

    @Test
    fun requestsNotificationAfterLocationIsGranted() {
        val step = TrackRecordingActionGateEngine.resolve(
            status = TrackRecordingStatus.PAUSED,
            hasForegroundLocationPermission = true,
            notificationPermissionGranted = false,
            locationSnapshot = reliableLocationSnapshot,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingActionGateStep.REQUEST_NOTIFICATION, step)
    }

    @Test
    fun waitsForReliableLocationBeforeStartingTrackRecording() {
        val step = TrackRecordingActionGateEngine.resolve(
            status = TrackRecordingStatus.IDLE,
            hasForegroundLocationPermission = true,
            notificationPermissionGranted = true,
            locationSnapshot = TrailMateLocationSnapshot.searching()
        )

        assertEquals(TrackRecordingActionGateStep.WAIT_FOR_RELIABLE_LOCATION, step)
    }

    @Test
    fun labelsStartActionAsWaitingForLocationWhenFixIsNotReliable() {
        val label = TrackRecordingActionGateEngine.primaryActionLabel(
            status = TrackRecordingStatus.IDLE,
            step = TrackRecordingActionGateStep.WAIT_FOR_RELIABLE_LOCATION
        )

        assertEquals("等待定位稳定", label)
    }

    @Test
    fun labelsActiveRecordingActionAsPause() {
        val label = TrackRecordingActionGateEngine.primaryActionLabel(
            status = TrackRecordingStatus.RECORDING,
            step = TrackRecordingActionGateStep.APPLY_TRACK_ACTION
        )

        assertEquals("暂停记录", label)
    }

    @Test
    fun waitsForAccurateLocationBeforeResumingTrackRecording() {
        val step = TrackRecordingActionGateEngine.resolve(
            status = TrackRecordingStatus.PAUSED,
            hasForegroundLocationPermission = true,
            notificationPermissionGranted = true,
            locationSnapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOCATED,
                latitude = 30.25,
                longitude = 120.15,
                elevationMeters = null,
                horizontalAccuracyMeters = 86.0,
                timestampEpochMillis = 10_000L
            ),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingActionGateStep.WAIT_FOR_RELIABLE_LOCATION, step)
    }

    @Test
    fun waitsForFreshLocationBeforeStartingTrackRecording() {
        val step = TrackRecordingActionGateEngine.resolve(
            status = TrackRecordingStatus.IDLE,
            hasForegroundLocationPermission = true,
            notificationPermissionGranted = true,
            locationSnapshot = reliableLocationSnapshot.copy(timestampEpochMillis = NOW_EPOCH_MILLIS - 120_000L),
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingActionGateStep.WAIT_FOR_RELIABLE_LOCATION, step)
    }

    @Test
    fun appliesTrackActionWhenStartPermissionsAndReliableLocationAreReady() {
        val step = TrackRecordingActionGateEngine.resolve(
            status = TrackRecordingStatus.FINISHED,
            hasForegroundLocationPermission = true,
            notificationPermissionGranted = true,
            locationSnapshot = reliableLocationSnapshot,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(TrackRecordingActionGateStep.APPLY_TRACK_ACTION, step)
    }

    @Test
    fun allowsPausingActiveRecordingWithoutPermissionPrompts() {
        val step = TrackRecordingActionGateEngine.resolve(
            status = TrackRecordingStatus.RECORDING,
            hasForegroundLocationPermission = false,
            notificationPermissionGranted = false,
            locationSnapshot = TrailMateLocationSnapshot.permissionRequired()
        )

        assertEquals(TrackRecordingActionGateStep.APPLY_TRACK_ACTION, step)
    }

    private val reliableLocationSnapshot = TrailMateLocationSnapshot(
        status = TrailMateLocationStatus.LOCATED,
        latitude = 30.25,
        longitude = 120.15,
        elevationMeters = 142.0,
        horizontalAccuracyMeters = 12.0,
        timestampEpochMillis = NOW_EPOCH_MILLIS
    )

    private companion object {
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
