package com.trailmate.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateLocationActivationEngineTest {
    @Test
    fun opensSystemSettingsWhenPermissionExistsButProviderIsDisabled() {
        val action = TrailMateLocationActivationEngine.resolveRequestAction(
            hasForegroundPermission = true,
            hasEnabledProvider = false
        )

        assertEquals(TrailMateLocationActivationAction.OPEN_SYSTEM_LOCATION_SETTINGS, action)
    }

    @Test
    fun startsTrackingWhenPermissionAndProviderAreReady() {
        val action = TrailMateLocationActivationEngine.resolveRequestAction(
            hasForegroundPermission = true,
            hasEnabledProvider = true
        )

        assertEquals(TrailMateLocationActivationAction.START_TRACKING, action)
    }

    @Test
    fun requestsPermissionBeforeCheckingProvider() {
        val action = TrailMateLocationActivationEngine.resolveRequestAction(
            hasForegroundPermission = false,
            hasEnabledProvider = false
        )

        assertEquals(TrailMateLocationActivationAction.REQUEST_PERMISSION, action)
    }

    @Test
    fun opensAppSettingsWhenPrecisePermissionWasDeniedPermanently() {
        val action = TrailMateLocationActivationEngine.resolveRequestAction(
            hasForegroundPermission = false,
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true,
            hasRequestedLocationPermissionBefore = true,
            shouldShowPreciseLocationRationale = false
        )

        assertEquals(TrailMateLocationActivationAction.OPEN_APP_LOCATION_SETTINGS, action)
    }

    @Test
    fun requestsPermissionAgainWhenSystemCanStillShowPrecisePermissionDialog() {
        val action = TrailMateLocationActivationEngine.resolveRequestAction(
            hasForegroundPermission = false,
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true,
            hasRequestedLocationPermissionBefore = true,
            shouldShowPreciseLocationRationale = true
        )

        assertEquals(TrailMateLocationActivationAction.REQUEST_PERMISSION, action)
    }

    @Test
    fun requestsPrecisePermissionBeforeStartingOutdoorLocation() {
        val action = TrailMateLocationActivationEngine.resolveRequestAction(
            hasForegroundPermission = true,
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true
        )

        assertEquals(TrailMateLocationActivationAction.REQUEST_PERMISSION, action)
    }

    @Test
    fun retriesTrackingAfterReturningFromSettingsWhenProviderIsReady() {
        val action = TrailMateLocationActivationEngine.resolveSettingsReturnAction(
            pendingSettingsReturn = true,
            hasForegroundPermission = true,
            hasPreciseLocationPermission = true,
            hasEnabledProvider = true
        )

        assertEquals(TrailMateLocationActivationAction.START_TRACKING, action)
    }

    @Test
    fun requestsPrecisePermissionAfterReturningFromSettingsWithApproximateOnlyPermission() {
        val action = TrailMateLocationActivationEngine.resolveSettingsReturnAction(
            pendingSettingsReturn = true,
            hasForegroundPermission = true,
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true
        )

        assertEquals(TrailMateLocationActivationAction.REQUEST_PERMISSION, action)
    }

    @Test
    fun staysProviderDisabledAfterReturningFromSettingsWhenProviderIsStillOff() {
        val action = TrailMateLocationActivationEngine.resolveSettingsReturnAction(
            pendingSettingsReturn = true,
            hasForegroundPermission = true,
            hasEnabledProvider = false
        )

        assertEquals(TrailMateLocationActivationAction.SHOW_PROVIDER_DISABLED, action)
    }

    @Test
    fun waitsForReturnWhenGeneralSettingsFallbackOpens() {
        val outcome = TrailMateLocationSettingsLaunchPolicy.resolve(
            locationSettingsOpened = false,
            generalSettingsOpened = true
        )

        assertEquals(TrailMateLocationSettingsLaunchOutcome.WAIT_FOR_RETURN, outcome)
    }

    @Test
    fun reportsUnavailableWhenNoSystemSettingsScreenCanOpen() {
        val outcome = TrailMateLocationSettingsLaunchPolicy.resolve(
            locationSettingsOpened = false,
            generalSettingsOpened = false
        )

        assertEquals(TrailMateLocationSettingsLaunchOutcome.SHOW_UNAVAILABLE, outcome)
    }

    @Test
    fun appSettingsReturnStartsTrackingWhenPrecisePermissionAndProviderAreReady() {
        val effect = TrailMateLocationAppSettingsReturnEffectEngine.resolve(
            pendingAppSettingsReturn = true,
            hasForegroundPermission = true,
            hasPreciseLocationPermission = true,
            hasEnabledProvider = true
        )

        assertEquals(TrailMateLocationAppSettingsReturnEffect.START_TRACKING, effect)
    }

    @Test
    fun appSettingsReturnShowsPermissionRequiredWithoutLoopingWhenPrecisePermissionIsStillMissing() {
        val effect = TrailMateLocationAppSettingsReturnEffectEngine.resolve(
            pendingAppSettingsReturn = true,
            hasForegroundPermission = false,
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true
        )

        assertEquals(TrailMateLocationAppSettingsReturnEffect.SHOW_PERMISSION_REQUIRED, effect)
    }

    @Test
    fun appSettingsReturnShowsProviderDisabledAfterPermissionIsGrantedButGpsIsOff() {
        val effect = TrailMateLocationAppSettingsReturnEffectEngine.resolve(
            pendingAppSettingsReturn = true,
            hasForegroundPermission = true,
            hasPreciseLocationPermission = true,
            hasEnabledProvider = false
        )

        assertEquals(TrailMateLocationAppSettingsReturnEffect.SHOW_PROVIDER_DISABLED, effect)
    }
}
