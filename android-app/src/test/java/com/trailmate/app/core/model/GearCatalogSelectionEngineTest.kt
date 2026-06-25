package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GearCatalogSelectionEngineTest {
    @Test
    fun catalogMatchingUsesCatalogItemsDirectly() {
        val item = GearCatalogItem(
            catalogItemId = "cat_headlamp_bd_spot_400",
            category = "头灯",
            brand = "Black Diamond",
            model = "Spot 400",
            displayName = "Black Diamond Spot 400",
            weightGrams = 78,
            tags = listOf("夜间"),
            source = "seed"
        )

        val matches = GearCatalogSelectionEngine.matchCatalogItems(
            catalogItems = listOf(item),
            routeCategory = "头灯",
            query = "spot"
        )

        assertEquals(listOf(item), matches)
    }

    @Test
    fun catalogMatchingTreatsShortRouteCategoryAsServerCategoryAlias() {
        val rainShell = GearCatalogItem(
            catalogItemId = "cat_rain_arcteryx_beta_lt",
            category = "雨衣（防水透气）",
            brand = "Arc'teryx",
            model = "Beta LT Jacket",
            displayName = "Arc'teryx Beta LT Jacket",
            weightGrams = 395,
            tags = emptyList(),
            imageUrl = "https://cdn.trailmate.local/gear/arcteryx-beta-lt.png",
            source = "seed"
        )

        val matches = GearCatalogSelectionEngine.matchCatalogItems(
            catalogItems = listOf(rainShell),
            routeCategory = "雨衣",
            query = ""
        )

        assertEquals(listOf(rainShell), matches)
    }

    @Test
    fun catalogMatchingDoesNotRequirePersonalInventory() {
        val firstAid = GearCatalogItem(
            catalogItemId = "cat_first_aid_adventure_medical_ultralight",
            category = "急救包",
            brand = "Adventure Medical Kits",
            model = "Ultralight/Watertight .7",
            displayName = "Adventure Medical Kits Ultralight/Watertight .7",
            weightGrams = 227,
            tags = listOf("急救"),
            imageUrl = "https://cdn.trailmate.local/gear/adventure-medical-ultralight-7.png",
            source = "seed"
        )

        val matches = GearCatalogSelectionEngine.matchCatalogItems(
            catalogItems = listOf(firstAid),
            routeCategory = "急救包",
            query = "medical"
        )

        assertEquals("cat_first_aid_adventure_medical_ultralight", matches.single().catalogItemId)
        assertEquals("https://cdn.trailmate.local/gear/adventure-medical-ultralight-7.png", matches.single().imageUrl)
    }

    @Test
    fun serverThumbnailUrlIsPreferredWhenPresent() {
        val withImage = GearCatalogItem(
            catalogItemId = "cat_headlamp_bd_spot_400",
            category = "头灯",
            brand = "Black Diamond",
            model = "Spot 400",
            displayName = "Black Diamond Spot 400",
            weightGrams = 78,
            tags = emptyList(),
            imageUrl = "https://cdn.trailmate.local/gear/black-diamond-spot-400.png",
            source = "seed"
        )
        val withoutImage = withImage.copy(catalogItemId = "cat_headlamp_no_image", imageUrl = null)

        assertTrue(GearCatalogThumbnailPolicy.shouldLoadServerThumbnail(withImage))
        assertFalse(GearCatalogThumbnailPolicy.shouldLoadServerThumbnail(withoutImage))
    }

    @Test
    fun previewCatalogCarriesServerThumbnailPathsWithoutDirectLoadingRelativeUrls() {
        val previewItems = TrailMateGearCatalogPreviewData.items

        assertTrue(previewItems.all { item -> item.imageUrl?.startsWith("/gear-thumbnails/") == true })
        assertTrue(previewItems.all { item -> item.imageAttribution == "TrailMate hosted catalog thumbnail" })
        assertFalse(GearCatalogThumbnailPolicy.shouldLoadServerThumbnail(previewItems.first()))
    }

    @Test
    fun matchedCatalogItemPresentsAsMatchedEvenWhenAiMarkedCategoryMissing() {
        val recommendation = GearRecommendation(
            category = "雨衣",
            status = GearStatus.MISSING,
            rationale = "山脊暴露，需要防水层。"
        )
        val item = GearCatalogItem(
            catalogItemId = "cat_rain_arcteryx_beta_lt",
            category = "雨衣（防水透气）",
            brand = "Arc'teryx",
            model = "Beta LT Jacket",
            displayName = "Arc'teryx Beta LT Jacket",
            weightGrams = 395,
            tags = emptyList(),
            source = "seed"
        )

        val presentation = GearCatalogSelectionEngine.presentRouteMatch(
            recommendation = recommendation,
            matchedCatalogItem = item
        )

        assertEquals("已匹配 Arc'teryx Beta LT Jacket", presentation.statusLine)
        assertEquals(GearCatalogRouteMatchTone.MATCHED, presentation.tone)
    }

    @Test
    fun catalogMatchesResolveMissingRouteGearForDeparture() {
        val recommendations = listOf(
            GearRecommendation(
                category = "登山杖",
                status = GearStatus.MISSING,
                rationale = "长距离下坡建议准备。"
            ),
            GearRecommendation(
                category = "头灯",
                status = GearStatus.CHECK,
                rationale = "出发前检查电量。"
            )
        )
        val catalogItems = listOf(
            GearCatalogItem(
                catalogItemId = "cat_poles_leki_legacy_lite",
                category = "登山杖",
                brand = "Leki",
                model = "Legacy Lite AS",
                displayName = "Leki Legacy Lite AS",
                weightGrams = 510,
                tags = listOf("长距离"),
                source = "seed"
            )
        )

        val resolved = GearCatalogSelectionEngine.resolveRouteMatchesForDeparture(
            recommendations = recommendations,
            catalogItems = catalogItems
        )

        assertEquals(GearStatus.COVERED, resolved.first { it.category == "登山杖" }.status)
        assertEquals("cat_poles_leki_legacy_lite", resolved.first { it.category == "登山杖" }.matchedGearItemId)
        assertEquals(GearStatus.CHECK, resolved.first { it.category == "头灯" }.status)
    }
}
