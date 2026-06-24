package com.trailmate.app.feature.gear

data class GearCatalogSourceUiState(
    val label: String,
    val caption: String,
    val isLoading: Boolean,
    val canRetry: Boolean
) {
    companion object {
        fun localPreview(): GearCatalogSourceUiState =
            GearCatalogSourceUiState(
                label = "品牌库缓存",
                caption = "当前使用内置品牌库预览；接入服务端后会同步品牌、型号和缩略图。",
                isLoading = false,
                canRetry = false
            )

        fun loading(): GearCatalogSourceUiState =
            GearCatalogSourceUiState(
                label = "同步品牌库",
                caption = "正在从服务端同步品牌、型号和缩略图。",
                isLoading = true,
                canRetry = false
            )

        fun serverSynced(itemCount: Int): GearCatalogSourceUiState =
            GearCatalogSourceUiState(
                label = "服务端品牌库",
                caption = "已同步 ${itemCount} 件品牌装备，品牌、型号和缩略图由服务端维护。",
                isLoading = false,
                canRetry = false
            )

        fun fallbackCache(): GearCatalogSourceUiState =
            GearCatalogSourceUiState(
                label = "品牌库缓存",
                caption = "服务端品牌库暂时不可用，已使用本地缓存；稍后可重试同步。",
                isLoading = false,
                canRetry = true
            )
    }
}
