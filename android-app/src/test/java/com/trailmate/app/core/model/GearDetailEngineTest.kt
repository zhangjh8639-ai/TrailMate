package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GearDetailEngineTest {
    @Test
    fun summarizesBrandModelWeightAvailabilityAndRouteMatch() {
        val item = GearItem(
            id = "shell-1",
            category = "雨衣",
            brand = "Patagonia",
            model = "Torrentshell",
            weightGrams = 400,
            available = true
        )
        val recommendations = listOf(
            GearRecommendation(
                category = "雨衣",
                status = GearStatus.COVERED,
                rationale = "现有雨衣可以覆盖山脊风和小雨。",
                matchedGearItemId = "shell-1"
            )
        )

        val summary = GearDetailEngine.summarize(item, recommendations)

        assertEquals("Patagonia Torrentshell", summary.title)
        assertEquals("雨衣", summary.category)
        assertEquals("400g / 可用", summary.statusLine)
        assertEquals("匹配雨衣建议。", summary.routeMatchLine)
        assertEquals("现有雨衣可以覆盖山脊风和小雨。", summary.routeRationale)
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
        assertEquals("重量待填 / 未打包", summary.statusLine)
        assertEquals("当前路线暂无匹配。", summary.routeMatchLine)
        assertEquals("导入路线并生成装备建议后，可查看这件装备为什么重要。", summary.routeRationale)
    }
}
