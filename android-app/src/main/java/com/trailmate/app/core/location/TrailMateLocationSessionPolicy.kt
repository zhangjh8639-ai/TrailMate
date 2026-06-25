package com.trailmate.app.core.location

object TrailMateLocationSessionPolicy {
    fun shouldKeepLocationRequestActive(snapshot: TrailMateLocationSnapshot): Boolean =
        when (snapshot.status) {
            TrailMateLocationStatus.SEARCHING,
            TrailMateLocationStatus.LOCATED,
            TrailMateLocationStatus.LOW_ACCURACY -> true
            TrailMateLocationStatus.DISABLED,
            TrailMateLocationStatus.PERMISSION_REQUIRED,
            TrailMateLocationStatus.PROVIDER_DISABLED,
            TrailMateLocationStatus.UNAVAILABLE -> false
        }
}
