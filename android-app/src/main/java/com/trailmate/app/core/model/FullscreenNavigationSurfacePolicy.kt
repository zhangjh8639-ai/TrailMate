package com.trailmate.app.core.model

object FullscreenNavigationSurfacePolicy {
    private val allowedFirstViewportSurfaces = setOf(
        "map",
        "current_location",
        "route_polyline",
        "checkpoints",
        "recorded_track",
        "current_checkpoint",
        "next_checkpoint",
        "gps_status",
        "recording_status",
        "base_map_status",
        "gear_status",
        "recording_action",
        "safety_share"
    )

    fun isAllowedInFirstViewport(surfaceId: String): Boolean =
        surfaceId in allowedFirstViewportSurfaces
}
