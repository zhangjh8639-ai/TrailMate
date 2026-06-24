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
    fun startHikeStaysInCockpitInsteadOfEnteringFullscreenImmediately() {
        assertFalse(RouteCockpitPrimaryActionKind.START_HIKE.opensFullscreenFromCockpit())
    }

    @Test
    fun fieldRecordingActionsCanEnterFullscreenFromCockpit() {
        assertTrue(RouteCockpitPrimaryActionKind.START_RECORDING.opensFullscreenFromCockpit())
        assertTrue(RouteCockpitPrimaryActionKind.PAUSE_RECORDING.opensFullscreenFromCockpit())
        assertTrue(RouteCockpitPrimaryActionKind.RESUME_RECORDING.opensFullscreenFromCockpit())
    }

    @Test
    fun fullscreenShortcutIsHiddenUntilHikeIsInProgress() {
        assertFalse(RouteCockpitPrimaryActionKind.START_HIKE.showsFullscreenShortcutInActionDrawer())
        assertFalse(RouteCockpitPrimaryActionKind.REQUEST_LOCATION.showsFullscreenShortcutInActionDrawer())
        assertFalse(RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP.showsFullscreenShortcutInActionDrawer())

        assertTrue(RouteCockpitPrimaryActionKind.START_RECORDING.showsFullscreenShortcutInActionDrawer())
        assertTrue(RouteCockpitPrimaryActionKind.PAUSE_RECORDING.showsFullscreenShortcutInActionDrawer())
        assertTrue(RouteCockpitPrimaryActionKind.RESUME_RECORDING.showsFullscreenShortcutInActionDrawer())
    }
}
