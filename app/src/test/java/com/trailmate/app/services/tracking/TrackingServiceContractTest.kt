package com.trailmate.app.services.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingServiceContractTest {
    @Test
    fun startAndStopActionsAreStableAndExplicit() {
        assertEquals(
            "com.trailmate.app.tracking.action.START",
            TrackingServiceIntents.ActionStart,
        )
        assertEquals(
            "com.trailmate.app.tracking.action.STOP",
            TrackingServiceIntents.ActionStop,
        )
        assertEquals(1001, TrackingServiceIntents.ForegroundNotificationId)
    }

    @Test
    fun activeNotificationContentIsClearAndDoesNotPromiseRescue() {
        val content = TrackingNotificationContent.active()
        val combinedText = "${content.title} ${content.text} ${content.channelName}"

        assertEquals("TrailMate 导航进行中", content.title)
        assertTrue(content.text.contains("正在保持轨迹导航"))
        listOf("自动救援", "救援正在赶来", "保证安全", "已联系救援").forEach { unsafeClaim ->
            assertFalse(combinedText.contains(unsafeClaim))
        }
    }

    @Test
    fun controllerStartsForegroundThenLocationUpdatesWhenPermissionIsReady() {
        val controller = TrackingServiceController()
        val actions = controller.handle(
            action = TrackingServiceIntents.ActionStart,
            canStartLocationForeground = true,
            hasTrackingStartContext = true,
        )

        assertEquals(
            listOf(
                TrackingServiceCommand.StartForeground,
                TrackingServiceCommand.StartLocationUpdates,
            ),
            actions.commands,
        )
        assertEquals(TrackingServiceStartResult.NotSticky, actions.startResult)
    }

    @Test
    fun controllerStopsWhenStartActionLacksLocationPermission() {
        val controller = TrackingServiceController()
        val actions = controller.handle(
            action = TrackingServiceIntents.ActionStart,
            canStartLocationForeground = false,
            hasTrackingStartContext = true,
        )

        assertEquals(
            listOf(
                TrackingServiceCommand.StopLocationUpdates,
                TrackingServiceCommand.StopForeground,
                TrackingServiceCommand.StopSelf,
            ),
            actions.commands,
        )
        assertEquals(TrackingServiceStartResult.NotSticky, actions.startResult)
    }

    @Test
    fun controllerStopsWhenStartActionLacksRouteSessionContext() {
        val controller = TrackingServiceController()
        val actions = controller.handle(
            action = TrackingServiceIntents.ActionStart,
            canStartLocationForeground = true,
            hasTrackingStartContext = false,
        )

        assertEquals(
            listOf(
                TrackingServiceCommand.StopLocationUpdates,
                TrackingServiceCommand.StopForeground,
                TrackingServiceCommand.StopSelf,
            ),
            actions.commands,
        )
        assertEquals(TrackingServiceStartResult.NotSticky, actions.startResult)
    }

    @Test
    fun controllerStopsForStopNullAndUnknownActions() {
        val controller = TrackingServiceController()

        listOf(
            TrackingServiceIntents.ActionStop,
            null,
            "unexpected",
        ).forEach { action ->
            val actions = controller.handle(
                action = action,
                canStartLocationForeground = true,
                hasTrackingStartContext = true,
            )

            assertEquals(
                listOf(
                    TrackingServiceCommand.StopLocationUpdates,
                    TrackingServiceCommand.StopForeground,
                    TrackingServiceCommand.StopSelf,
                ),
                actions.commands,
            )
            assertEquals(TrackingServiceStartResult.NotSticky, actions.startResult)
        }
    }
}
