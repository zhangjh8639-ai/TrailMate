package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GearInventoryRulesTest {
    @Test
    fun availableBrandGearCoversMatchingRecommendationCategory() {
        val inventory = GearInventory(
            items = listOf(
                GearItem(
                    id = "poles-1",
                    category = "登山杖",
                    brand = "Leki",
                    model = "Makalu Lite",
                    weightGrams = 510,
                    available = true
                )
            )
        )

        val updated = inventory.applyTo(
            GearRecommendation(
                category = "登山杖",
                status = GearStatus.MISSING,
                rationale = "Long descent and late climb make poles useful."
            )
        )

        assertEquals(GearStatus.COVERED, updated.status)
        assertEquals("poles-1", updated.matchedGearItemId)
    }

    @Test
    fun unavailableBrandGearDoesNotCoverRecommendationCategory() {
        val inventory = GearInventory(
            items = listOf(
                GearItem(
                    id = "warm-1",
                    category = "保暖层",
                    brand = "Arc'teryx",
                    model = "Atom Hoody",
                    weightGrams = 370,
                    available = false
                )
            )
        )

        val updated = inventory.applyTo(
            GearRecommendation(
                category = "保暖层",
                status = GearStatus.MISSING,
                rationale = "High point stops and late finish may feel cold."
            )
        )

        assertEquals(GearStatus.MISSING, updated.status)
        assertEquals(null, updated.matchedGearItemId)
    }

    @Test
    fun coveredRecommendationBecomesMissingWhenOwnedGearIsUnavailable() {
        val inventory = GearInventory(emptyList())

        val updated = inventory.applyTo(
            GearRecommendation(
                category = "雨衣",
                status = GearStatus.COVERED,
                rationale = "Existing shell covers wind and light rain.",
                matchedGearItemId = "shell-1"
            )
        )

        assertEquals(GearStatus.MISSING, updated.status)
        assertEquals(null, updated.matchedGearItemId)
    }

    @Test
    fun addingBrandGearKeepsInventoryAppendOnly() {
        val inventory = GearInventory(TrailMateSampleData.gearItems)
        val updated = inventory.addBrandGear(
            category = "保暖层",
            brand = "Rab",
            model = "Xenair Alpine Light",
            weightGrams = 309
        )

        assertEquals(TrailMateSampleData.gearItems.size + 1, updated.items.size)
        assertTrue(updated.items.any { it.brand == "Rab" && it.category == "保暖层" })
    }

    @Test
    fun brandAndModelAreOptionalWhenAddingGear() {
        val inventory = GearInventory(emptyList())

        val updated = inventory.addBrandGear(
            category = "保暖层",
            brand = "",
            model = "",
            weightGrams = null
        )

        assertEquals("保暖层", updated.items.single().category)
        assertEquals(null, updated.items.single().brand)
        assertEquals(null, updated.items.single().model)
    }

    @Test
    fun removingOwnedGearStopsRecommendationMatch() {
        val inventory = GearInventory(TrailMateSampleData.gearItems)
        val updated = inventory.remove("shell-1")
            .applyTo(TrailMateSampleData.gearRecommendations.first { it.category == "雨衣" })

        assertEquals(GearStatus.MISSING, updated.status)
        assertEquals(null, updated.matchedGearItemId)
    }

    @Test
    fun markingOwnedGearUnavailableStopsRecommendationMatch() {
        val inventory = GearInventory(TrailMateSampleData.gearItems)
        val updated = inventory.setAvailability("headlamp-1", available = false)
            .applyTo(TrailMateSampleData.gearRecommendations.first { it.category == "头灯" })

        assertEquals(GearStatus.MISSING, updated.status)
        assertEquals(null, updated.matchedGearItemId)
    }

    @Test
    fun addingGearRequiresCategory() {
        assertThrows(IllegalArgumentException::class.java) {
            GearInventory(emptyList()).addBrandGear(
                category = " ",
                brand = "Rab",
                model = "Xenair Alpine Light",
                weightGrams = 309
            )
        }
    }

    @Test
    fun addingGearRejectsNegativeWeight() {
        assertThrows(IllegalArgumentException::class.java) {
            GearInventory(emptyList()).addBrandGear(
                category = "保暖层",
                brand = "Rab",
                model = "Xenair Alpine Light",
                weightGrams = -1
            )
        }
    }
}
