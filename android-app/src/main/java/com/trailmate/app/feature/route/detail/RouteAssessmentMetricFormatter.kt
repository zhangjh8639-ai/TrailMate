package com.trailmate.app.feature.route.detail

internal object RouteAssessmentMetricFormatter {
    private val durationRangePattern = Regex("""^\s*(\d+):([0-5]\d)\s*-\s*(\d+):([0-5]\d)\s*$""")

    fun durationRangeForMetric(value: String): String {
        val match = durationRangePattern.matchEntire(value) ?: return value
        val (lowerHours, lowerMinutes, upperHours, upperMinutes) = match.destructured
        if (lowerHours.toInt() >= 10 || upperHours.toInt() >= 10) {
            val roundedUpperHours = upperHours.toInt() + if (upperMinutes.toInt() > 0) 1 else 0
            return "${lowerHours.toInt()}-${roundedUpperHours}h"
        }
        return "${lowerHours}h$lowerMinutes-${upperHours}h$upperMinutes"
    }
}
