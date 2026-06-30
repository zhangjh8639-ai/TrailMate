package com.trailmate.app

import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.persistence.TrailMateSnapshot

data class TrailMateAppSession(
    val snapshot: TrailMateSnapshot
) {
    val hasAuthenticatedAccount: Boolean
        get() = snapshot.authSession != null

    val hasProfile: Boolean
        get() = snapshot.profile != null

    val isReadyForHome: Boolean
        get() = hasAuthenticatedAccount && hasProfile

    val baselineProfile: BaselineProfile
        get() = snapshot.profile ?: TrailMateSampleData.baselineProfile

    fun withAuthSession(session: TrailMateAuthSession): TrailMateAppSession =
        copy(snapshot = snapshot.copy(authSession = session))

    fun withoutAuthSession(): TrailMateAppSession =
        copy(snapshot = snapshot.copy(authSession = null))

    fun withProfile(profile: BaselineProfile): TrailMateAppSession =
        copy(snapshot = snapshot.copy(profile = profile))

    fun withImportedRoute(route: ImportedRoute): TrailMateAppSession =
        copy(snapshot = snapshot.copy(importedRoute = route))

    fun withHistoricalActivities(historicalActivities: List<HistoricalActivity>): TrailMateAppSession =
        copy(snapshot = snapshot.copy(historicalActivities = historicalActivities))

    fun withGpxImportQueue(queue: GpxImportQueue): TrailMateAppSession =
        copy(snapshot = snapshot.copy(gpxImportQueue = queue))

    fun withTrackRecording(trackRecording: TrackRecordingState): TrailMateAppSession =
        copy(snapshot = snapshot.copy(latestTrackRecording = trackRecording))

    fun withAmapPrivacyConsent(consent: AmapPrivacyConsent): TrailMateAppSession =
        copy(snapshot = snapshot.copy(amapPrivacyConsent = consent))

    fun withOfflineRoutePackKeys(keys: Set<String>): TrailMateAppSession =
        copy(snapshot = snapshot.copy(savedOfflineRoutePackKeys = keys))

    fun withOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>): TrailMateAppSession =
        copy(snapshot = snapshot.copy(offlineBaseMapTileProofs = proofs))

    fun withAiGearAdvisorResponse(response: AiGearAdvisorResponse?): TrailMateAppSession =
        copy(snapshot = snapshot.copy(aiGearAdvisorResponse = response))

    fun recoverInterruptedGpxImports(
        nowEpochMillis: Long,
        runningTimeoutMillis: Long,
        retryDelayMillis: Long
    ): TrailMateAppSession =
        withGpxImportQueue(
            snapshot.gpxImportQueue.recoverInterruptedRunningJobs(
                nowEpochMillis = nowEpochMillis,
                runningTimeoutMillis = runningTimeoutMillis,
                retryDelayMillis = retryDelayMillis
            )
        )

    fun clear(): TrailMateAppSession =
        TrailMateAppSession(TrailMateSnapshot.empty())
}
