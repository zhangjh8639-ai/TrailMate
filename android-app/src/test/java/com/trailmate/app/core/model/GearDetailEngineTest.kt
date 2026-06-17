package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GearDetailEngineTest {
    @Test
    fun summarizesBrandModelWeightAvailabilityAndRouteMatch() {
        val item = GearItem(
            id = "shell-1",
            category = "Rain shell",
            brand = "Patagonia",
            model = "Torrentshell",
            weightGrams = 400,
            available = true
        )
        val recommendations = listOf(
            GearRecommendation(
                category = "Rain shell",
                status = GearStatus.COVERED,
                rationale = "Existing shell covers wind and light rain.",
                matchedGearItemId = "shell-1"
            )
        )

        val summary = GearDetailEngine.summarize(item, recommendations)

        assertEquals("Patagonia Torrentshell", summary.title)
        assertEquals("Rain shell", summary.category)
        assertEquals("400g / ready", summary.statusLine)
        assertEquals("Matches Rain shell recommendation.", summary.routeMatchLine)
        assertEquals("Existing shell covers wind and light rain.", summary.routeRationale)
    }

    @Test
    fun summarizesUnknownWeightAndNoCurrentRouteMatch() {
        val item = GearItem(
            id = "cup-1",
            category = "Camp cup",
            brand = null,
            model = null,
            weightGrams = null,
            available = false
        )

        val summary = GearDetailEngine.summarize(item, emptyList())

        assertEquals("Camp cup", summary.title)
        assertEquals("weight TBD / not packed", summary.statusLine)
        assertEquals("No current route match.", summary.routeMatchLine)
        assertEquals("Add or import a route recommendation to see why this item matters.", summary.routeRationale)
    }
}
