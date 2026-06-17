package com.trailmate.app.core.model

import kotlin.math.ceil
import kotlin.math.roundToInt

object HikePlanEngine {
    fun build(route: ImportedRoute, assessment: RouteAssessmentSummary): HikePlanSummary {
        require(route.readyForAssessment()) { "Route must be parsed before building a hike plan." }

        val upperDurationMinutes = upperDurationMinutes(
            estimatedDurationRange = assessment.estimatedDurationRange,
            route = route
        )
        val checkpoints = mutableListOf(
            HikePlanCheckpoint(
                type = HikePlanCheckpointType.START,
                title = "Start",
                distanceKm = 0.0,
                timeFromStart = "0:00",
                note = "Confirm offline GPX, weather, daylight, and battery before leaving."
            )
        )

        if (route.distanceKm >= 3.0) {
            checkpoints += checkpointAt(
                type = HikePlanCheckpointType.ENERGY_CHECK,
                title = "Energy check",
                route = route,
                durationMinutes = upperDurationMinutes,
                progress = 0.35,
                note = "Check pace and snack plan before effort starts to build."
            )
        }

        checkpoints += checkpointAt(
            type = HikePlanCheckpointType.REST_CHECK,
            title = "Rest check",
            route = route,
            durationMinutes = upperDurationMinutes,
            progress = 0.58,
            note = "Take a short rest if effort feels higher than planned."
        )

        assessment.risks.firstOrNull()?.let { risk ->
            checkpoints += checkpointAt(
                type = HikePlanCheckpointType.RISK_CHECK,
                title = "Risk check",
                route = route,
                durationMinutes = upperDurationMinutes,
                progress = 0.74,
                note = "Review assessment flag: $risk"
            )
        }

        checkpoints += HikePlanCheckpoint(
            type = HikePlanCheckpointType.FINISH,
            title = "Finish",
            distanceKm = route.distanceKm.roundToTenth(),
            timeFromStart = formatMinutes(upperDurationMinutes),
            note = "Log actual time and route feel to improve future assessments."
        )

        return HikePlanSummary(checkpoints = checkpoints.take(MAX_CHECKPOINTS))
            .ensureFinish(route = route, upperDurationMinutes = upperDurationMinutes)
    }

    private fun checkpointAt(
        type: HikePlanCheckpointType,
        title: String,
        route: ImportedRoute,
        durationMinutes: Int,
        progress: Double,
        note: String
    ): HikePlanCheckpoint =
        HikePlanCheckpoint(
            type = type,
            title = title,
            distanceKm = (route.distanceKm * progress).roundToTenth(),
            timeFromStart = formatMinutes(ceil(durationMinutes * progress).roundToInt()),
            note = note
        )

    private fun HikePlanSummary.ensureFinish(
        route: ImportedRoute,
        upperDurationMinutes: Int
    ): HikePlanSummary {
        val finish = HikePlanCheckpoint(
            type = HikePlanCheckpointType.FINISH,
            title = "Finish",
            distanceKm = route.distanceKm.roundToTenth(),
            timeFromStart = formatMinutes(upperDurationMinutes),
            note = "Log actual time and route feel to improve future assessments."
        )
        val withoutFinish = checkpoints.filterNot { it.type == HikePlanCheckpointType.FINISH }

        return copy(checkpoints = (withoutFinish.take(MAX_CHECKPOINTS - 1) + finish))
    }

    private fun upperDurationMinutes(
        estimatedDurationRange: String,
        route: ImportedRoute
    ): Int {
        val upperText = estimatedDurationRange.substringAfter("-", estimatedDurationRange).trim()
        val match = DurationPattern.matchEntire(upperText)
        if (match != null) {
            val hours = match.groupValues[1].toInt()
            val minutes = match.groupValues[2].toInt()
            return (hours * 60 + minutes).coerceAtLeast(1)
        }

        val fallbackHours = route.distanceKm / 3.2 + route.ascentMeters / 300.0
        return ceil(fallbackHours * 60).roundToInt().coerceAtLeast(30)
    }

    private fun formatMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return "$hours:${minutes.toString().padStart(2, '0')}"
    }

    private fun Double.roundToTenth(): Double =
        (this * 10.0).roundToInt() / 10.0

    private val DurationPattern = Regex("""(\d+):([0-5]\d)""")
    private const val MAX_CHECKPOINTS = 6
}
