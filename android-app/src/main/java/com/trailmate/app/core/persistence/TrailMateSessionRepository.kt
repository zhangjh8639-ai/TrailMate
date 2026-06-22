package com.trailmate.app.core.persistence

import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState

interface TrailMateSessionRepository {
    fun loadSnapshot(): TrailMateSnapshot

    fun saveProfile(profile: BaselineProfile)

    fun saveInventory(inventory: GearInventory)

    fun saveImportedRoute(route: ImportedRoute)

    fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>)

    fun saveGpxImportQueue(queue: GpxImportQueue)

    fun saveTrackRecording(trackRecording: TrackRecordingState)

    fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent)

    fun saveOfflineRoutePackKeys(keys: Set<String>)

    fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>)

    fun clearLocalData()
}

class LocalTrailMateSessionRepository(
    private val store: TrailMateSessionStore
) : TrailMateSessionRepository {
    override fun loadSnapshot(): TrailMateSnapshot =
        store.load()

    override fun saveProfile(profile: BaselineProfile) {
        store.saveProfile(profile)
    }

    override fun saveInventory(inventory: GearInventory) {
        store.saveInventory(inventory)
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

    override fun clearLocalData() {
        store.clear()
    }
}
