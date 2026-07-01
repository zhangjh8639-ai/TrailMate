package com.trailmate.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TrailMateTabsTest {
    @Test
    fun bottomTabsMatchCurrentInformationArchitecture() {
        val labels = TrailMateTab.entries.map { it.label }

        assertEquals(listOf("发现", "路线", "导航", "记录", "我的"), labels)
        listOf("规划", "装备", "社区", "商城").forEach { legacyEntry ->
            assertFalse(labels.contains(legacyEntry))
        }
    }
}
