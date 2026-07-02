package com.trailmate.app.services.tracking

import com.trailmate.app.core.model.NavigationEvent
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.RouteId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingServiceLauncherTest {
    @Test
    fun startCommandUsesForegroundTrackingServiceStartActionWithRealSessionContext() {
        val session = NavigationSession.create(
            id = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAt = Instant.parse("2026-07-01T01:02:03Z"),
        ).reduce(NavigationEvent.StartNavigation)

        val request = TrackingServiceStartRequest.fromSession(session)
        val command = TrackingServiceLaunchCommand.start(request)

        assertEquals(TrackingServiceIntents.ActionStart, command.intentAction)
        assertEquals(session.id, command.sessionId)
        assertEquals(RouteId("longjing"), command.routeId)
        assertEquals(session.startedAt.toEpochMilli(), command.startedAtEpochMillis)
        assertEquals(session.direction, command.direction)
        assertFalse(command.createsFakeLocationPoints)
        assertTrue(command.persistsTrack)
    }

    @Test
    fun stopCommandUsesForegroundTrackingServiceStopActionOnly() {
        val command = TrackingServiceLaunchCommand.stop()

        assertEquals(TrackingServiceIntents.ActionStop, command.intentAction)
        assertFalse(command.createsFakeLocationPoints)
        assertFalse(command.persistsTrack)
    }
}
