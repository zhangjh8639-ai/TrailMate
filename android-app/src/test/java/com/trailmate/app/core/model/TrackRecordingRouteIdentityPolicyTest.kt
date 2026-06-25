package com.trailmate.app.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecordingRouteIdentityPolicyTest {
    @Test
    fun matchesRecordingWhenRouteNameAndRouteKeyMatch() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊",
            routeKey = "longjing.gpx|龙井山脊|15.2|860|128"
        )

        assertTrue(
            TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
                trackRecording = recording,
                routeName = "龙井山脊",
                routeKey = "longjing.gpx|龙井山脊|15.2|860|128"
            )
        )
    }

    @Test
    fun rejectsSameNameRecordingWhenRouteKeyDiffers() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊",
            routeKey = "old.gpx|龙井山脊|8.0|300|80"
        )

        assertFalse(
            TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
                trackRecording = recording,
                routeName = "龙井山脊",
                routeKey = "new.gpx|龙井山脊|15.2|860|128"
            )
        )
    }

    @Test
    fun legacyRecordingWithoutRouteKeyFallsBackToRouteName() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊"
        )

        assertTrue(
            TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
                trackRecording = recording,
                routeName = "龙井山脊",
                routeKey = "longjing.gpx|龙井山脊|15.2|860|128"
            )
        )
    }

    @Test
    fun rejectsDifferentRouteNameWhenRouteKeyIsNotAvailable() {
        val recording = TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = "龙井山脊"
        )

        assertFalse(
            TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
                trackRecording = recording,
                routeName = "别的路线",
                routeKey = null
            )
        )
    }
}
