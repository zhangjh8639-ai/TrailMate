package com.trailmate.app.core.persistence

import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState

interface TrailMateSessionRepository {
    fun loadSnapshot(): TrailMateSnapshot

    fun saveAuthSession(session: TrailMateAuthSession)

    fun clearAuthSession()

    fun saveProfile(profile: BaselineProfile)

    fun saveImportedRoute(route: ImportedRoute)

    fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>)

    fun saveGpxImportQueue(queue: GpxImportQueue)

    fun saveTrackRecording(trackRecording: TrackRecordingState)

    fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent)

    fun saveOfflineRoutePackKeys(keys: Set<String>)

    fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>)

    fun saveAiGearAdvisorResponse(response: AiGearAdvisorResponse?)

    fun clearLocalData()
}

class LocalTrailMateSessionRepository(
    private val store: TrailMateSessionStore
) : TrailMateSessionRepository {
    override fun loadSnapshot(): TrailMateSnapshot =
        store.load()

    override fun saveAuthSession(session: TrailMateAuthSession) {
        store.saveAuthSession(session)
    }

    override fun clearAuthSession() {
        store.clearAuthSession()
    }

    override fun saveProfile(profile: BaselineProfile) {
        store.saveProfile(profile)
    }

    override fun saveImportedRoute(route: ImportedRoute) {
        store.saveImportedRoute(route)
    }

    override fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>) {
        store.saveHistoricalActivities(historicalActivities)
    }

    override fun saveGpxImportQueue(queue: GpxImportQueue) {
        store.saveGpxImportQueue(queue)
    }

    override fun saveTrackRecording(trackRecording: TrackRecordingState) {
        store.saveTrackRecording(trackRecording)
    }

    override fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent) {
        store.saveAmapPrivacyConsent(consent)
    }

    override fun saveOfflineRoutePackKeys(keys: Set<String>) {
        store.saveOfflineRoutePackKeys(keys)
    }

    override fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>) {
        store.saveOfflineBaseMapTileProofs(proofs)
    }

    override fun saveAiGearAdvisorResponse(response: AiGearAdvisorResponse?) {
        store.saveAiGearAdvisorResponse(response)
    }

    override fun clearLocalData() {
        store.clear()
    }
}
