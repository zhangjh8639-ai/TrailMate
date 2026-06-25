package com.trailmate.app.core.model

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

enum class ExerciseFrequency { RARELY, ONE_TO_TWO_PER_WEEK, THREE_PLUS_PER_WEEK }
enum class TypicalDuration { UNDER_30, MIN_30_TO_60, OVER_60 }
enum class ExperienceLevel { BEGINNER, REGULAR, EXPERIENCED }
enum class AscentExperience { UNDER_300, M300_TO_800, OVER_800 }
enum class ConfidenceLevel { LOW, MEDIUM, HIGH }
enum class MatchLevel { RECOMMENDED, CAUTION, NOT_RECOMMENDED }
enum class GearStatus { COVERED, CHECK, MISSING, OPTIONAL }
enum class RouteImportStatus { EMPTY, PARSED }
enum class HikePlanCheckpointType { START, ENERGY_CHECK, REST_CHECK, RISK_CHECK, FINISH }
enum class HikeSessionStatus { READY, ACTIVE, PAUSED, COMPLETED }
enum class TrackRecordingStatus { IDLE, RECORDING, PAUSED, FINISHED }

data class BaselineProfile(
    val exerciseFrequency: ExerciseFrequency,
    val typicalDuration: TypicalDuration,
    val experienceLevel: ExperienceLevel,
    val ascentExperience: AscentExperience,
    val heightCm: Int?,
    val weightKg: Int?,
    val commonPackWeightKg: Int?
) {
    fun initialConfidence(): ConfidenceLevel = ConfidenceLevel.LOW

    fun explanation(): String =
        "在导入足够历史 GPX 之前，TrailMate 会先参考基础档案做保守估算。"

    fun bodyMetricsLabel(): String {
        val height = heightCm?.let { "${it}cm" }
        val weight = weightKg?.let { "${it}kg" }

        return listOfNotNull(height, weight).joinToString(" / ").ifBlank { "未填写" }
    }

    fun packWeightLabel(): String =
        commonPackWeightKg?.let { "背包 $it kg" } ?: "背包待填"
}

data class GearRecommendation(
    val category: String,
    val status: GearStatus,
    val rationale: String,
    val matchedGearItemId: String? = null
)

data class ImportedRoute(
    val routeName: String,
    val fileName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val status: RouteImportStatus,
    val pointCount: Int = 0,
    val durationMinutes: Int? = null,
    val routePoints: List<RoutePoint> = emptyList()
) {
    fun readyForAssessment(): Boolean = status == RouteImportStatus.PARSED

    fun summaryLabel(): String = String.format(Locale.US, "%.1f km / +%d m", distanceKm, ascentMeters)
}

fun ImportedRoute.offlineRoutePackKey(): String =
    "$fileName|$routeName|$distanceKm|$ascentMeters|$pointCount|${routePoints.geometryFingerprint()}"

private fun List<RoutePoint>.geometryFingerprint(): String {
    if (isEmpty()) {
        return "geometry-none"
    }

    val digest = MessageDigest.getInstance("SHA-256")
    forEach { point ->
        val encoded = String.format(
            Locale.US,
            "%.6f,%.6f,%.2f,%.3f;",
            point.latitude,
            point.longitude,
            point.elevationMeters ?: Double.NaN,
            point.distanceAlongRouteKm
        )
        digest.update(encoded.toByteArray(StandardCharsets.UTF_8))
    }
    return digest.digest()
        .take(8)
        .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val distanceAlongRouteKm: Double
)

data class HistoricalActivity(
    val routeName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val durationMinutes: Int
)

data class CapabilityProfileSummary(
    val title: String,
    val value: String,
    val caption: String,
    val confidenceLevel: ConfidenceLevel,
    val evidenceLabel: String
)

data class RouteAssessmentSummary(
    val routeName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val matchLevel: MatchLevel,
    val confidenceLevel: ConfidenceLevel,
    val estimatedDurationRange: String,
    val risks: List<String>
)

data class HikePlanCheckpoint(
    val type: HikePlanCheckpointType,
    val title: String,
    val distanceKm: Double,
    val timeFromStart: String,
    val note: String
)

data class HikePlanSummary(
    val checkpoints: List<HikePlanCheckpoint>
) {
    val checkpointCount: Int get() = checkpoints.size

    fun nextMovingCheckpoint(): HikePlanCheckpoint? =
        checkpoints.firstOrNull { checkpoint -> checkpoint.type != HikePlanCheckpointType.START }
}

data class HikeSessionState(
    val status: HikeSessionStatus,
    val reachedCheckpointIndex: Int
)

data class RecordedTrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val horizontalAccuracyMeters: Double,
    val timestampEpochMillis: Long
)

data class TrackRecordingState(
    val status: TrackRecordingStatus = TrackRecordingStatus.IDLE,
    val routeName: String? = null,
    val startedAtEpochMillis: Long? = null,
    val pausedAtEpochMillis: Long? = null,
    val recordingActiveSinceEpochMillis: Long? = null,
    val finishedAtEpochMillis: Long? = null,
    val points: List<RecordedTrackPoint> = emptyList(),
    val totalDistanceKm: Double = 0.0,
    val routeKey: String? = null
) {
    val pointCount: Int get() = points.size
}
