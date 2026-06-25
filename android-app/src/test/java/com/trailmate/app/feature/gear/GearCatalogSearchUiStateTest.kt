package com.trailmate.app.feature.gear

import com.trailmate.app.core.model.GearCatalogItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GearCatalogSearchUiStateTest {
    @Test
    fun initialStateUsesRequestedCategory() {
        val state = GearCatalogSearchUiState.initial("头灯")

        assertEquals("头灯", state.category)
        assertEquals("", state.query)
        assertFalse(state.isLoading)
        assertTrue(state.results.isEmpty())
        assertFalse(state.requestInputFocus)
    }

    @Test
    fun loadingStateKeepsSearchInput() {
        val state = GearCatalogSearchUiState.initial("雨衣（防水透气）")
            .withQuery("beta")
            .loading()

        assertEquals("beta", state.query)
        assertTrue(state.isLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun successStateStoresResults() {
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

        val state = GearCatalogSearchUiState.initial("雨衣（防水透气）")
            .success(listOf(item))

        assertFalse(state.isLoading)
        assertEquals(1, state.results.size)
        assertEquals("Arc'teryx Beta LT Jacket", state.results.first().displayName)
    }

    @Test
    fun failureStateShowsChineseMessage() {
        val state = GearCatalogSearchUiState.initial("头灯")
            .failure("品牌库暂时不可用，请稍后重试。")

        assertFalse(state.isLoading)
        assertEquals("品牌库暂时不可用，请稍后重试。", state.errorMessage)
    }

    @Test
    fun editingQueryMarksSearchAsUserInitiated() {
        val state = GearCatalogSearchUiState.initial("雨衣（防水透气）")
            .withQuery("beta")

        assertEquals("beta", state.query)
        assertTrue(state.requestInputFocus)
    }
}
