package com.trailmate.app.feature.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavigationTrackingStartReducerTest {
    private val reducer = NavigationTrackingStartReducer()

    @Test
    fun startRequestsPermissionWhenStartPermissionsAreMissing() {
        val decision = reducer.onStartClicked(hasRequiredStartPermissions = false)

        assertEquals(TrackingStartMode.PermissionRequired, decision.state.mode)
        assertEquals(TrackingStartEffect.RequestPermissions, decision.effect)
        assertEquals("需要定位权限", decision.state.title)
    }

    @Test
    fun startLaunchesServiceWhenStartPermissionsAreGranted() {
        val decision = reducer.onStartClicked(hasRequiredStartPermissions = true)

        assertEquals(TrackingStartMode.Starting, decision.state.mode)
        assertEquals(TrackingStartEffect.StartTrackingService, decision.effect)
        assertEquals("正在请求启动前台导航", decision.state.title)
    }

    @Test
    fun permissionGrantAfterRequestLaunchesService() {
        val decision = reducer.onPermissionResult(
            TrackingPermissionGrantResult(
                hasForegroundLocation = true,
                hasNotificationPermission = true,
            ),
        )

        assertEquals(TrackingStartMode.Starting, decision.state.mode)
        assertEquals(TrackingStartEffect.StartTrackingService, decision.effect)
    }

    @Test
    fun permissionDenialKeepsRouteVisibleWithoutStartingService() {
        val decision = reducer.onPermissionResult(
            TrackingPermissionGrantResult(
                hasForegroundLocation = false,
                hasNotificationPermission = true,
            ),
        )

        assertEquals(TrackingStartMode.PermissionDenied, decision.state.mode)
        assertNull(decision.effect)
        assertEquals("定位权限未开启", decision.state.title)
    }

    @Test
    fun notificationDenialDoesNotStartForegroundServiceOnAndroid13Plus() {
        val decision = reducer.onPermissionResult(
            TrackingPermissionGrantResult(
                hasForegroundLocation = true,
                hasNotificationPermission = false,
            ),
        )

        assertEquals(TrackingStartMode.NotificationDenied, decision.state.mode)
        assertNull(decision.effect)
        assertEquals("通知权限未开启", decision.state.title)
    }

    @Test
    fun stopSendsStopServiceAndWaitsForServiceClear() {
        val decision = reducer.onStopClicked()

        assertEquals(TrackingStartMode.Stopping, decision.state.mode)
        assertEquals(TrackingStartEffect.StopTrackingService, decision.effect)
        assertEquals("正在结束前台导航", decision.state.title)
    }

    @Test
    fun activeUiStateCanBeRebuiltFromSavedMode() {
        val state = TrackingStartUiState.fromMode(TrackingStartMode.Active)

        assertEquals("前台导航服务运行中", state.title)
        assertEquals("停止前台导航", state.secondaryActionLabel)
    }

    @Test
    fun startingUiStateDoesNotClaimServiceIsAlreadyRunning() {
        val state = TrackingStartUiState.fromMode(TrackingStartMode.Starting)

        assertEquals("正在请求启动前台导航", state.title)
        assertNull(state.primaryActionLabel)
        assertNull(state.secondaryActionLabel)
    }
}
