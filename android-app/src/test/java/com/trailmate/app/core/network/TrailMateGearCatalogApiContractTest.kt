package com.trailmate.app.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateGearCatalogApiContractTest {
    @Test
    fun exposesGearCatalogEndpoints() {
        assertEquals("/gear/catalog/categories", TrailMateServerApiContract.Endpoints.gearCatalogCategories)
        assertEquals("/gear/catalog/search", TrailMateServerApiContract.Endpoints.gearCatalogSearch)
    }

    @Test
    fun catalogDtoCarriesServerOwnedGearFields() {
        val dto = TrailMateGearCatalogItemDto(
            catalogItemId = "cat_rain_arcteryx_beta_lt",
            category = "雨衣（防水透气）",
            brand = "Arc'teryx",
            model = "Beta LT Jacket",
            displayName = "Arc'teryx Beta LT Jacket",
            weightGrams = 395,
            tags = listOf("防水", "硬壳"),
            imageUrl = "https://cdn.trailmate.local/gear/arcteryx-beta-lt.png",
            imageAttribution = "TrailMate catalog seed image",
            source = "seed"
        )

        assertEquals("cat_rain_arcteryx_beta_lt", dto.catalogItemId)
        assertEquals("Arc'teryx Beta LT Jacket", dto.displayName)
        assertEquals("https://cdn.trailmate.local/gear/arcteryx-beta-lt.png", dto.imageUrl)
        assertEquals(false, dto.tags.isEmpty())
    }
}
