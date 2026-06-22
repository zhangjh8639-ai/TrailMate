package com.trailmate.app.core.location

enum class TrailMateLocationActivationAction {
    REQUEST_PERMISSION,
    OPEN_APP_LOCATION_SETTINGS,
    OPEN_SYSTEM_LOCATION_SETTINGS,
    START_TRACKING,
    SHOW_PROVIDER_DISABLED,
    NONE
}

enum class TrailMateLocationAppSettingsReturnEffect {
    START_TRACKING,
    SHOW_PROVIDER_DISABLED,
    SHOW_PERMISSION_REQUIRED,
    NONE
}

enum class TrailMateLocationSettingsReturnEffect {
    REQUEST_PERMISSION,
    START_TRACKING,
    SHOW_PROVIDER_DISABLED,
    NONE
}

enum class TrailMateLocationSettingsLaunchOutcome {
    WAIT_FOR_RETURN,
    SHOW_UNAVAILABLE
}

object TrailMateLocationActivationEngine {
    fun resolveRequestAction(
        hasForegroundPermission: Boolean,
        hasPreciseLocationPermission: Boolean = hasForegroundPermission,
        hasEnabledProvider: Boolean,
        hasRequestedLocationPermissionBefore: Boolean = false,
        shouldShowPreciseLocationRationale: Boolean = true
    ): TrailMateLocationActivationAction =
        when {
            !hasForegroundPermission || !hasPreciseLocationPermission ->
                if (hasRequestedLocationPermissionBefore && !shouldShowPreciseLocationRationale) {
                    TrailMateLocationActivationAction.OPEN_APP_LOCATION_SETTINGS
                } else {
                    TrailMateLocationActivationAction.REQUEST_PERMISSION
                }
            hasEnabledProvider -> TrailMateLocationActivationAction.START_TRACKING
            else -> TrailMateLocationActivationAction.OPEN_SYSTEM_LOCATION_SETTINGS
        }

    fun resolveSettingsReturnAction(
        pendingSettingsReturn: Boolean,
        hasForegroundPermission: Boolean,
        hasPreciseLocationPermission: Boolean = hasForegroundPermission,
        hasEnabledProvider: Boolean
    ): TrailMateLocationActivationAction =
        if (!pendingSettingsReturn) {
            TrailMateLocationActivationAction.NONE
        } else if (!hasForegroundPermission || !hasPreciseLocationPermission) {
            TrailMateLocationActivationAction.REQUEST_PERMISSION
        } else if (hasEnabledProvider) {
            TrailMateLocationActivationAction.START_TRACKING
        } else {
            TrailMateLocationActivationAction.SHOW_PROVIDER_DISABLED
        }
}

object TrailMateLocationSettingsReturnEffectEngine {
    fun resolve(
        pendingSettingsReturn: Boolean,
        hasForegroundPermission: Boolean,
        hasPreciseLocationPermission: Boolean = hasForegroundPermission,
        hasEnabledProvider: Boolean
    ): TrailMateLocationSettingsReturnEffect =
        when (
            TrailMateLocationActivationEngine.resolveSettingsReturnAction(
                pendingSettingsReturn = pendingSettingsReturn,
                hasForegroundPermission = hasForegroundPermission,
                hasPreciseLocationPermission = hasPreciseLocationPermission,
                hasEnabledProvider = hasEnabledProvider
            )
        ) {
            TrailMateLocationActivationAction.REQUEST_PERMISSION ->
                TrailMateLocationSettingsReturnEffect.REQUEST_PERMISSION
            TrailMateLocationActivationAction.START_TRACKING ->
                TrailMateLocationSettingsReturnEffect.START_TRACKING
            TrailMateLocationActivationAction.SHOW_PROVIDER_DISABLED ->
                TrailMateLocationSettingsReturnEffect.SHOW_PROVIDER_DISABLED
            TrailMateLocationActivationAction.OPEN_APP_LOCATION_SETTINGS,
            TrailMateLocationActivationAction.OPEN_SYSTEM_LOCATION_SETTINGS,
            TrailMateLocationActivationAction.NONE ->
                TrailMateLocationSettingsReturnEffect.NONE
        }
}

object TrailMateLocationAppSettingsReturnEffectEngine {
    fun resolve(
        pendingAppSettingsReturn: Boolean,
        hasForegroundPermission: Boolean,
        hasPreciseLocationPermission: Boolean = hasForegroundPermission,
        hasEnabledProvider: Boolean
    ): TrailMateLocationAppSettingsReturnEffect =
        when {
            !pendingAppSettingsReturn -> TrailMateLocationAppSettingsReturnEffect.NONE
            !hasForegroundPermission || !hasPreciseLocationPermission ->
                TrailMateLocationAppSettingsReturnEffect.SHOW_PERMISSION_REQUIRED
            hasEnabledProvider -> TrailMateLocationAppSettingsReturnEffect.START_TRACKING
            else -> TrailMateLocationAppSettingsReturnEffect.SHOW_PROVIDER_DISABLED
        }
}

object TrailMateLocationSettingsLaunchPolicy {
    fun resolve(
        locationSettingsOpened: Boolean,
        generalSettingsOpened: Boolean
    ): TrailMateLocationSettingsLaunchOutcome =
        if (locationSettingsOpened || generalSettingsOpened) {
            TrailMateLocationSettingsLaunchOutcome.WAIT_FOR_RETURN
        } else {
            TrailMateLocationSettingsLaunchOutcome.SHOW_UNAVAILABLE
        }
}
