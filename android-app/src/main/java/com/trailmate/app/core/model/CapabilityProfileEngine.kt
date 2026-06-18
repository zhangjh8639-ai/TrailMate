package com.trailmate.app.core.model

import java.util.Locale
import kotlin.math.roundToInt

object CapabilityProfileEngine {
    private const val REQUIRED_HISTORY_COUNT = 3

    fun build(
        baselineProfile: BaselineProfile,
        historicalActivities: List<HistoricalActivity>
    ): CapabilityProfileSummary {
        if (historicalActivities.size < REQUIRED_HISTORY_COUNT) {
            return CapabilityProfileSummary(
                title = "Questionnaire fallback",
                value = baselineProfile.initialConfidence().name,
                caption = "Use sample history to preview how 3 GPX activities can calibrate distance and ascent capacity.",
                confidenceLevel = ConfidenceLevel.LOW,
                evidenceLabel = "${historicalActivities.size}/$REQUIRED_HISTORY_COUNT GPX"
            )
        }

        val longest = historicalActivities.maxBy { it.distanceKm }
        val averageDistance = historicalActivities.map { it.distanceKm }.average()
        val averageAscent = historicalActivities.map { it.ascentMeters }.average().roundToInt()
        val averagePaceMinutesPerKm = historicalActivities.averagePaceMinutesPerKm()
        val paceCopy = averagePaceMinutesPerKm?.let { pace ->
            String.format(Locale.US, " Pace %.0f min/km.", pace)
        }.orEmpty()

        return CapabilityProfileSummary(
            title = "Historical profile",
            value = String.format(Locale.US, "Longest %.1f km / +%d m", longest.distanceKm, longest.ascentMeters),
            caption = String.format(
                Locale.US,
                "Average %.1f km / +%d m across %d GPX activities.%s",
                averageDistance,
                averageAscent,
                historicalActivities.size,
                paceCopy
            ),
            confidenceLevel = ConfidenceLevel.MEDIUM,
            evidenceLabel = "${historicalActivities.size}/$REQUIRED_HISTORY_COUNT GPX"
        )
    }

    private fun List<HistoricalActivity>.averagePaceMinutesPerKm(): Double? {
        val validActivities = filter { activity ->
            activity.distanceKm > 0.0 && activity.durationMinutes > 0
        }
        val totalDistanceKm = validActivities.sumOf { it.distanceKm }
        val totalMinutes = validActivities.sumOf { it.durationMinutes }
        val pace = totalMinutes / totalDistanceKm

        return pace.takeIf { it.isFinite() && it > 0.0 }
    }
}
