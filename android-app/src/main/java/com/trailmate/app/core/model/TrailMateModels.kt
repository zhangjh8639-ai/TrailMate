package com.trailmate.app.core.model

enum class ExerciseFrequency { RARELY, ONE_TO_TWO_PER_WEEK, THREE_PLUS_PER_WEEK }
enum class TypicalDuration { UNDER_30, MIN_30_TO_60, OVER_60 }
enum class ExperienceLevel { BEGINNER, REGULAR, EXPERIENCED }
enum class AscentExperience { UNDER_300, M300_TO_800, OVER_800 }
enum class ConfidenceLevel { LOW, MEDIUM, HIGH }
enum class MatchLevel { RECOMMENDED, CAUTION, NOT_RECOMMENDED }
enum class GearStatus { COVERED, CHECK, MISSING, OPTIONAL }
enum class RouteImportStatus { EMPTY, PARSED }

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

    fun bodyMetricsLabel(): String {
        val height = heightCm?.let { "${it}cm" }
        val weight = weightKg?.let { "${it}kg" }

        return listOfNotNull(height, weight).joinToString(" / ").ifBlank { "Not set" }
    }

    fun packWeightLabel(): String =
        commonPackWeightKg?.let { "$it kg pack" } ?: "Pack TBD"
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

data class GearInventory(
    val items: List<GearItem>
) {
    fun addBrandGear(
        category: String,
        brand: String?,
        model: String?,
        weightGrams: Int?
    ): GearInventory {
        val normalizedCategory = category.trim()
        require(normalizedCategory.isNotBlank()) { "Gear category is required." }
        require(weightGrams == null || weightGrams >= 0) { "Gear weight cannot be negative." }

        val item = GearItem(
            id = nextItemId(category = normalizedCategory, brand = brand.orEmpty(), model = model.orEmpty()),
            category = normalizedCategory,
            brand = brand?.trim().orEmpty().ifBlank { null },
            model = model?.trim().orEmpty().ifBlank { null },
            weightGrams = weightGrams,
            available = true
        )

        return copy(items = items + item)
    }

    fun setAvailability(itemId: String, available: Boolean): GearInventory =
        copy(
            items = items.map { item ->
                if (item.id == itemId) item.copy(available = available) else item
            }
        )

    fun remove(itemId: String): GearInventory =
        copy(items = items.filterNot { it.id == itemId })

    fun applyTo(recommendation: GearRecommendation): GearRecommendation {
        val match = availableMatchFor(recommendation)
        if (match == null) {
            return recommendation.copy(
                status = when (recommendation.status) {
                    GearStatus.COVERED,
                    GearStatus.CHECK -> GearStatus.MISSING
                    else -> recommendation.status
                },
                matchedGearItemId = null
            )
        }

        return recommendation.copy(
            status = if (recommendation.status == GearStatus.MISSING) {
                GearStatus.COVERED
            } else {
                recommendation.status
            },
            matchedGearItemId = match.id
        )
    }

    fun applyTo(recommendations: List<GearRecommendation>): List<GearRecommendation> =
        recommendations.map(::applyTo)

    private fun availableMatchFor(recommendation: GearRecommendation): GearItem? {
        val explicitMatch = recommendation.matchedGearItemId?.let { matchedId ->
            items.firstOrNull { item ->
                item.id == matchedId &&
                    item.available &&
                    item.category.equals(recommendation.category, ignoreCase = true)
            }
        }

        return explicitMatch ?: items.firstOrNull { item ->
            item.available && item.category.equals(recommendation.category, ignoreCase = true)
        }
    }

    private fun nextItemId(category: String, brand: String, model: String): String {
        val base = listOf(category, brand, model)
            .joinToString("-")
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "gear-item" }
        var index = 1
        var candidate = "$base-$index"

        while (items.any { it.id == candidate }) {
            index += 1
            candidate = "$base-$index"
        }

        return candidate
    }
}

data class ImportedRoute(
    val routeName: String,
    val fileName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val status: RouteImportStatus
) {
    fun readyForAssessment(): Boolean = status == RouteImportStatus.PARSED

    fun summaryLabel(): String = "${distanceKm} km / +${ascentMeters} m"
}

data class RouteAssessmentSummary(
    val routeName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val matchLevel: MatchLevel,
    val confidenceLevel: ConfidenceLevel,
    val estimatedDurationRange: String,
    val risks: List<String>
)
