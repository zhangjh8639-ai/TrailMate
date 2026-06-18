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
        val profileToken = snapshot.profile?.initialConfidence()?.name ?: "none"
        val routeToken = snapshot.importedRoute?.routeName ?: "none"

        return TrailMateDataControlSummary(
            profileLine = profileLine,
            routeLine = routeLine,
            inventoryLine = "$inventoryCount items / $readyCount ready",
            exportPreview = "profile=$profileToken; route=$routeToken; gear=$inventoryCount"
        )
    }
}
