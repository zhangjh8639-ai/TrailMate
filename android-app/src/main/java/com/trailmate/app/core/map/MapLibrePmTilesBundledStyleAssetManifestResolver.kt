package com.trailmate.app.core.map

object MapLibrePmTilesBundledStyleAssetManifestResolver {
    const val LABEL_FONT_STACK = "Noto Sans Regular"
    const val GLYPH_PROBE_ASSET_PATH = "trailmate/maplibre/protomaps/glyphs/$LABEL_FONT_STACK/0-255.pbf"
    const val SPRITE_JSON_ASSET_PATH = "trailmate/maplibre/protomaps/sprite.json"
    const val SPRITE_IMAGE_ASSET_PATH = "trailmate/maplibre/protomaps/sprite.png"

    private const val GLYPHS_URL = "asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf"
    private const val SPRITE_JSON_URL = "asset://trailmate/maplibre/protomaps/sprite.json"
    private const val SPRITE_IMAGE_URL = "asset://trailmate/maplibre/protomaps/sprite.png"

    fun resolve(assetExists: (String) -> Boolean): MapLibrePmTilesStyleAssetManifest {
        val complete = assetExists(GLYPH_PROBE_ASSET_PATH) &&
            assetExists(SPRITE_JSON_ASSET_PATH) &&
            assetExists(SPRITE_IMAGE_ASSET_PATH)
        if (!complete) {
            return MapLibrePmTilesStyleAssetManifest.unavailable()
        }

        return MapLibrePmTilesStyleAssetManifest(
            glyphsUrl = GLYPHS_URL,
            spriteJsonUrl = SPRITE_JSON_URL,
            spriteImageUrl = SPRITE_IMAGE_URL
        )
    }
}
