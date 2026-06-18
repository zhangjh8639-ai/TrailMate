package com.trailmate.app.core.model

import java.util.Locale

object CapabilityProfileEngine {
    fun build(
        baselineProfile: BaselineProfile,
        historicalActivities: List<HistoricalActivity>
    ): CapabilityProfileSummary {
        val historicalProfile = HistoricalCapabilityProfileEngine.build(historicalActivities)
        if (historicalProfile == null) {
            return CapabilityProfileSummary(
                title = "Questionnaire fallback",
                value = baselineProfile.initialConfidence().name,
                caption = "Use sample history to preview how 3 GPX activities can calibrate distance and ascent capacity.",
                confidenceLevel = ConfidenceLevel.LOW,
                evidenceLabel = "${historicalActivities.size}/${HistoricalCapabilityProfileEngine.REQUIRED_HISTORY_COUNT} GPX"
            )
        }

        val paceCopy = historicalProfile.averagePaceMinutesPerKm?.let { pace ->
            String.format(Locale.US, " Pace %.0f min/km.", pace)
        }.orEmpty()

        return CapabilityProfileSummary(
            title = "Historical profile",
            value = String.format(
                Locale.US,
                "Longest %.1f km / +%d m",
                historicalProfile.stableDistanceKm,
                historicalProfile.stableAscentMeters.toInt()
            ),
            caption = String.format(
                Locale.US,
                "Average %.1f km / +%d m across %d GPX activities.%s",
                historicalProfile.averageDistanceKm,
                historicalProfile.averageAscentMeters,
                historicalProfile.activityCount,
                paceCopy
            ),
            confidenceLevel = historicalProfile.confidenceLevel,
            evidenceLabel = "${historicalProfile.activityCount}/${HistoricalCapabilityProfileEngine.REQUIRED_HISTORY_COUNT} GPX"
        )
    }
}
