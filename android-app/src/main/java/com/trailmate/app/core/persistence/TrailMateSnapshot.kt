package com.trailmate.app.core.persistence

import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState

data class TrailMateSnapshot(
    val authSession: TrailMateAuthSession? = null,
    val profile: BaselineProfile? = null,
    val importedRoute: ImportedRoute? = null,
    val historicalActivities: List<HistoricalActivity> = emptyList(),
    val gpxImportQueue: GpxImportQueue = GpxImportQueue(),
    val latestTrackRecording: TrackRecordingState = TrackRecordingState(),
    val savedOfflineRoutePackKeys: Set<String> = emptySet(),
    val offlineBaseMapTileProofs: List<AmapOfflineBaseMapTileProof> = emptyList(),
    val amapPrivacyConsent: AmapPrivacyConsent = AmapPrivacyConsent()
) {
    companion object {
        fun empty(): TrailMateSnapshot =
            TrailMateSnapshot(
                authSession = null,
                profile = null,
                importedRoute = null,
                historicalActivities = emptyList(),
                gpxImportQueue = GpxImportQueue(),
                latestTrackRecording = TrackRecordingState(),
                savedOfflineRoutePackKeys = emptySet(),
                offlineBaseMapTileProofs = emptyList(),
                amapPrivacyConsent = AmapPrivacyConsent()
            )
    }
}
