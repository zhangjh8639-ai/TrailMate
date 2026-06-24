package com.trailmate.app.core.persistence

import com.trailmate.app.core.auth.TrailMateAuthProvider
import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.gpx.GpxImportJob
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus
import com.trailmate.app.core.model.TypicalDuration
import java.io.StringReader
import java.io.StringWriter
import java.util.Properties

object TrailMateSnapshotCodec {
    fun encode(snapshot: TrailMateSnapshot): String {
        val properties = Properties()

        properties["version"] = "1"
        snapshot.authSession?.let { session ->
            properties["auth.present"] = "true"
            properties["auth.userId"] = session.userId
            properties["auth.provider"] = session.provider.name
            properties["auth.accessToken"] = session.accessToken
            properties["auth.refreshToken"] = session.refreshToken
            properties["auth.expiresAt"] = session.expiresAt
            properties["auth.phoneNumber"] = session.phoneNumber.orEmpty()
            properties["auth.wechatOpenId"] = session.wechatOpenId.orEmpty()
            properties["auth.displayName"] = session.displayName.orEmpty()
        }

        snapshot.profile?.let { profile ->
            properties["profile.present"] = "true"
            properties["profile.exerciseFrequency"] = profile.exerciseFrequency.name
            properties["profile.typicalDuration"] = profile.typicalDuration.name
            properties["profile.experienceLevel"] = profile.experienceLevel.name
            properties["profile.ascentExperience"] = profile.ascentExperience.name
            properties["profile.heightCm"] = profile.heightCm?.toString().orEmpty()
            properties["profile.weightKg"] = profile.weightKg?.toString().orEmpty()
            properties["profile.commonPackWeightKg"] = profile.commonPackWeightKg?.toString().orEmpty()
        }

        snapshot.importedRoute?.let { route ->
            properties["route.present"] = "true"
            properties["route.routeName"] = route.routeName
            properties["route.fileName"] = route.fileName
            properties["route.distanceKm"] = route.distanceKm.toString()
            properties["route.ascentMeters"] = route.ascentMeters.toString()
            properties["route.status"] = route.status.name
            properties["route.pointCount"] = route.pointCount.toString()
            properties["route.durationMinutes"] = route.durationMinutes?.toString().orEmpty()
            properties["route.routePoint.count"] = route.routePoints.size.toString()
            route.routePoints.forEachIndexed { index, point ->
                val prefix = "route.routePoint.$index"
                properties["$prefix.latitude"] = point.latitude.toString()
                properties["$prefix.longitude"] = point.longitude.toString()
                properties["$prefix.elevationMeters"] = point.elevationMeters?.toString().orEmpty()
                properties["$prefix.distanceAlongRouteKm"] = point.distanceAlongRouteKm.toString()
            }
        }

        properties["history.count"] = snapshot.historicalActivities.size.toString()
        snapshot.historicalActivities.forEachIndexed { index, activity ->
            val prefix = "history.$index"
            properties["$prefix.routeName"] = activity.routeName
            properties["$prefix.distanceKm"] = activity.distanceKm.toString()
            properties["$prefix.ascentMeters"] = activity.ascentMeters.toString()
            properties["$prefix.durationMinutes"] = activity.durationMinutes.toString()
        }

        properties["gpxQueue.count"] = snapshot.gpxImportQueue.jobs.size.toString()
        snapshot.gpxImportQueue.jobs.forEachIndexed { index, job ->
            val prefix = "gpxQueue.$index"
            properties["$prefix.id"] = job.id
            properties["$prefix.kind"] = job.kind.name
            properties["$prefix.sourceUri"] = job.sourceUri
            properties["$prefix.fileName"] = job.fileName
            properties["$prefix.status"] = job.status.name
            properties["$prefix.attemptCount"] = job.attemptCount.toString()
            properties["$prefix.maxAttempts"] = job.maxAttempts.toString()
            properties["$prefix.nextAttemptAtEpochMillis"] = job.nextAttemptAtEpochMillis?.toString().orEmpty()
            properties["$prefix.lastError"] = job.lastError.orEmpty()
            properties["$prefix.createdAtEpochMillis"] = job.createdAtEpochMillis.toString()
            properties["$prefix.updatedAtEpochMillis"] = job.updatedAtEpochMillis.toString()
        }

        snapshot.latestTrackRecording.takeIf { it.status != TrackRecordingStatus.IDLE || it.points.isNotEmpty() }?.let { track ->
            properties["track.present"] = "true"
            properties["track.status"] = track.status.name
            properties["track.routeName"] = track.routeName.orEmpty()
            properties["track.startedAtEpochMillis"] = track.startedAtEpochMillis?.toString().orEmpty()
            properties["track.pausedAtEpochMillis"] = track.pausedAtEpochMillis?.toString().orEmpty()
            properties["track.recordingActiveSinceEpochMillis"] =
                track.recordingActiveSinceEpochMillis?.toString().orEmpty()
            properties["track.finishedAtEpochMillis"] = track.finishedAtEpochMillis?.toString().orEmpty()
            properties["track.totalDistanceKm"] = track.totalDistanceKm.toString()
            properties["track.point.count"] = track.points.size.toString()
            track.points.forEachIndexed { index, point ->
                val prefix = "track.point.$index"
                properties["$prefix.latitude"] = point.latitude.toString()
                properties["$prefix.longitude"] = point.longitude.toString()
                properties["$prefix.elevationMeters"] = point.elevationMeters?.toString().orEmpty()
                properties["$prefix.horizontalAccuracyMeters"] = point.horizontalAccuracyMeters.toString()
                properties["$prefix.timestampEpochMillis"] = point.timestampEpochMillis.toString()
            }
        }

        properties["offlineRoutePack.count"] = snapshot.savedOfflineRoutePackKeys.size.toString()
        snapshot.savedOfflineRoutePackKeys.sorted().forEachIndexed { index, key ->
            properties["offlineRoutePack.$index.key"] = key
        }

        properties["offlineBaseMapTileProof.count"] = snapshot.offlineBaseMapTileProofs.size.toString()
        snapshot.offlineBaseMapTileProofs.forEachIndexed { index, proof ->
            val prefix = "offlineBaseMapTileProof.$index"
            properties["$prefix.routeKey"] = proof.routeKey
            properties["$prefix.targetAdcode"] = proof.targetAdcode.orEmpty()
            properties["$prefix.targetCityName"] = proof.targetCityName.orEmpty()
            properties["$prefix.verifiedAtEpochMillis"] = proof.verifiedAtEpochMillis.toString()
            properties["$prefix.networkDisabled"] = proof.networkDisabled.toString()
            properties["$prefix.tileVisible"] = proof.tileVisible.toString()
        }

        properties["amapPrivacy.accepted"] = snapshot.amapPrivacyConsent.accepted.toString()
        properties["amapPrivacy.acceptedAtEpochMillis"] =
            snapshot.amapPrivacyConsent.acceptedAtEpochMillis?.toString().orEmpty()
        properties["amapPrivacy.policyVersion"] = snapshot.amapPrivacyConsent.policyVersion

        return StringWriter().use { writer ->
            properties.store(writer, "TrailMate local snapshot")
            writer.toString()
        }
    }

    fun decode(raw: String): TrailMateSnapshot {
        if (raw.isBlank()) {
            return TrailMateSnapshot()
        }

        return runCatching {
            val properties = Properties().apply {
                load(StringReader(raw))
            }

            TrailMateSnapshot(
                authSession = properties.decodeAuthSession(),
                profile = properties.decodeProfile(),
                importedRoute = properties.decodeImportedRoute(),
                historicalActivities = properties.decodeHistoricalActivities(),
                gpxImportQueue = properties.decodeGpxImportQueue(),
                latestTrackRecording = properties.decodeTrackRecording(),
                savedOfflineRoutePackKeys = properties.decodeOfflineRoutePackKeys(),
                offlineBaseMapTileProofs = properties.decodeOfflineBaseMapTileProofs(),
                amapPrivacyConsent = properties.decodeAmapPrivacyConsent()
            )
        }.getOrDefault(TrailMateSnapshot())
    }

    private fun Properties.decodeAuthSession(): TrailMateAuthSession? {
        if (getProperty("auth.present") != "true") {
            return null
        }

        val userId = getProperty("auth.userId")?.takeIf { it.isNotBlank() } ?: return null
        val provider = enumValue<TrailMateAuthProvider>("auth.provider") ?: return null
        val accessToken = getProperty("auth.accessToken")?.takeIf { it.isNotBlank() } ?: return null
        val refreshToken = getProperty("auth.refreshToken")?.takeIf { it.isNotBlank() } ?: return null
        val expiresAt = getProperty("auth.expiresAt")?.takeIf { it.isNotBlank() } ?: return null

        return TrailMateAuthSession(
            userId = userId,
            provider = provider,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            phoneNumber = getProperty("auth.phoneNumber").orEmpty().ifBlank { null },
            wechatOpenId = getProperty("auth.wechatOpenId").orEmpty().ifBlank { null },
            displayName = getProperty("auth.displayName").orEmpty().ifBlank { null }
        )
    }

    private fun Properties.decodeProfile(): BaselineProfile? {
        if (getProperty("profile.present") != "true") {
            return null
        }

        val exerciseFrequency = enumValue<ExerciseFrequency>("profile.exerciseFrequency") ?: return null
        val typicalDuration = enumValue<TypicalDuration>("profile.typicalDuration") ?: return null
        val experienceLevel = enumValue<ExperienceLevel>("profile.experienceLevel") ?: return null
        val ascentExperience = enumValue<AscentExperience>("profile.ascentExperience") ?: return null

        return BaselineProfile(
            exerciseFrequency = exerciseFrequency,
            typicalDuration = typicalDuration,
            experienceLevel = experienceLevel,
            ascentExperience = ascentExperience,
            heightCm = nullableInt("profile.heightCm"),
            weightKg = nullableInt("profile.weightKg"),
            commonPackWeightKg = nullableInt("profile.commonPackWeightKg")
        )
    }

    private fun Properties.decodeImportedRoute(): ImportedRoute? {
        if (getProperty("route.present") != "true") {
            return null
        }

        val routeName = getProperty("route.routeName")?.takeIf { it.isNotBlank() } ?: return null
        val fileName = getProperty("route.fileName")?.takeIf { it.isNotBlank() } ?: return null
        val distanceKm = getProperty("route.distanceKm")?.toDoubleOrNull() ?: return null
        val ascentMeters = getProperty("route.ascentMeters")?.toIntOrNull() ?: return null
        val status = enumValue<RouteImportStatus>("route.status") ?: return null
        val pointCount = getProperty("route.pointCount")?.toIntOrNull() ?: 0
        val durationMinutes = nullableInt("route.durationMinutes")
        val routePoints = decodeRoutePoints()

        return ImportedRoute(
            routeName = routeName,
            fileName = fileName,
            distanceKm = distanceKm,
            ascentMeters = ascentMeters,
            status = status,
            pointCount = pointCount,
            durationMinutes = durationMinutes,
            routePoints = routePoints
        )
    }

    private fun Properties.decodeRoutePoints(): List<RoutePoint> {
        val count = getProperty("route.routePoint.count")?.toIntOrNull() ?: return emptyList()
        return (0 until count).mapNotNull { index ->
            val prefix = "route.routePoint.$index"
            val latitude = getProperty("$prefix.latitude")?.toDoubleOrNull() ?: return@mapNotNull null
            val longitude = getProperty("$prefix.longitude")?.toDoubleOrNull() ?: return@mapNotNull null
            val distanceAlongRouteKm = getProperty("$prefix.distanceAlongRouteKm")?.toDoubleOrNull()
                ?: return@mapNotNull null

            RoutePoint(
                latitude = latitude,
                longitude = longitude,
                elevationMeters = nullableDouble("$prefix.elevationMeters"),
                distanceAlongRouteKm = distanceAlongRouteKm
            )
        }
    }

    private fun Properties.decodeHistoricalActivities(): List<HistoricalActivity> {
        val count = getProperty("history.count")?.toIntOrNull() ?: return emptyList()

        return (0 until count).mapNotNull { index ->
            val prefix = "history.$index"
            val routeName = getProperty("$prefix.routeName")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val distanceKm = getProperty("$prefix.distanceKm")?.toDoubleOrNull() ?: return@mapNotNull null
            val ascentMeters = getProperty("$prefix.ascentMeters")?.toIntOrNull() ?: return@mapNotNull null
            val durationMinutes = getProperty("$prefix.durationMinutes")?.toIntOrNull() ?: return@mapNotNull null

            HistoricalActivity(
                routeName = routeName,
                distanceKm = distanceKm,
                ascentMeters = ascentMeters,
                durationMinutes = durationMinutes
            )
        }
    }

    private fun Properties.decodeGpxImportQueue(): GpxImportQueue {
        val count = getProperty("gpxQueue.count")?.toIntOrNull() ?: return GpxImportQueue()
        val jobs = (0 until count).mapNotNull { index ->
            val prefix = "gpxQueue.$index"
            val id = getProperty("$prefix.id")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val kind = enumValue<GpxImportJobKind>("$prefix.kind") ?: return@mapNotNull null
            val sourceUri = getProperty("$prefix.sourceUri")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val fileName = getProperty("$prefix.fileName")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val status = enumValue<GpxImportJobStatus>("$prefix.status") ?: return@mapNotNull null
            val attemptCount = getProperty("$prefix.attemptCount")?.toIntOrNull() ?: return@mapNotNull null
            val maxAttempts = getProperty("$prefix.maxAttempts")?.toIntOrNull() ?: return@mapNotNull null
            val createdAt = getProperty("$prefix.createdAtEpochMillis")?.toLongOrNull() ?: return@mapNotNull null
            val updatedAt = getProperty("$prefix.updatedAtEpochMillis")?.toLongOrNull() ?: return@mapNotNull null

            GpxImportJob(
                id = id,
                kind = kind,
                sourceUri = sourceUri,
                fileName = fileName,
                status = status,
                attemptCount = attemptCount,
                maxAttempts = maxAttempts,
                nextAttemptAtEpochMillis = nullableLong("$prefix.nextAttemptAtEpochMillis"),
                lastError = getProperty("$prefix.lastError").orEmpty().ifBlank { null },
                createdAtEpochMillis = createdAt,
                updatedAtEpochMillis = updatedAt
            )
        }

        return GpxImportQueue(jobs = jobs.filter(::isValidGpxImportJob).sanitizeGpxImportJobs())
    }

    private fun Properties.decodeTrackRecording(): TrackRecordingState {
        if (getProperty("track.present") != "true") {
            return TrackRecordingState()
        }
        val status = enumValue<TrackRecordingStatus>("track.status") ?: return TrackRecordingState()
        val count = getProperty("track.point.count")?.toIntOrNull() ?: 0
        val points = (0 until count).mapNotNull { index ->
            val prefix = "track.point.$index"
            val latitude = getProperty("$prefix.latitude")?.toDoubleOrNull() ?: return@mapNotNull null
            val longitude = getProperty("$prefix.longitude")?.toDoubleOrNull() ?: return@mapNotNull null
            val horizontalAccuracyMeters = getProperty("$prefix.horizontalAccuracyMeters")?.toDoubleOrNull()
                ?: return@mapNotNull null
            val timestampEpochMillis = getProperty("$prefix.timestampEpochMillis")?.toLongOrNull()
                ?: return@mapNotNull null

            RecordedTrackPoint(
                latitude = latitude,
                longitude = longitude,
                elevationMeters = nullableDouble("$prefix.elevationMeters"),
                horizontalAccuracyMeters = horizontalAccuracyMeters,
                timestampEpochMillis = timestampEpochMillis
            )
        }

        return TrackRecordingState(
            status = status,
            routeName = getProperty("track.routeName").orEmpty().ifBlank { null },
            startedAtEpochMillis = nullableLong("track.startedAtEpochMillis"),
            pausedAtEpochMillis = nullableLong("track.pausedAtEpochMillis"),
            recordingActiveSinceEpochMillis = nullableLong("track.recordingActiveSinceEpochMillis"),
            finishedAtEpochMillis = nullableLong("track.finishedAtEpochMillis"),
            points = points,
            totalDistanceKm = getProperty("track.totalDistanceKm")?.toDoubleOrNull() ?: 0.0
        )
    }

    private fun Properties.decodeOfflineRoutePackKeys(): Set<String> {
        val count = getProperty("offlineRoutePack.count")?.toIntOrNull() ?: return emptySet()

        return (0 until count)
            .mapNotNull { index ->
                getProperty("offlineRoutePack.$index.key")?.takeIf { it.isNotBlank() }
            }
            .toSet()
    }

    private fun Properties.decodeOfflineBaseMapTileProofs(): List<AmapOfflineBaseMapTileProof> {
        val count = getProperty("offlineBaseMapTileProof.count")?.toIntOrNull() ?: return emptyList()

        return (0 until count).mapNotNull { index ->
            val prefix = "offlineBaseMapTileProof.$index"
            val routeKey = getProperty("$prefix.routeKey")?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val verifiedAtEpochMillis = nullableLong("$prefix.verifiedAtEpochMillis")
                ?: return@mapNotNull null

            AmapOfflineBaseMapTileProof(
                routeKey = routeKey,
                targetAdcode = getProperty("$prefix.targetAdcode").orEmpty().ifBlank { null },
                targetCityName = getProperty("$prefix.targetCityName").orEmpty().ifBlank { null },
                verifiedAtEpochMillis = verifiedAtEpochMillis,
                networkDisabled = getProperty("$prefix.networkDisabled")?.toBooleanStrictOrNull() ?: false,
                tileVisible = getProperty("$prefix.tileVisible")?.toBooleanStrictOrNull() ?: false
            )
        }
    }

    private fun Properties.decodeAmapPrivacyConsent(): AmapPrivacyConsent =
        AmapPrivacyConsent(
            accepted = getProperty("amapPrivacy.accepted")?.toBooleanStrictOrNull() ?: false,
            acceptedAtEpochMillis = nullableLong("amapPrivacy.acceptedAtEpochMillis"),
            policyVersion = getProperty("amapPrivacy.policyVersion")
                ?.takeIf { it.isNotBlank() }
                ?: AmapPrivacyConsent.CURRENT_POLICY_VERSION
        )

    private fun List<GpxImportJob>.sanitizeGpxImportJobs(): List<GpxImportJob> {
        val seenIds = mutableSetOf<String>()
        var hasRunningJob = false
        return filter { job ->
            if (!seenIds.add(job.id)) {
                return@filter false
            }
            if (job.status == GpxImportJobStatus.RUNNING) {
                if (hasRunningJob) {
                    return@filter false
                }
                hasRunningJob = true
            }
            true
        }
    }

    private fun isValidGpxImportJob(job: GpxImportJob): Boolean {
        val baseValid = job.id.isNotBlank() &&
            job.sourceUri.isNotBlank() &&
            job.fileName.isNotBlank() &&
            job.attemptCount >= 0 &&
            job.maxAttempts > 0
        if (!baseValid) {
            return false
        }

        return when (job.status) {
            GpxImportJobStatus.QUEUED ->
                job.attemptCount < job.maxAttempts &&
                    job.nextAttemptAtEpochMillis == null
            GpxImportJobStatus.RUNNING ->
                job.attemptCount in 1..job.maxAttempts &&
                    job.nextAttemptAtEpochMillis == null
            GpxImportJobStatus.WAITING_RETRY ->
                job.attemptCount in 1 until job.maxAttempts &&
                    job.nextAttemptAtEpochMillis != null
            GpxImportJobStatus.SUCCEEDED ->
                job.attemptCount in 1..job.maxAttempts &&
                    job.nextAttemptAtEpochMillis == null
            GpxImportJobStatus.FAILED ->
                job.attemptCount >= job.maxAttempts &&
                    job.nextAttemptAtEpochMillis == null
        }
    }

    private inline fun <reified T : Enum<T>> Properties.enumValue(key: String): T? =
        getProperty(key)?.let { value ->
            runCatching { enumValueOf<T>(value) }.getOrNull()
        }

    private fun Properties.nullableInt(key: String): Int? =
        getProperty(key).orEmpty().takeIf { it.isNotBlank() }?.toIntOrNull()

    private fun Properties.nullableLong(key: String): Long? =
        getProperty(key).orEmpty().takeIf { it.isNotBlank() }?.toLongOrNull()

    private fun Properties.nullableDouble(key: String): Double? =
        getProperty(key).orEmpty().takeIf { it.isNotBlank() }?.toDoubleOrNull()
}
