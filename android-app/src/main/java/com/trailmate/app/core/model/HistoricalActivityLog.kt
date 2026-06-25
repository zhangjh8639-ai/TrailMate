package com.trailmate.app.core.model

import java.util.Locale

data class HistoricalActivityLog(
    val activities: List<HistoricalActivity>
) {
    fun addAll(newActivities: List<HistoricalActivity>): HistoricalActivityLog {
        val seen = activities.map { it.key() }.toMutableSet()
        val deduped = activities.toMutableList()

        newActivities.forEach { activity ->
            if (seen.add(activity.key())) {
                deduped += activity
            }
        }

        return copy(activities = deduped)
    }

    fun remove(activityKey: String): HistoricalActivityLog {
        var removed = false
        return copy(
            activities = activities.filterNot { activity ->
                val shouldRemove = !removed && activity.key() == activityKey
                if (shouldRemove) {
                    removed = true
                }
                shouldRemove
            }
        )
    }
}

fun HistoricalActivity.key(): String =
    listOf(
        routeName.trim().lowercase(),
        String.format(Locale.US, "%.1f", distanceKm),
        ascentMeters.toString(),
        durationMinutes.toString()
    ).joinToString("|")

fun HistoricalActivity.summaryLabel(): String {
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60

    return String.format(
        Locale.US,
        "%.1f km / +%d m / %d:%02d",
        distanceKm,
        ascentMeters,
        hours,
        minutes
    )
}
