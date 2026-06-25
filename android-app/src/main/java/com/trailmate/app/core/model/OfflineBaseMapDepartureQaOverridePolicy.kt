package com.trailmate.app.core.model

data class OfflineBaseMapDepartureState(
    val downloadedRegionCount: Int?,
    val coversTargetRoute: Boolean,
    val tilesVerifiedWithoutNetwork: Boolean
)

object OfflineBaseMapDepartureQaOverridePolicy {
    fun apply(
        state: OfflineBaseMapDepartureState,
        debugBypassEnabled: Boolean
    ): OfflineBaseMapDepartureState =
        if (!debugBypassEnabled) {
            state
        } else {
            state.copy(
                downloadedRegionCount = maxOf(state.downloadedRegionCount ?: 0, 1),
                coversTargetRoute = true,
                tilesVerifiedWithoutNetwork = true
            )
        }
}
