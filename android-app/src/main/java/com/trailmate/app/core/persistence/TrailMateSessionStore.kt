package com.trailmate.app.core.persistence

import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState

interface TrailMateSessionStore {
    fun load(): TrailMateSnapshot

    fun saveProfile(profile: BaselineProfile)

    fun saveInventory(inventory: GearInventory)

    fun saveImportedRoute(route: ImportedRoute)

    fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>)

    fun saveGpxImportQueue(queue: GpxImportQueue)

    fun saveTrackRecording(trackRecording: TrackRecordingState)

    fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent)

    fun saveOfflineRoutePackKeys(keys: Set<String>)

    fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>)

    fun clear()
}
