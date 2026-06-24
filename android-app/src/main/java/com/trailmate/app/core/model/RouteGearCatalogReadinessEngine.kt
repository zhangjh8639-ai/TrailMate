package com.trailmate.app.core.model

object RouteGearCatalogReadinessEngine {
    fun resolve(
        route: ImportedRoute,
        assessment: RouteAssessmentSummary,
        catalogItems: List<GearCatalogItem>
    ): List<GearRecommendation> {
        val routeNeeds = RouteGearAdvisorEngine.recommend(
            route = route,
            assessment = assessment
        )

        return GearCatalogSelectionEngine.resolveRouteMatchesForDeparture(
            recommendations = routeNeeds,
            catalogItems = catalogItems
        )
    }
}
