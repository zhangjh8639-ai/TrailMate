package com.trailmate.app.core.map

enum class MapLibrePmTilesStyleAssetStatus {
    READY,
    MISSING_GLYPHS,
    MISSING_SPRITE_JSON,
    MISSING_SPRITE_IMAGE,
    MISMATCHED_SPRITE_ASSETS,
    NETWORK_ASSET_URL
}

data class MapLibrePmTilesStyleAssetManifest(
    val glyphsUrl: String?,
    val spriteJsonUrl: String?,
    val spriteImageUrl: String?
)

data class MapLibrePmTilesStyleAssetReadiness(
    val status: MapLibrePmTilesStyleAssetStatus,
    val readyForLabels: Boolean,
    val title: String,
    val caption: String,
    val glyphsUrl: String? = null,
    val spriteUrl: String? = null
)

object MapLibrePmTilesStyleAssetReadinessEngine {
    private const val MISSING_TITLE = "离线地图标注资源待补齐"
    private const val MISSING_CAPTION = "标注资源缺失时将暂不显示文字或图标标注，路线几何和底图上下文仍可查看。"
    private const val READY_TITLE = "离线地图标注资源已就绪"
    private const val READY_CAPTION = "字形与精灵资源已连接，离线地图标注可随路线几何一起显示。"

    fun resolve(manifest: MapLibrePmTilesStyleAssetManifest): MapLibrePmTilesStyleAssetReadiness {
        if (manifest.glyphsUrl.isNullOrBlank()) {
            return notReady(MapLibrePmTilesStyleAssetStatus.MISSING_GLYPHS)
        }
        if (manifest.spriteJsonUrl.isNullOrBlank()) {
            return notReady(MapLibrePmTilesStyleAssetStatus.MISSING_SPRITE_JSON)
        }
        if (manifest.spriteImageUrl.isNullOrBlank()) {
            return notReady(MapLibrePmTilesStyleAssetStatus.MISSING_SPRITE_IMAGE)
        }
        if (!manifest.glyphsUrl.isOfflineAssetUrl() ||
            !manifest.spriteJsonUrl.isOfflineAssetUrl() ||
            !manifest.spriteImageUrl.isOfflineAssetUrl()
        ) {
            return notReady(MapLibrePmTilesStyleAssetStatus.NETWORK_ASSET_URL)
        }
        val spriteBaseUrl = manifest.spriteJsonUrl.toSpriteBaseUrl()
        if (spriteBaseUrl != manifest.spriteImageUrl.toSpriteBaseUrl()) {
            return notReady(MapLibrePmTilesStyleAssetStatus.MISMATCHED_SPRITE_ASSETS)
        }

        return MapLibrePmTilesStyleAssetReadiness(
            status = MapLibrePmTilesStyleAssetStatus.READY,
            readyForLabels = true,
            title = READY_TITLE,
            caption = READY_CAPTION,
            glyphsUrl = manifest.glyphsUrl,
            spriteUrl = spriteBaseUrl
        )
    }

    private fun notReady(status: MapLibrePmTilesStyleAssetStatus): MapLibrePmTilesStyleAssetReadiness =
        MapLibrePmTilesStyleAssetReadiness(
            status = status,
            readyForLabels = false,
            title = MISSING_TITLE,
            caption = MISSING_CAPTION
        )

    private fun String.toSpriteBaseUrl(): String =
        removeSuffix(".json")
            .removeSuffix(".png")
            .removeSuffix("@2x")

    private fun String.isOfflineAssetUrl(): Boolean =
        trim().startsWith("asset://")
}
