package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineBaseMapDepartureQaOverridePolicyTest {
    @Test
    fun keepsRealOfflineBaseMapStateWhenDebugBypassIsDisabled() {
        val state = OfflineBaseMapDepartureState(
            downloadedRegionCount = 0,
            coversTargetRoute = false,
            tilesVerifiedWithoutNetwork = false
        )

        assertEquals(
            state,
            OfflineBaseMapDepartureQaOverridePolicy.apply(
                state = state,
                debugBypassEnabled = false
            )
        )
    }

    @Test
    fun marksOfflineBaseMapReadyForDebugTrackRecordingQa() {
        val result = OfflineBaseMapDepartureQaOverridePolicy.apply(
            state = OfflineBaseMapDepartureState(
                downloadedRegionCount = 0,
                coversTargetRoute = false,
                tilesVerifiedWithoutNetwork = false
            ),
            debugBypassEnabled = true
        )

        assertEquals(1, result.downloadedRegionCount)
        assertEquals(true, result.coversTargetRoute)
        assertEquals(true, result.tilesVerifiedWithoutNetwork)
    }
}
