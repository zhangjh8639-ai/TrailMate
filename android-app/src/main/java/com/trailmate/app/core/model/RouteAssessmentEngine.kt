package com.trailmate.app.core.model

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

object RouteAssessmentEngine {
    private const val MIN_HISTORICAL_DISTANCE_KM = 1.0
    private const val MIN_HISTORICAL_ASCENT_METERS = 100.0
    private const val ASCENT_METERS_PER_EFFECTIVE_KM = 100.0
    private const val MIN_HISTORICAL_EFFECTIVE_SPEED_KMH = 1.0

    fun assess(
        profile: BaselineProfile,
        route: ImportedRoute,
        historicalActivities: List<HistoricalActivity> = emptyList()
    ): RouteAssessmentSummary {
        require(route.readyForAssessment()) { "Route must be parsed before assessment." }

        val capacity = AssessmentCapacity.from(profile = profile, historicalActivities = historicalActivities)
        val distanceRatio = route.distanceKm / capacity.stableDistanceKm
        val ascentRatio = route.ascentMeters.toDouble() / capacity.stableAscentMeters
        val distanceRisk = riskForRatio(distanceRatio)
        val ascentRisk = riskForRatio(ascentRatio)
        val totalRisk = (distanceRisk * 0.48 + ascentRisk * 0.52).roundToInt()
        val matchLevel = when {
            totalRisk < 35 -> MatchLevel.RECOMMENDED
            totalRisk < 72 -> MatchLevel.CAUTION
            else -> MatchLevel.NOT_RECOMMENDED
        }
        val durationHours = estimatedDurationHours(
            route = route,
            profile = profile,
            capacity = capacity,
            distanceRatio = distanceRatio,
            ascentRatio = ascentRatio
        )
        val risks = buildRisks(
            route = route,
            capacity = capacity,
            distanceRatio = distanceRatio,
            ascentRatio = ascentRatio
        )

        return RouteAssessmentSummary(
            routeName = route.routeName,
            distanceKm = route.distanceKm,
            ascentMeters = route.ascentMeters,
            matchLevel = matchLevel,
            confidenceLevel = capacity.confidenceLevel,
            estimatedDurationRange = durationRange(durationHours, capacity.confidenceLevel),
            risks = risks
        )
    }

    private fun riskForRatio(ratio: Double): Int =
        when {
            ratio <= 0.80 -> 0
            ratio <= 1.00 -> interpolate(ratio, 0.80, 1.00, 0, 20)
            ratio <= 1.20 -> interpolate(ratio, 1.00, 1.20, 20, 50)
            ratio <= 1.50 -> interpolate(ratio, 1.20, 1.50, 50, 82)
            else -> 95
        }

    private fun interpolate(
        ratio: Double,
        minRatio: Double,
        maxRatio: Double,
        minScore: Int,
        maxScore: Int
    ): Int {
        val progress = ((ratio - minRatio) / (maxRatio - minRatio)).coerceIn(0.0, 1.0)
        return (minScore + (maxScore - minScore) * progress).roundToInt()
    }

    private fun estimatedDurationHours(
        route: ImportedRoute,
        profile: BaselineProfile,
        capacity: AssessmentCapacity,
        distanceRatio: Double,
        ascentRatio: Double
    ): Double {
        val historicalEffectiveSpeedKmh = capacity.historicalEffectiveSpeedKmh
        val baseHours = if (historicalEffectiveSpeedKmh != null) {
            route.effectiveDistanceKm() / historicalEffectiveSpeedKmh
        } else {
            route.distanceKm / profile.flatSpeedKmh() + route.ascentMeters / profile.ascentMetersPerHour()
        }
        val overloadPenalty = 1.0 + maxOf(distanceRatio - 1.0, 0.0) * 0.08 + maxOf(ascentRatio - 1.0, 0.0) * 0.12
        return baseHours * overloadPenalty
    }

    private fun durationRange(hours: Double, confidenceLevel: ConfidenceLevel): String {
        val lowerFactor = if (confidenceLevel == ConfidenceLevel.LOW) 0.85 else 0.90
        val upperFactor = if (confidenceLevel == ConfidenceLevel.LOW) 1.35 else 1.20

        return "${formatHours(hours * lowerFactor)}-${formatHours(hours * upperFactor)}"
    }

    private fun formatHours(hours: Double): String {
        val totalMinutes = ceil(hours * 60).roundToInt()
        val hourPart = totalMinutes / 60
        val minutePart = totalMinutes % 60

        return "$hourPart:${minutePart.toString().padStart(2, '0')}"
    }

    private fun buildRisks(
        route: ImportedRoute,
        capacity: AssessmentCapacity,
        distanceRatio: Double,
        ascentRatio: Double
    ): List<String> {
        val risks = mutableListOf<String>()
        if (distanceRatio > 1.05) {
            risks += "Distance exceeds ${capacity.sourceLabel} stable range (${capacity.stableDistanceKm.roundToInt()} km)."
        }
        if (ascentRatio > 1.05) {
            risks += "Route ascent exceeds ${capacity.sourceLabel} ascent range (+${capacity.stableAscentMeters.roundToInt()} m)."
        }
        if (route.pointCount < 10) {
            risks += "Route has sparse GPX points; import a detailed track before relying on segment timing."
        }
        risks += capacity.evidenceLine

        return risks
    }

    private fun BaselineProfile.flatSpeedKmh(): Double =
        when (experienceLevel) {
            ExperienceLevel.BEGINNER -> 3.6
            ExperienceLevel.REGULAR -> 4.0
            ExperienceLevel.EXPERIENCED -> 4.5
        } + when (exerciseFrequency) {
            ExerciseFrequency.RARELY -> -0.2
            ExerciseFrequency.ONE_TO_TWO_PER_WEEK -> 0.0
            ExerciseFrequency.THREE_PLUS_PER_WEEK -> 0.3
        }

    private fun BaselineProfile.ascentMetersPerHour(): Double =
        when (ascentExperience) {
            AscentExperience.UNDER_300 -> 340.0
            AscentExperience.M300_TO_800 -> 430.0
            AscentExperience.OVER_800 -> 560.0
        }

    private data class AssessmentCapacity(
        val stableDistanceKm: Double,
        val stableAscentMeters: Double,
        val confidenceLevel: ConfidenceLevel,
        val sourceLabel: String,
        val evidenceLine: String,
        val historicalEffectiveSpeedKmh: Double? = null
    ) {
        companion object {
            fun from(
                profile: BaselineProfile,
                historicalActivities: List<HistoricalActivity>
            ): AssessmentCapacity {
                if (historicalActivities.size >= 3) {
                    val longestDistance = historicalActivities.maxOf { activity -> activity.distanceKm }
                    val longestAscent = historicalActivities.maxOf { activity -> activity.ascentMeters }

                    return AssessmentCapacity(
                        stableDistanceKm = longestDistance.coerceAtLeast(MIN_HISTORICAL_DISTANCE_KM),
                        stableAscentMeters = longestAscent.toDouble().coerceAtLeast(MIN_HISTORICAL_ASCENT_METERS),
                        confidenceLevel = ConfidenceLevel.MEDIUM,
                        sourceLabel = "historical GPX",
                        evidenceLine = "Historical GPX evidence covers up to ${
                            String.format(Locale.US, "%.1f", longestDistance)
                        } km / +$longestAscent m.",
                        historicalEffectiveSpeedKmh = historicalActivities.historicalEffectiveSpeedKmh()
                    )
                }

                return AssessmentCapacity(
                    stableDistanceKm = profile.stableDistanceKm(),
                    stableAscentMeters = profile.questionnaireStableAscentMeters(),
                    confidenceLevel = ConfidenceLevel.LOW,
                    sourceLabel = "questionnaire",
                    evidenceLine = "Confidence stays LOW until at least 3 historical GPX activities are imported."
                )
            }
        }
    }

    private fun BaselineProfile.questionnaireStableAscentMeters(): Double =
        when (ascentExperience) {
            AscentExperience.UNDER_300 -> 300.0
            AscentExperience.M300_TO_800 -> 700.0
            AscentExperience.OVER_800 -> 1_100.0
        }

    private fun BaselineProfile.stableDistanceKm(): Double {
        val durationBase = when (typicalDuration) {
            TypicalDuration.UNDER_30 -> 4.0
            TypicalDuration.MIN_30_TO_60 -> 12.0
            TypicalDuration.OVER_60 -> 14.0
        }
        val exerciseBoost = when (exerciseFrequency) {
            ExerciseFrequency.RARELY -> 0.85
            ExerciseFrequency.ONE_TO_TWO_PER_WEEK -> 1.0
            ExerciseFrequency.THREE_PLUS_PER_WEEK -> 1.2
        }
        val experienceBoost = when (experienceLevel) {
            ExperienceLevel.BEGINNER -> 0.9
            ExperienceLevel.REGULAR -> 1.0
            ExperienceLevel.EXPERIENCED -> 1.35
        }

        return durationBase * exerciseBoost * experienceBoost
    }

    private fun List<HistoricalActivity>.historicalEffectiveSpeedKmh(): Double? {
        val validActivities = filter { activity ->
            activity.durationMinutes > 0 && activity.effectiveDistanceKm() > 0.0
        }
        val totalHours = validActivities.sumOf { activity -> activity.durationMinutes }.toDouble() / 60.0
        val totalEffectiveDistanceKm = validActivities.sumOf { activity -> activity.effectiveDistanceKm() }
        val speedKmh = totalEffectiveDistanceKm / totalHours

        return speedKmh
            .takeIf { it.isFinite() && it > 0.0 }
            ?.coerceAtLeast(MIN_HISTORICAL_EFFECTIVE_SPEED_KMH)
    }

    private fun HistoricalActivity.effectiveDistanceKm(): Double =
        distanceKm + ascentMeters / ASCENT_METERS_PER_EFFECTIVE_KM

    private fun ImportedRoute.effectiveDistanceKm(): Double =
        distanceKm + ascentMeters / ASCENT_METERS_PER_EFFECTIVE_KM
}
