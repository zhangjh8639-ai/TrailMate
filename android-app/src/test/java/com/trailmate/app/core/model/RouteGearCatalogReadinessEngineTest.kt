package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteGearCatalogReadinessEngineTest {
    @Test
    fun resolvesRouteGearNeedsFromServerCatalogCandidates() {
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = TrailMateSampleData.importedTargetRoute
        )
        val catalogItems = listOf(
            GearCatalogItem(
                catalogItemId = "cat_poles_leki_legacy_lite",
                category = "登山杖",
                brand = "Leki",
                model = "Legacy Lite AS",
                displayName = "Leki Legacy Lite AS",
                weightGrams = 510,
                tags = listOf("长距离", "下坡", "稳定"),
                imageUrl = "https://cdn.trailmate.local/gear/leki-legacy-lite-as.png",
                source = "server"
            )
        )

        val recommendations = RouteGearCatalogReadinessEngine.resolve(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = assessment,
            catalogItems = catalogItems
        )

        val poles = recommendations.first { it.category == "登山杖" }
        assertEquals(GearStatus.COVERED, poles.status)
        assertEquals("cat_poles_leki_legacy_lite", poles.matchedGearItemId)
    }
}
