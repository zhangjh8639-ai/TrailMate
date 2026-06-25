package com.trailmate.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateLocationSettingsReturnEffectTest {
    @Test
    fun requestsPermissionWhenReturningFromSystemSettingsWithoutPrecisePermission() {
        val effect = TrailMateLocationSettingsReturnEffectEngine.resolve(
            pendingSettingsReturn = true,
            hasForegroundPermission = true,
            hasPreciseLocationPermission = false,
            hasEnabledProvider = true
        )

        assertEquals(TrailMateLocationSettingsReturnEffect.REQUEST_PERMISSION, effect)
    }
}
