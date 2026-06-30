package com.trailmate.app.core.persistence

import com.trailmate.app.core.gpx.GpxImportJob
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.auth.TrailMateAuthProvider
import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TypicalDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class TrailMateSnapshotCodecTest {
    @Test
    fun snapshotRoundTripsProfileAndImportedRoute() {
        val snapshot = TrailMateSnapshot(
            authSession = TrailMateAuthSession(
                userId = "usr-1",
                provider = TrailMateAuthProvider.PHONE,
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresAt = "2026-06-22T12:00:00Z",
                phoneNumber = "+8613800138000",
                wechatOpenId = null,
                displayName = null
            ),
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
                typicalDuration = TypicalDuration.OVER_60,
                experienceLevel = ExperienceLevel.EXPERIENCED,
                ascentExperience = AscentExperience.OVER_800,
                heightCm = 181,
                weightKg = 76,
                commonPackWeightKg = 7
            ),
            importedRoute = ImportedRoute(
                routeName = "West Ridge",
                fileName = "west-ridge.gpx",
                distanceKm = 8.4,
                ascentMeters = 540,
                status = RouteImportStatus.PARSED,
                pointCount = 64,
                durationMinutes = 128,
                routePoints = listOf(
                    RoutePoint(
                        latitude = 30.0,
                        longitude = 120.0,
                        elevationMeters = 100.0,
                        distanceAlongRouteKm = 0.0
                    ),
                    RoutePoint(
                        latitude = 30.01,
                        longitude = 120.0,
                        elevationMeters = 120.0,
                        distanceAlongRouteKm = 1.1
                    )
                )
            ),
            historicalActivities = listOf(
                HistoricalActivity(
                    routeName = "Old Ridge",
                    distanceKm = 11.2,
                    ascentMeters = 620,
                    durationMinutes = 240
                )
            ),
            latestTrackRecording = TrackRecordingEngine.appendLocation(
                state = TrackRecordingEngine.start(
                    routeName = "West Ridge",
                    routeKey = "west-ridge.gpx|West Ridge|8.4|540|64",
                    nowEpochMillis = 1_000L
                ),
                point = RecordedTrackPoint(
                    latitude = 30.0,
                    longitude = 120.0,
                    elevationMeters = 100.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 1_100L
                ),
                nowEpochMillis = 1_100L
            ),
            savedOfflineRoutePackKeys = setOf("west-ridge.gpx|West Ridge|8.4|540|64"),
            offlineBaseMapTileProofs = listOf(
                AmapOfflineBaseMapTileProof(
                    routeKey = "west-ridge.gpx|West Ridge|8.4|540|64",
                    targetAdcode = "330100",
                    targetCityName = "杭州市",
                    verifiedAtEpochMillis = 1_700_000_000_000L,
                    networkDisabled = true,
                    tileVisible = true
                )
            ),
            amapPrivacyConsent = AmapPrivacyConsent.accepted(nowEpochMillis = 2_000L)
        )

        val decoded = TrailMateSnapshotCodec.decode(TrailMateSnapshotCodec.encode(snapshot))

        assertEquals(snapshot, decoded)
    }

    @Test
    fun snapshotRoundTripPreservesUnsetBodyFields() {
        val snapshot = TrailMateSnapshot(
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.RARELY,
                typicalDuration = TypicalDuration.UNDER_30,
                experienceLevel = ExperienceLevel.BEGINNER,
                ascentExperience = AscentExperience.UNDER_300,
                heightCm = null,
                weightKg = null,
                commonPackWeightKg = null
            )
        )

        val decoded = TrailMateSnapshotCodec.decode(TrailMateSnapshotCodec.encode(snapshot))

        assertNull(decoded.profile?.heightCm)
        assertNull(decoded.profile?.weightKg)
        assertNull(decoded.profile?.commonPackWeightKg)
    }

    @Test
    fun blankSnapshotDecodesToPrototypeDefaults() {
        val decoded = TrailMateSnapshotCodec.decode("")

        assertNull(decoded.profile)
        assertNull(decoded.importedRoute)
    }

    @Test
    fun explicitEmptySnapshotRoundTripsWithoutPrototypeDefaults() {
        val decoded = TrailMateSnapshotCodec.decode(
            TrailMateSnapshotCodec.encode(TrailMateSnapshot.empty())
        )

        assertNull(decoded.authSession)
        assertNull(decoded.profile)
        assertNull(decoded.importedRoute)
        assertEquals(0, decoded.historicalActivities.size)
    }

    @Test
    fun snapshotEncodeDoesNotPersistPersonalGearInventory() {
        val encoded = TrailMateSnapshotCodec.encode(TrailMateSnapshot())

        assertFalse(encoded.contains("inventory.count"))
        assertFalse(encoded.contains("inventory.0."))
    }

    @Test
    fun snapshotDecodeIgnoresLegacyPersonalGearInventoryFields() {
        val raw = """
            version=1
            inventory.count=1
            inventory.0.id=shell-1
            inventory.0.category=雨衣
            inventory.0.brand=Patagonia
            inventory.0.model=Torrentshell
            inventory.0.weightGrams=400
            inventory.0.available=true
            route.present=true
            route.routeName=龙井山脊
            route.fileName=longjing.gpx
            route.distanceKm=15.2
            route.ascentMeters=860
            route.status=PARSED
            route.pointCount=120
            history.count=0
            gpxQueue.count=0
        """.trimIndent()

        val decoded = TrailMateSnapshotCodec.decode(raw)

        assertEquals("龙井山脊", decoded.importedRoute?.routeName)
    }

    @Test
    fun snapshotRoundTripPreservesWechatAuthSession() {
        val session = TrailMateAuthSession(
            userId = "usr-wechat",
            provider = TrailMateAuthProvider.WECHAT,
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresAt = "2026-06-22T12:00:00Z",
            phoneNumber = null,
            wechatOpenId = "wx-open-id",
            displayName = "张三"
        )
        val snapshot = TrailMateSnapshot(authSession = session)

        val decoded = TrailMateSnapshotCodec.decode(TrailMateSnapshotCodec.encode(snapshot))

        assertEquals(session, decoded.authSession)
    }

    @Test
    fun snapshotRoundTripPreservesLatestAiGearAdviceArtifact() {
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = "longjing#15.2#860#CAUTION#LOW",
            recommendations = listOf(
                GearRecommendation(
                    category = "头灯",
                    status = GearStatus.CHECK,
                    rationale = "预计耗时较长，确认电量并准备备用照明。",
                    matchedGearItemId = "cat_headlamp_bd_spot_400"
                ),
                GearRecommendation(
                    category = "登山杖",
                    status = GearStatus.MISSING,
                    rationale = "累计爬升较高，下坡段需要稳定支撑。",
                    matchedGearItemId = "cat_poles_leki_legacy_lite"
                )
            )
        )
        val snapshot = TrailMateSnapshot(aiGearAdvisorResponse = response)

        val decoded = TrailMateSnapshotCodec.decode(TrailMateSnapshotCodec.encode(snapshot))

        assertEquals(response, decoded.aiGearAdvisorResponse)
    }

    @Test
    fun snapshotRoundTripPreservesGpxImportQueue() {
        val queue = GpxImportQueue(
            jobs = listOf(
                GpxImportJob(
                    id = "job-1",
                    kind = GpxImportJobKind.TARGET_ROUTE,
                    sourceUri = "content://trailmate/routes/ridge",
                    fileName = "ridge.gpx",
                    status = GpxImportJobStatus.WAITING_RETRY,
                    attemptCount = 1,
                    maxAttempts = 3,
                    nextAttemptAtEpochMillis = 61_200L,
                    lastError = "Parser unavailable.",
                    createdAtEpochMillis = 1_000L,
                    updatedAtEpochMillis = 1_200L
                )
            )
        )
        val snapshot = TrailMateSnapshot(gpxImportQueue = queue)

        val decoded = TrailMateSnapshotCodec.decode(TrailMateSnapshotCodec.encode(snapshot))

        assertEquals(queue, decoded.gpxImportQueue)
    }

    @Test
    fun snapshotDecodeDropsBlankOfflineRoutePackKeys() {
        val raw = """
            version=1
            inventory.count=0
            history.count=0
            gpxQueue.count=0
            offlineRoutePack.count=3
            offlineRoutePack.0.key=west-ridge.gpx|West Ridge|8.4|540|64
            offlineRoutePack.1.key=
            offlineRoutePack.2.key=longjing-ridge-target.gpx|龙井山脊|15.2|860|120
        """.trimIndent()

        val decoded = TrailMateSnapshotCodec.decode(raw)

        assertEquals(
            setOf(
                "west-ridge.gpx|West Ridge|8.4|540|64",
                "longjing-ridge-target.gpx|龙井山脊|15.2|860|120"
            ),
            decoded.savedOfflineRoutePackKeys
        )
    }

    @Test
    fun snapshotDecodeDropsInvalidGpxImportJobs() {
        val raw = """
            version=1
            inventory.count=0
            history.count=0
            gpxQueue.count=2
            gpxQueue.0.id=invalid-waiting
            gpxQueue.0.kind=TARGET_ROUTE
            gpxQueue.0.sourceUri=content://trailmate/routes/ridge
            gpxQueue.0.fileName=ridge.gpx
            gpxQueue.0.status=WAITING_RETRY
            gpxQueue.0.attemptCount=1
            gpxQueue.0.maxAttempts=3
            gpxQueue.0.createdAtEpochMillis=1000
            gpxQueue.0.updatedAtEpochMillis=1200
            gpxQueue.1.id=valid-queued
            gpxQueue.1.kind=HISTORICAL_ACTIVITY
            gpxQueue.1.sourceUri=content://trailmate/history/old-ridge
            gpxQueue.1.fileName=old-ridge.gpx
            gpxQueue.1.status=QUEUED
            gpxQueue.1.attemptCount=0
            gpxQueue.1.maxAttempts=3
            gpxQueue.1.createdAtEpochMillis=1000
            gpxQueue.1.updatedAtEpochMillis=1000
        """.trimIndent()

        val decoded = TrailMateSnapshotCodec.decode(raw)

        assertEquals("valid-queued", decoded.gpxImportQueue.jobs.single().id)
    }

    @Test
    fun snapshotDecodeEnforcesGlobalGpxImportQueueInvariants() {
        val raw = """
            version=1
            inventory.count=0
            history.count=0
            gpxQueue.count=4
            gpxQueue.0.id=duplicate
            gpxQueue.0.kind=TARGET_ROUTE
            gpxQueue.0.sourceUri=content://trailmate/routes/ridge
            gpxQueue.0.fileName=ridge.gpx
            gpxQueue.0.status=QUEUED
            gpxQueue.0.attemptCount=0
            gpxQueue.0.maxAttempts=3
            gpxQueue.0.createdAtEpochMillis=1000
            gpxQueue.0.updatedAtEpochMillis=1000
            gpxQueue.1.id=duplicate
            gpxQueue.1.kind=HISTORICAL_ACTIVITY
            gpxQueue.1.sourceUri=content://trailmate/history/old-ridge
            gpxQueue.1.fileName=old-ridge.gpx
            gpxQueue.1.status=QUEUED
            gpxQueue.1.attemptCount=0
            gpxQueue.1.maxAttempts=3
            gpxQueue.1.createdAtEpochMillis=1000
            gpxQueue.1.updatedAtEpochMillis=1000
            gpxQueue.2.id=running-1
            gpxQueue.2.kind=TARGET_ROUTE
            gpxQueue.2.sourceUri=content://trailmate/routes/running-1
            gpxQueue.2.fileName=running-1.gpx
            gpxQueue.2.status=RUNNING
            gpxQueue.2.attemptCount=1
            gpxQueue.2.maxAttempts=3
            gpxQueue.2.createdAtEpochMillis=1000
            gpxQueue.2.updatedAtEpochMillis=1000
            gpxQueue.3.id=running-2
            gpxQueue.3.kind=TARGET_ROUTE
            gpxQueue.3.sourceUri=content://trailmate/routes/running-2
            gpxQueue.3.fileName=running-2.gpx
            gpxQueue.3.status=RUNNING
            gpxQueue.3.attemptCount=1
            gpxQueue.3.maxAttempts=3
            gpxQueue.3.createdAtEpochMillis=1000
            gpxQueue.3.updatedAtEpochMillis=1000
        """.trimIndent()

        val decoded = TrailMateSnapshotCodec.decode(raw)

        assertEquals(
            listOf("duplicate", "running-1"),
            decoded.gpxImportQueue.jobs.map { job -> job.id }
        )
    }
}
