package com.trailmate.app.core.persistence

import android.content.Context
import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState

class SharedPreferencesTrailMateSessionStore(context: Context) : TrailMateSessionStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun load(): TrailMateSnapshot =
        preferences.getString(KEY_SNAPSHOT, null)
            ?.let(TrailMateSnapshotCodec::decode)
            ?: TrailMateSnapshot()

    override fun saveAuthSession(session: TrailMateAuthSession) {
        update { snapshot -> snapshot.copy(authSession = session) }
    }

    override fun clearAuthSession() {
        update { snapshot -> snapshot.copy(authSession = null) }
    }

    override fun saveProfile(profile: BaselineProfile) {
        update { snapshot -> snapshot.copy(profile = profile) }
    }

    override fun saveImportedRoute(route: ImportedRoute) {
        update { snapshot -> snapshot.copy(importedRoute = route) }
    }

    override fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>) {
        update { snapshot -> snapshot.copy(historicalActivities = historicalActivities) }
    }

    override fun saveGpxImportQueue(queue: GpxImportQueue) {
        update { snapshot -> snapshot.copy(gpxImportQueue = queue) }
    }

    override fun saveTrackRecording(trackRecording: TrackRecordingState) {
        update { snapshot -> snapshot.copy(latestTrackRecording = trackRecording) }
    }

    override fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent) {
        update { snapshot -> snapshot.copy(amapPrivacyConsent = consent) }
    }

    override fun saveOfflineRoutePackKeys(keys: Set<String>) {
        update { snapshot -> snapshot.copy(savedOfflineRoutePackKeys = keys) }
    }

    override fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>) {
        update { snapshot -> snapshot.copy(offlineBaseMapTileProofs = proofs) }
    }

    override fun clear() {
        preferences.edit()
            .putString(KEY_SNAPSHOT, TrailMateSnapshotCodec.encode(TrailMateSnapshot.empty()))
            .apply()
    }

    private fun update(transform: (TrailMateSnapshot) -> TrailMateSnapshot) {
        val updated = transform(load())
        preferences.edit()
            .putString(KEY_SNAPSHOT, TrailMateSnapshotCodec.encode(updated))
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "trailmate_session"
        const val KEY_SNAPSHOT = "snapshot"
    }
}
