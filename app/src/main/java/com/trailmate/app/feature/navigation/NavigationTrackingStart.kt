package com.trailmate.app.feature.navigation

data class TrackingStartUiState(
    val mode: TrackingStartMode,
    val title: String,
    val body: String,
    val primaryActionLabel: String?,
    val secondaryActionLabel: String?,
) {
    fun visibleText(): List<String> =
        listOfNotNull(title, body, primaryActionLabel, secondaryActionLabel)

    companion object {
        fun ready(): TrackingStartUiState =
            TrackingStartUiState(
                mode = TrackingStartMode.Ready,
                title = "准备开始轨迹导航",
                body = "开始时会请求定位权限，用于持续获取当前位置；授权前不会记录轨迹点。",
                primaryActionLabel = "开始轨迹导航",
                secondaryActionLabel = null,
            )

        fun permissionRequired(): TrackingStartUiState =
            TrackingStartUiState(
                mode = TrackingStartMode.PermissionRequired,
                title = "需要定位权限",
                body = "TrailMate 需要当前位置来记录真实轨迹点；后续可用于偏航判断和复盘。",
                primaryActionLabel = "继续授权",
                secondaryActionLabel = null,
            )

        fun active(): TrackingStartUiState =
            TrackingStartUiState(
                mode = TrackingStartMode.Active,
                title = "前台导航服务运行中",
                body = "正在通过前台服务接收真实定位并记录轨迹点；锁屏后仍会保持运行状态。",
                primaryActionLabel = null,
                secondaryActionLabel = "停止前台导航",
            )

        fun starting(): TrackingStartUiState =
            TrackingStartUiState(
                mode = TrackingStartMode.Starting,
                title = "正在请求启动前台导航",
                body = "系统正在启动前台导航服务；确认运行前不会记录为已启动。",
                primaryActionLabel = null,
                secondaryActionLabel = null,
            )

        fun stopping(): TrackingStartUiState =
            TrackingStartUiState(
                mode = TrackingStartMode.Stopping,
                title = "正在结束前台导航",
                body = "正在停止前台导航服务；完成前不会开始新的轨迹导航。",
                primaryActionLabel = null,
                secondaryActionLabel = null,
            )

        fun permissionDenied(): TrackingStartUiState =
            TrackingStartUiState(
                mode = TrackingStartMode.PermissionDenied,
                title = "定位权限未开启",
                body = "未获得定位权限，无法持续获取当前位置；你仍可以查看路线信息。",
                primaryActionLabel = "重新授权定位",
                secondaryActionLabel = null,
            )

        fun notificationDenied(): TrackingStartUiState =
            TrackingStartUiState(
                mode = TrackingStartMode.NotificationDenied,
                title = "通知权限未开启",
                body = "未获得通知权限，无法在锁屏或后台清楚显示导航运行状态；请重新授权后再开始。",
                primaryActionLabel = "重新授权通知",
                secondaryActionLabel = null,
            )

        fun fromMode(mode: TrackingStartMode): TrackingStartUiState =
            when (mode) {
                TrackingStartMode.Ready -> ready()
                TrackingStartMode.Starting -> starting()
                TrackingStartMode.PermissionRequired -> permissionRequired()
                TrackingStartMode.Active -> active()
                TrackingStartMode.Stopping -> stopping()
                TrackingStartMode.PermissionDenied -> permissionDenied()
                TrackingStartMode.NotificationDenied -> notificationDenied()
            }
    }
}

enum class TrackingStartMode {
    Ready,
    Starting,
    PermissionRequired,
    Active,
    Stopping,
    PermissionDenied,
    NotificationDenied,
}

enum class TrackingStartEffect {
    RequestPermissions,
    StartTrackingService,
    StopTrackingService,
}

data class TrackingStartDecision(
    val state: TrackingStartUiState,
    val effect: TrackingStartEffect?,
)

class NavigationTrackingStartReducer {
    fun onStartClicked(hasRequiredStartPermissions: Boolean): TrackingStartDecision =
        if (hasRequiredStartPermissions) {
            TrackingStartDecision(
                state = TrackingStartUiState.starting(),
                effect = TrackingStartEffect.StartTrackingService,
            )
        } else {
            TrackingStartDecision(
                state = TrackingStartUiState.permissionRequired(),
                effect = TrackingStartEffect.RequestPermissions,
            )
        }

    fun onPermissionResult(grantResult: TrackingPermissionGrantResult): TrackingStartDecision =
        when {
            grantResult.canStartTracking -> TrackingStartDecision(
                state = TrackingStartUiState.starting(),
                effect = TrackingStartEffect.StartTrackingService,
            )
            !grantResult.hasForegroundLocation -> TrackingStartDecision(
                state = TrackingStartUiState.permissionDenied(),
                effect = null,
            )
            else -> TrackingStartDecision(
                state = TrackingStartUiState.notificationDenied(),
                effect = null,
            )
        }

    fun onStopClicked(): TrackingStartDecision =
        TrackingStartDecision(
            state = TrackingStartUiState.stopping(),
            effect = TrackingStartEffect.StopTrackingService,
        )
}
