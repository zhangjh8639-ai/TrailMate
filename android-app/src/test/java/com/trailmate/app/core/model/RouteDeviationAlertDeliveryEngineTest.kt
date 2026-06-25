package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationAlertDeliveryEngineTest {
    @Test
    fun urgentOffRouteDecisionPostsAndVibratesWhenAllowed() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE,
                shouldNotify = true,
                shouldVibrate = true,
                title = "疑似偏离路线",
                caption = "请先停下，当前位置距计划路线约 112 m。核对地图、路标和现场路径后再继续。"
            ),
            notificationPermissionGranted = true,
            deviceCanVibrate = true
        )

        assertTrue(plan.shouldPostNotification)
        assertTrue(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.URGENT, plan.tone)
        assertEquals(RouteDeviationAlertDeliveryChannel.URGENT_ALERT, plan.channel)
        assertEquals("TrailMate 偏航提醒", plan.notificationTitle)
        assertTrue(plan.notificationText.contains("停下"))
        assertFalse(plan.notificationText.contains("救援"))
        assertFalse(plan.notificationText.contains("重新规划"))
        assertFalse(plan.notificationText.contains("安全保障"))
        assertNull(plan.inAppOnlyReason)
    }

    @Test
    fun nonInterruptingDecisionsDoNotPostOrVibrate() {
        listOf(
            RouteDeviationAlertKind.OFF_ROUTE_SILENT,
            RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX,
            RouteDeviationAlertKind.NONE
        ).forEach { kind ->
            val plan = RouteDeviationAlertDeliveryEngine.resolve(
                decision = decision(
                    kind = kind,
                    shouldNotify = kind != RouteDeviationAlertKind.NONE,
                    shouldVibrate = kind != RouteDeviationAlertKind.NONE,
                    title = "偏离恢复中",
                    caption = "继续核对路线，暂不重复提醒。当前位置距计划路线约 125 m。"
                ),
                notificationPermissionGranted = true,
                deviceCanVibrate = true
            )

            assertFalse(plan.shouldPostNotification)
            assertFalse(plan.shouldVibrate)
            assertEquals(RouteDeviationAlertDeliveryTone.NONE, plan.tone)
        }
    }

    @Test
    fun missingNotificationPermissionUsesInAppFallbackAndVibration() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE_ESCALATED,
                shouldNotify = true,
                shouldVibrate = true,
                title = "偏离距离增加",
                caption = "你可能正在远离计划路线，当前偏离约 172 m。请停下确认是否需要原路返回。"
            ),
            notificationPermissionGranted = false,
            deviceCanVibrate = true
        )

        assertFalse(plan.shouldPostNotification)
        assertTrue(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.URGENT, plan.tone)
        assertEquals("通知权限未开启，TrailMate 只能在路线页内显示偏航提醒。", plan.inAppOnlyReason)
    }

    @Test
    fun urgentDecisionDoesNotVibrateWhenDeviceCannotVibrate() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.OFF_ROUTE,
                shouldNotify = true,
                shouldVibrate = true,
                title = "疑似偏离路线",
                caption = "请先停下，当前位置距计划路线约 112 m。核对地图、路标和现场路径后再继续。"
            ),
            notificationPermissionGranted = true,
            deviceCanVibrate = false
        )

        assertTrue(plan.shouldPostNotification)
        assertFalse(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.URGENT, plan.tone)
    }

    @Test
    fun rejoinedDecisionCanNotifyWithoutVibration() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.REJOINED_ROUTE,
                shouldNotify = true,
                shouldVibrate = false,
                title = "已回到路线",
                caption = "当前位置已回到计划路线附近，请确认下一检查点后继续。"
            ),
            notificationPermissionGranted = true,
            deviceCanVibrate = true
        )

        assertTrue(plan.shouldPostNotification)
        assertFalse(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.REJOINED, plan.tone)
        assertEquals(RouteDeviationAlertDeliveryChannel.ROUTE_STATUS, plan.channel)
        assertEquals("TrailMate 路线提醒", plan.notificationTitle)
        assertTrue(plan.notificationText.contains("已回到路线"))
    }

    @Test
    fun rejoinedDecisionNeverVibratesEvenIfDecisionInputIsMalformed() {
        val plan = RouteDeviationAlertDeliveryEngine.resolve(
            decision = decision(
                kind = RouteDeviationAlertKind.REJOINED_ROUTE,
                shouldNotify = true,
                shouldVibrate = true,
                title = "已回到路线",
                caption = "当前位置已回到计划路线附近，请确认下一检查点后继续。"
            ),
            notificationPermissionGranted = true,
            deviceCanVibrate = true
        )

        assertTrue(plan.shouldPostNotification)
        assertFalse(plan.shouldVibrate)
        assertEquals(RouteDeviationAlertDeliveryTone.REJOINED, plan.tone)
    }

    private fun decision(
        kind: RouteDeviationAlertKind,
        shouldNotify: Boolean,
        shouldVibrate: Boolean,
        title: String,
        caption: String
    ) = RouteDeviationAlertDecision(
        kind = kind,
        shouldNotify = shouldNotify,
        shouldVibrate = shouldVibrate,
        title = title,
        caption = caption,
        primaryActionLabel = "查看路线",
        nextState = RouteDeviationAlertState()
    )
}
