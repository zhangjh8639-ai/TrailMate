package com.trailmate.app.core.model

enum class ExerciseFrequency { RARELY, ONE_TO_TWO_PER_WEEK, THREE_PLUS_PER_WEEK }
enum class TypicalDuration { UNDER_30, MIN_30_TO_60, OVER_60 }
enum class ExperienceLevel { BEGINNER, REGULAR, EXPERIENCED }
enum class AscentExperience { UNDER_300, M300_TO_800, OVER_800 }
enum class ConfidenceLevel { LOW, MEDIUM, HIGH }
enum class MatchLevel { RECOMMENDED, CAUTION, NOT_RECOMMENDED }
enum class GearStatus { COVERED, CHECK, MISSING, OPTIONAL }

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
        "This temporary profile uses questionnaire defaults until enough GPX history is imported."
}

data class GearItem(
    val id: String,
    val category: String,
    val brand: String?,
    val model: String?,
    val weightGrams: Int?,
    val available: Boolean
)

data class GearRecommendation(
    val category: String,
    val status: GearStatus,
    val rationale: String,
    val matchedGearItemId: String? = null
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
