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
        val weight = item.weightGrams?.let { "${it}g" } ?: "重量待填"
        val availability = if (item.available) "可用" else "未打包"
        val routeMatch = routeGearRecommendations.firstOrNull { recommendation ->
            recommendation.matchedGearItemId == item.id
        }

        return GearDetailSummary(
            title = title,
            category = item.category,
            statusLine = "$weight / $availability",
            routeMatchLine = routeMatch?.let { recommendation ->
                "匹配${recommendation.category}建议。"
            } ?: "当前路线暂无匹配。",
            routeRationale = routeMatch?.rationale
                ?: "导入路线并生成装备建议后，可查看这件装备为什么重要。"
        )
    }
}
