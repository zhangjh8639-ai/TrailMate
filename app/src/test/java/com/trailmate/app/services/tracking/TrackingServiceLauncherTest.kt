package com.trailmate.app.services.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TrackingServiceLauncherTest {
    @Test
    fun startCommandUsesForegroundTrackingServiceStartActionOnly() {
        val command = TrackingServiceLaunchCommand.start()

        assertEquals(TrackingServiceIntents.ActionStart, command.intentAction)
        assertFalse(command.createsFakeLocationPoints)
        assertFalse(command.persistsTrack)
    }

    @Test
    fun stopCommandUsesForegroundTrackingServiceStopActionOnly() {
        val command = TrackingServiceLaunchCommand.stop()

        assertEquals(TrackingServiceIntents.ActionStop, command.intentAction)
        assertFalse(command.createsFakeLocationPoints)
        assertFalse(command.persistsTrack)
    }
}
