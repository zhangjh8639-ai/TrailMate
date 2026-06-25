package com.trailmate.app

import com.trailmate.app.core.auth.TrailMateAuthProvider
import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.persistence.TrailMateSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateAppSessionTest {
    @Test
    fun accountAuthenticationIsSeparateFromProfileCompletion() {
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
        val session = TrailMateAppSession(TrailMateSnapshot())

        val authenticated = session.withAuthSession(authSession)

        assertEquals(false, session.hasAuthenticatedAccount)
        assertEquals(true, authenticated.hasAuthenticatedAccount)
        assertEquals(false, authenticated.hasProfile)
        assertEquals(false, authenticated.isReadyForHome)
        assertEquals(true, authenticated.withProfile(com.trailmate.app.core.model.TrailMateSampleData.baselineProfile).isReadyForHome)
    }

    @Test
    fun recoverInterruptedGpxImportsConvertsStaleRunningJobsOnStartup() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
        val session = TrailMateAppSession(TrailMateSnapshot(gpxImportQueue = queue))

        val recovered = session.recoverInterruptedGpxImports(
            nowEpochMillis = 121_100L,
            runningTimeoutMillis = 120_000L,
            retryDelayMillis = 30_000L
        )

        val recoveredJob = recovered.snapshot.gpxImportQueue.jobs.single()
        assertEquals(GpxImportJobStatus.WAITING_RETRY, recoveredJob.status)
        assertEquals(151_100L, recoveredJob.nextAttemptAtEpochMillis)
    }

    @Test
    fun recoverInterruptedGpxImportsCanTreatAnyRestoredRunningJobAsInterrupted() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
        val session = TrailMateAppSession(TrailMateSnapshot(gpxImportQueue = queue))

        val recovered = session.recoverInterruptedGpxImports(
            nowEpochMillis = 1_101L,
            runningTimeoutMillis = 0L,
            retryDelayMillis = 30_000L
        )

        assertEquals(GpxImportJobStatus.WAITING_RETRY, recovered.snapshot.gpxImportQueue.jobs.single().status)
    }

    @Test
    fun withoutAuthSessionPreservesProfileAndOutdoorData() {
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
        val snapshot = TrailMateSnapshot(
            authSession = authSession,
            profile = com.trailmate.app.core.model.TrailMateSampleData.baselineProfile,
            importedRoute = com.trailmate.app.core.model.TrailMateSampleData.importedTargetRoute,
            historicalActivities = com.trailmate.app.core.model.TrailMateSampleData.historicalActivities
        )
        val session = TrailMateAppSession(snapshot)

        val signedOut = session.withoutAuthSession()

        assertEquals(null, signedOut.snapshot.authSession)
        assertEquals(snapshot.profile, signedOut.snapshot.profile)
        assertEquals(snapshot.importedRoute, signedOut.snapshot.importedRoute)
        assertEquals(snapshot.historicalActivities, signedOut.snapshot.historicalActivities)
        assertEquals(false, signedOut.isReadyForHome)
    }
}
