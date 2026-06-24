package com.trailmate.app.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenNavigationSurfacePolicyTest {
    @Test
    fun firstViewportAllowsOnlyFieldCriticalSurfaces() {
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("map"))
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("current_location"))
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("recording_action"))
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("safety_share"))

        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("amap_launch_diagnostics"))
        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("baseline_profile_evidence"))
        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("ai_gear_explanation"))
        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("offline_map_long_explanation"))
    }
}
