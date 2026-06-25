package com.trailmate.app.core.model

object GearCatalogSelectionEngine {
    fun matchCatalogItems(
        catalogItems: List<GearCatalogItem>,
        routeCategory: String,
        query: String
    ): List<GearCatalogItem> {
        val normalizedQuery = query.trim().lowercase()
        return catalogItems
            .filter { item -> routeCategory.isBlank() || categoriesMatch(item.category, routeCategory) }
            .filter { item -> normalizedQuery.isBlank() || item.searchableText().contains(normalizedQuery) }
            .sortedBy { item -> item.displayName }
    }

    fun resolveRouteMatchesForDeparture(
        recommendations: List<GearRecommendation>,
        catalogItems: List<GearCatalogItem>
    ): List<GearRecommendation> =
        recommendations.map { recommendation ->
            val matchedCatalogItem = matchCatalogItems(
                catalogItems = catalogItems,
                routeCategory = recommendation.category,
                query = ""
            ).firstOrNull()

            when {
                matchedCatalogItem == null -> recommendation
                recommendation.status == GearStatus.MISSING -> recommendation.copy(
                    status = GearStatus.COVERED,
                    matchedGearItemId = matchedCatalogItem.catalogItemId
                )
                recommendation.matchedGearItemId == null -> recommendation.copy(
                    matchedGearItemId = matchedCatalogItem.catalogItemId
                )
                else -> recommendation
            }
        }

    fun categoriesMatch(catalogCategory: String, routeCategory: String): Boolean =
        normalizeCategory(catalogCategory) == normalizeCategory(routeCategory)

    fun presentRouteMatch(
        recommendation: GearRecommendation,
        matchedCatalogItem: GearCatalogItem?
    ): GearCatalogRouteMatchPresentation {
        val needsMatchedItemCheck = matchedCatalogItem != null &&
            recommendation.status == GearStatus.CHECK &&
            recommendation.category.contains("头灯")
        return when {
            needsMatchedItemCheck -> GearCatalogRouteMatchPresentation(
                statusLine = "电量建议 ≥ 80%",
                tone = GearCatalogRouteMatchTone.CHECK
            )
            matchedCatalogItem != null -> GearCatalogRouteMatchPresentation(
                statusLine = "已匹配 ${matchedCatalogItem.brand} ${matchedCatalogItem.model}",
                tone = GearCatalogRouteMatchTone.MATCHED
            )
            recommendation.status == GearStatus.CHECK -> GearCatalogRouteMatchPresentation(
                statusLine = "电量建议 ≥ 80%",
                tone = GearCatalogRouteMatchTone.CHECK
            )
            recommendation.status == GearStatus.MISSING -> GearCatalogRouteMatchPresentation(
                statusLine = "服务端暂无匹配",
                tone = GearCatalogRouteMatchTone.MISSING
            )
            else -> GearCatalogRouteMatchPresentation(
                statusLine = recommendation.rationale,
                tone = GearCatalogRouteMatchTone.NEUTRAL
            )
        }
    }

    private fun GearCatalogItem.searchableText(): String =
        listOf(category, brand, model, displayName, tags.joinToString(" "))
            .joinToString(" ")
            .lowercase()

    private fun normalizeCategory(category: String): String {
        val compact = category
            .trim()
            .replace("（", "(")
            .replace("）", ")")
            .lowercase()
        return when {
            compact.contains("雨衣") || compact.contains("防水透气") || compact.contains("硬壳") -> "雨衣"
            compact.contains("保暖层") || compact.contains("保温层") || compact.contains("抓绒") || compact.contains("羽绒") -> "保温层"
            compact.contains("备用水") || compact.contains("水袋") || compact.contains("补水") -> "备用水"
            compact.contains("登山杖") -> "登山杖"
            compact.contains("头灯") -> "头灯"
            compact.contains("徒步鞋") -> "徒步鞋"
            compact.contains("急救包") || compact.contains("急救") -> "急救包"
            compact.contains("移动电源") || compact.contains("充电宝") -> "移动电源"
            compact.contains("导航设备") || compact.contains("备用导航") -> "导航设备"
            compact.contains("背包") -> "背包"
            else -> compact
        }
    }
}

object GearCatalogThumbnailPolicy {
    fun shouldLoadServerThumbnail(item: GearCatalogItem): Boolean =
        item.imageUrl?.startsWith("http", ignoreCase = true) == true
}

data class GearCatalogRouteMatchPresentation(
    val statusLine: String,
    val tone: GearCatalogRouteMatchTone
)

enum class GearCatalogRouteMatchTone {
    MATCHED,
    CHECK,
    MISSING,
    NEUTRAL
}
