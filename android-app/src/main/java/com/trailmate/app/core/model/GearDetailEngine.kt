package com.trailmate.app.core.model

data class GearDetailSummary(
    val title: String,
    val category: String,
    val statusLine: String,
    val routeMatchLine: String,
    val routeRationale: String
)

object GearDetailEngine {
    fun summarize(
        item: GearItem,
        routeGearRecommendations: List<GearRecommendation>
    ): GearDetailSummary {
        val title = listOfNotNull(item.brand, item.model)
            .joinToString(" ")
            .ifBlank { item.category }
        val weight = item.weightGrams?.let { "${it}g" } ?: "weight TBD"
        val availability = if (item.available) "ready" else "not packed"
        val routeMatch = routeGearRecommendations.firstOrNull { recommendation ->
            recommendation.matchedGearItemId == item.id
        }

        return GearDetailSummary(
            title = title,
            category = item.category,
            statusLine = "$weight / $availability",
            routeMatchLine = routeMatch?.let { recommendation ->
                "Matches ${recommendation.category} recommendation."
            } ?: "No current route match.",
            routeRationale = routeMatch?.rationale
                ?: "Add or import a route recommendation to see why this item matters."
        )
    }
}
