package com.trailmate.app.core.model

object RouteGearAdvisorEngine {
    fun recommend(
        route: ImportedRoute,
        assessment: RouteAssessmentSummary
    ): List<GearRecommendation> {
        require(route.readyForAssessment()) { "Route must be parsed before recommending gear." }

        val longRoute = route.distanceKm >= 10.0
        val highAscent = route.ascentMeters >= 600
        val lateOrLong = upperDurationMinutes(assessment.estimatedDurationRange) >= 300
        val hasConcreteRouteRisk = assessment.risks.any { risk ->
            !risk.contains("confidence", ignoreCase = true)
        }

        return listOf(
            GearRecommendation(
                category = "Rain shell",
                status = GearStatus.CHECK,
                rationale = "Check weather and pack a shell before committing to the route."
            ),
            GearRecommendation(
                category = "Headlamp",
                status = if (lateOrLong) GearStatus.CHECK else GearStatus.OPTIONAL,
                rationale = if (lateOrLong) {
                    "Estimated route time is long enough that light or battery margin should be checked."
                } else {
                    "Optional for short daylight routes; add one if timing may slip."
                }
            ),
            GearRecommendation(
                category = "Trekking poles",
                status = if (highAscent || longRoute) GearStatus.MISSING else GearStatus.OPTIONAL,
                rationale = if (highAscent || longRoute) {
                    "Distance or ascent suggests poles may reduce strain on long climbs and descents."
                } else {
                    "Optional for this shorter, lower-ascent route."
                }
            ),
            GearRecommendation(
                category = "Warm layer",
                status = if (highAscent || lateOrLong || hasConcreteRouteRisk) GearStatus.MISSING else GearStatus.OPTIONAL,
                rationale = if (highAscent || lateOrLong || hasConcreteRouteRisk) {
                    "Higher effort, elevation, or uncertainty can make stops feel cold."
                } else {
                    "Optional for short routes when weather stays mild."
                }
            ),
            GearRecommendation(
                category = "Extra water",
                status = if (longRoute || lateOrLong) GearStatus.MISSING else GearStatus.CHECK,
                rationale = if (longRoute || lateOrLong) {
                    "Longer route timing calls for extra water beyond your usual carry."
                } else {
                    "Check normal water capacity against current weather."
                }
            )
        )
    }

    private fun upperDurationMinutes(estimatedDurationRange: String): Int {
        val upperText = estimatedDurationRange.substringAfter("-", estimatedDurationRange).trim()
        val match = DurationPattern.matchEntire(upperText) ?: return 0
        val hours = match.groupValues[1].toInt()
        val minutes = match.groupValues[2].toInt()

        return hours * 60 + minutes
    }

    private val DurationPattern = Regex("""(\d+):([0-5]\d)""")
}
