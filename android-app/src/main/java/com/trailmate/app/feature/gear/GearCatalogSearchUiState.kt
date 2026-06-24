package com.trailmate.app.feature.gear

import com.trailmate.app.core.model.GearCatalogItem

data class GearCatalogSearchUiState(
    val category: String,
    val query: String,
    val isLoading: Boolean,
    val results: List<GearCatalogItem>,
    val errorMessage: String?,
    val requestInputFocus: Boolean
) {
    fun withQuery(nextQuery: String): GearCatalogSearchUiState =
        copy(query = nextQuery, requestInputFocus = nextQuery.isNotBlank())

    fun loading(): GearCatalogSearchUiState =
        copy(isLoading = true, errorMessage = null)

    fun success(items: List<GearCatalogItem>): GearCatalogSearchUiState =
        copy(isLoading = false, results = items, errorMessage = null)

    fun failure(message: String): GearCatalogSearchUiState =
        copy(isLoading = false, errorMessage = message)

    companion object {
        fun initial(category: String): GearCatalogSearchUiState =
            GearCatalogSearchUiState(
                category = category,
                query = "",
                isLoading = false,
                results = emptyList(),
                errorMessage = null,
                requestInputFocus = false
            )
    }
}
