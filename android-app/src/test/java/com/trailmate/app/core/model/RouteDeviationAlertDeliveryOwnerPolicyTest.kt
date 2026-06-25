package com.trailmate.app.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationAlertDeliveryOwnerPolicyTest {
    @Test
    fun routeScreenDoesNotDeliverInterruptiveAlertsWhileForegroundRecordingIsActive() {
        assertFalse(
            RouteDeviationAlertDeliveryOwnerPolicy.routeScreenMayDeliver(
                trackRecordingStatus = TrackRecordingStatus.RECORDING
            )
        )
    }

    @Test
    fun routeScreenMayDeliverWhenForegroundRecordingIsNotActive() {
        listOf(
            TrackRecordingStatus.IDLE,
            TrackRecordingStatus.PAUSED,
            TrackRecordingStatus.FINISHED
        ).forEach { status ->
            assertTrue(
                "Expected route screen to own delivery for $status",
                RouteDeviationAlertDeliveryOwnerPolicy.routeScreenMayDeliver(
                    trackRecordingStatus = status
                )
            )
        }
    }
}
