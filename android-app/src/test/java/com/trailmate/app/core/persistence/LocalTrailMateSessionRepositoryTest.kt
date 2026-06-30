package com.trailmate.app.core.persistence

import com.trailmate.app.core.auth.TrailMateAuthProvider
import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.offlineRoutePackKey
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalTrailMateSessionRepositoryTest {
    @Test
    fun loadsSnapshotFromLocalStore() {
        val snapshot = TrailMateSnapshot(
            profile = TrailMateSampleData.baselineProfile,
            historicalActivities = TrailMateSampleData.historicalActivities
        )
        val repository = LocalTrailMateSessionRepository(FakeTrailMateSessionStore(snapshot))

        assertEquals(snapshot, repository.loadSnapshot())
    }

    @Test
    fun savesProfileAndHistoryThroughLocalStore() {
        val store = FakeTrailMateSessionStore()
        val repository = LocalTrailMateSessionRepository(store)
        val authSession = TrailMateAuthSession(
            userId = "usr-1",
            provider = TrailMateAuthProvider.PHONE,
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresAt = "2026-06-22T12:00:00Z",
            phoneNumber = "+8613800138000",
            wechatOpenId = null,
            displayName = null
        )

        repository.saveAuthSession(authSession)
        repository.saveProfile(TrailMateSampleData.baselineProfile)
        repository.saveImportedRoute(TrailMateSampleData.importedTargetRoute)
        repository.saveHistoricalActivities(TrailMateSampleData.historicalActivities)
        val queue = GpxImportQueue().enqueue(
            id = "job-1",
            kind = GpxImportJobKind.TARGET_ROUTE,
            sourceUri = "content://trailmate/routes/ridge",
            fileName = "ridge.gpx",
            nowEpochMillis = 1_000L
        )
        repository.saveGpxImportQueue(queue)
        val trackRecording = TrackRecordingEngine.start(
            routeName = TrailMateSampleData.importedTargetRoute.routeName,
            nowEpochMillis = 2_000L
        )
        repository.saveTrackRecording(trackRecording)
        val amapPrivacyConsent = AmapPrivacyConsent.accepted(nowEpochMillis = 3_000L)
        repository.saveAmapPrivacyConsent(amapPrivacyConsent)
        val offlineRoutePackKeys = setOf(TrailMateSampleData.importedTargetRoute.offlineRoutePackKey())
        repository.saveOfflineRoutePackKeys(offlineRoutePackKeys)
        val offlineBaseMapTileProofs = listOf(
            AmapOfflineBaseMapTileProof(
                routeKey = TrailMateSampleData.importedTargetRoute.offlineRoutePackKey(),
                targetAdcode = "330100",
                targetCityName = "杭州市",
                verifiedAtEpochMillis = 4_000L,
                networkDisabled = true,
                tileVisible = true
            )
        )
        repository.saveOfflineBaseMapTileProofs(offlineBaseMapTileProofs)
        val aiGearAdvisorResponse = AiGearAdvisorResponse(
            assessmentFingerprint = "fingerprint-1",
            recommendations = listOf(
                GearRecommendation(
                    category = "头灯",
                    status = GearStatus.CHECK,
                    rationale = "确认电量。",
                    matchedGearItemId = "cat_headlamp_bd_spot_400"
                )
            )
        )
        repository.saveAiGearAdvisorResponse(aiGearAdvisorResponse)

        assertEquals(authSession, store.snapshot.authSession)
        assertEquals(TrailMateSampleData.baselineProfile, store.snapshot.profile)
        assertEquals(TrailMateSampleData.importedTargetRoute, store.snapshot.importedRoute)
        assertEquals(TrailMateSampleData.historicalActivities, store.snapshot.historicalActivities)
        assertEquals(queue, store.snapshot.gpxImportQueue)
        assertEquals(trackRecording, store.snapshot.latestTrackRecording)
        assertEquals(amapPrivacyConsent, store.snapshot.amapPrivacyConsent)
        assertEquals(offlineRoutePackKeys, store.snapshot.savedOfflineRoutePackKeys)
        assertEquals(offlineBaseMapTileProofs, store.snapshot.offlineBaseMapTileProofs)
        assertEquals(aiGearAdvisorResponse, store.snapshot.aiGearAdvisorResponse)
    }

    @Test
    fun clearLocalDataWritesExplicitEmptySnapshot() {
        val store = FakeTrailMateSessionStore(
            TrailMateSnapshot(
                profile = TrailMateSampleData.baselineProfile,
                importedRoute = TrailMateSampleData.importedTargetRoute,
                historicalActivities = TrailMateSampleData.historicalActivities
            )
        )
        val repository = LocalTrailMateSessionRepository(store)

        repository.clearLocalData()

        assertEquals(TrailMateSnapshot.empty(), store.snapshot)
    }

    @Test
    fun clearAuthSessionPreservesLocalOutdoorData() {
        val authSession = TrailMateAuthSession(
            userId = "usr-1",
            provider = TrailMateAuthProvider.WECHAT,
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresAt = "2026-06-22T12:00:00Z",
            phoneNumber = null,
            wechatOpenId = "openid",
            displayName = "张三"
        )
        val startingSnapshot = TrailMateSnapshot(
            authSession = authSession,
            profile = TrailMateSampleData.baselineProfile,
            importedRoute = TrailMateSampleData.importedTargetRoute,
            historicalActivities = TrailMateSampleData.historicalActivities
        )
        val store = FakeTrailMateSessionStore(startingSnapshot)
        val repository = LocalTrailMateSessionRepository(store)

        repository.clearAuthSession()

        assertEquals(
            startingSnapshot.copy(authSession = null),
            store.snapshot
        )
    }
}

private class FakeTrailMateSessionStore(
    initialSnapshot: TrailMateSnapshot = TrailMateSnapshot()
) : TrailMateSessionStore {
    var snapshot: TrailMateSnapshot = initialSnapshot
        private set

    override fun load(): TrailMateSnapshot = snapshot

    override fun saveAuthSession(session: TrailMateAuthSession) {
        snapshot = snapshot.copy(authSession = session)
    }

    override fun clearAuthSession() {
        snapshot = snapshot.copy(authSession = null)
    }

    override fun saveProfile(profile: BaselineProfile) {
        snapshot = snapshot.copy(profile = profile)
    }

    override fun saveImportedRoute(route: ImportedRoute) {
        snapshot = snapshot.copy(importedRoute = route)
    }

    override fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>) {
        snapshot = snapshot.copy(historicalActivities = historicalActivities)
    }

    override fun saveGpxImportQueue(queue: GpxImportQueue) {
        snapshot = snapshot.copy(gpxImportQueue = queue)
    }

    override fun saveTrackRecording(trackRecording: TrackRecordingState) {
        snapshot = snapshot.copy(latestTrackRecording = trackRecording)
    }

    override fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent) {
        snapshot = snapshot.copy(amapPrivacyConsent = consent)
    }

    override fun saveOfflineRoutePackKeys(keys: Set<String>) {
        snapshot = snapshot.copy(savedOfflineRoutePackKeys = keys)
    }

    override fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>) {
        snapshot = snapshot.copy(offlineBaseMapTileProofs = proofs)
    }

    override fun saveAiGearAdvisorResponse(response: AiGearAdvisorResponse?) {
        snapshot = snapshot.copy(aiGearAdvisorResponse = response)
    }

    override fun clear() {
        snapshot = TrailMateSnapshot.empty()
    }
}
