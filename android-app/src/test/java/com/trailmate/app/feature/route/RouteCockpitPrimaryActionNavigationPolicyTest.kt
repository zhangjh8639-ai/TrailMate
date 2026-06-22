package com.trailmate.app.feature.route

import com.trailmate.app.core.model.RouteCockpitPrimaryActionKind
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteCockpitPrimaryActionNavigationPolicyTest {
    @Test
    fun locationRepairActionsStayInCockpitInsteadOfOnlyEnteringFullscreen() {
        assertFalse(RouteCockpitPrimaryActionKind.REQUEST_LOCATION.opensFullscreenFromCockpit())
        assertFalse(RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS.opensFullscreenFromCockpit())
    }

    @Test
    fun fieldSessionActionsCanEnterFullscreenFromCockpit() {
        assertTrue(RouteCockpitPrimaryActionKind.START_HIKE.opensFullscreenFromCockpit())
        assertTrue(RouteCockpitPrimaryActionKind.START_RECORDING.opensFullscreenFromCockpit())
        assertTrue(RouteCockpitPrimaryActionKind.PAUSE_RECORDING.opensFullscreenFromCockpit())
        assertTrue(RouteCockpitPrimaryActionKind.RESUME_RECORDING.opensFullscreenFromCockpit())
    }
}
