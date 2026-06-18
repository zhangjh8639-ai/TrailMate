package com.trailmate.app.core.persistence

import java.util.Locale

data class TrailMateDataControlSummary(
    val profileLine: String,
    val routeLine: String,
    val inventoryLine: String,
    val exportPreview: String
)

object TrailMateDataControlEngine {
    fun summarize(snapshot: TrailMateSnapshot): TrailMateDataControlSummary {
        val profileLine = snapshot.profile?.let { "Profile saved" } ?: "No profile saved"
        val routeLine = snapshot.importedRoute?.let { route ->
            "${route.routeName} / ${String.format(Locale.US, "%.1f km", route.distanceKm)} / +${route.ascentMeters} m"
        } ?: "No route imported"
        val inventoryCount = snapshot.inventory.items.size
        val readyCount = snapshot.inventory.items.count { item -> item.available }
        val historyCount = snapshot.historicalActivities.size

        return TrailMateDataControlSummary(
            profileLine = profileLine,
            routeLine = routeLine,
            inventoryLine = "$inventoryCount items / $readyCount ready",
            exportPreview = buildExportPreview(
                snapshot = snapshot,
                inventoryCount = inventoryCount,
                readyCount = readyCount,
                historyCount = historyCount
            )
        )
    }

    private fun buildExportPreview(
        snapshot: TrailMateSnapshot,
        inventoryCount: Int,
        readyCount: Int,
        historyCount: Int
    ): String {
        val profilePreview = if (snapshot.profile != null) {
            "Profile: saved"
        } else {
            "Profile: not saved"
        }
        val routePreview = snapshot.importedRoute?.let { route ->
            "Route: ${route.routeName}, ${String.format(Locale.US, "%.1f km", route.distanceKm)}, +${route.ascentMeters} m"
        } ?: "Route: none"

        return listOf(
            profilePreview,
            routePreview,
            "History: $historyCount GPX activities",
            "Gear: $inventoryCount items, $readyCount ready"
        ).joinToString("; ")
    }
}
