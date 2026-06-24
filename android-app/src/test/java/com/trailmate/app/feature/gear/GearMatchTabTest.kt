package com.trailmate.app.feature.gear

import org.junit.Assert.assertEquals
import org.junit.Test

class GearMatchTabTest {
    @Test
    fun labelsDescribeServerCatalogRouteFlow() {
        assertEquals(
            listOf("路线需求", "品牌候选", "装备详情"),
            GearMatchTab.entries.map { it.label }
        )
    }
}
