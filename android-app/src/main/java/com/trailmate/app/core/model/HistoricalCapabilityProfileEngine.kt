package com.trailmate.app.core.model

import kotlin.math.roundToInt

data class HistoricalCapabilityProfile(
    val activityCount: Int,
    val stableDistanceKm: Double,
    val stableAscentMeters: Double,
    val averageDistanceKm: Double,
    val averageAscentMeters: Int,
    val averagePaceMinutesPerKm: Double?,
    val effectiveSpeedKmh: Double?,
    val confidenceLevel: ConfidenceLevel
)

object HistoricalCapabilityProfileEngine {
    const val REQUIRED_HISTORY_COUNT = 3
    private const val MIN_STABLE_DISTANCE_KM = 1.0
    private const val MIN_STABLE_ASCENT_METERS = 100.0
    private const val ASCENT_METERS_PER_EFFECTIVE_KM = 100.0
    private const val MIN_EFFECTIVE_SPEED_KMH = 1.0

    fun build(activities: List<HistoricalActivity>): HistoricalCapabilityProfile? {
        if (activities.size < REQUIRED_HISTORY_COUNT) {
            return null
        }

        val finiteDistances = activities.mapNotNull { activity ->
            activity.distanceKm.takeIf { distance -> distance.isFinite() && distance > 0.0 }
        }
        val saneAscents = activities.map { activity ->
            activity.ascentMeters.coerceAtLeast(0)
        }

        return HistoricalCapabilityProfile(
            activityCount = activities.size,
            stableDistanceKm = (finiteDistances.maxOrNull() ?: 0.0).coerceAtLeast(MIN_STABLE_DISTANCE_KM),
            stableAscentMeters = (saneAscents.maxOrNull() ?: 0).toDouble().coerceAtLeast(MIN_STABLE_ASCENT_METERS),
            averageDistanceKm = finiteDistances.averageFiniteOrZero(),
            averageAscentMeters = saneAscents.average().roundToInt(),
            averagePaceMinutesPerKm = activities.averagePaceMinutesPerKm(),
            effectiveSpeedKmh = activities.effectiveSpeedKmh(),
            confidenceLevel = ConfidenceLevel.MEDIUM
        )
    }

    private fun List<HistoricalActivity>.averagePaceMinutesPerKm(): Double? {
        val validActivities = filter { activity ->
            activity.distanceKm.isFinite() && activity.distanceKm > 0.0 && activity.durationMinutes > 0
        }
        val totalDistanceKm = validActivities.sumOf { activity -> activity.distanceKm }
        val totalMinutes = validActivities.sumOf { activity -> activity.durationMinutes }
        val pace = totalMinutes / totalDistanceKm

        return pace.takeIf { value -> value.isFinite() && value > 0.0 }
    }

    private fun List<HistoricalActivity>.effectiveSpeedKmh(): Double? {
        val validActivities = filter { activity ->
            activity.durationMinutes > 0 && activity.effectiveDistanceKm().let { distance ->
                distance.isFinite() && distance > 0.0
            }
        }
        val totalHours = validActivities.sumOf { activity -> activity.durationMinutes }.toDouble() / 60.0
        val totalEffectiveDistanceKm = validActivities.sumOf { activity -> activity.effectiveDistanceKm() }
        val speedKmh = totalEffectiveDistanceKm / totalHours

        return speedKmh
            .takeIf { value -> value.isFinite() && value > 0.0 }
            ?.coerceAtLeast(MIN_EFFECTIVE_SPEED_KMH)
    }

    private fun HistoricalActivity.effectiveDistanceKm(): Double =
        distanceKm + ascentMeters / ASCENT_METERS_PER_EFFECTIVE_KM

    private fun List<Double>.averageFiniteOrZero(): Double {
        if (isEmpty()) {
            return 0.0
        }

        return average().takeIf { value -> value.isFinite() } ?: 0.0
    }
}
