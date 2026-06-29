package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapLibrePmTilesBundledStyleAssetManifestResolverTest {
    @Test
    fun completeBundledAssetsReturnLocalStyleManifest() {
        val availableAssets = setOf(
            MapLibrePmTilesBundledStyleAssetManifestResolver.GLYPH_PROBE_ASSET_PATH,
            MapLibrePmTilesBundledStyleAssetManifestResolver.SPRITE_JSON_ASSET_PATH,
            MapLibrePmTilesBundledStyleAssetManifestResolver.SPRITE_IMAGE_ASSET_PATH
        )

        val manifest = MapLibrePmTilesBundledStyleAssetManifestResolver.resolve(
            assetExists = availableAssets::contains
        )

        assertEquals(
            "asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf",
            manifest.glyphsUrl
        )
        assertEquals(
            "asset://trailmate/maplibre/protomaps/sprite.json",
            manifest.spriteJsonUrl
        )
        assertEquals(
            "asset://trailmate/maplibre/protomaps/sprite.png",
            manifest.spriteImageUrl
        )
    }

    @Test
    fun missingAnyBundledAssetReturnsUnavailableManifest() {
        val onlyGlyphs = setOf(MapLibrePmTilesBundledStyleAssetManifestResolver.GLYPH_PROBE_ASSET_PATH)

        val manifest = MapLibrePmTilesBundledStyleAssetManifestResolver.resolve(
            assetExists = onlyGlyphs::contains
        )

        assertNull(manifest.glyphsUrl)
        assertNull(manifest.spriteJsonUrl)
        assertNull(manifest.spriteImageUrl)
    }

    @Test
    fun bundledGlyphProbeMatchesStyleFontStack() {
        assertEquals(
            "trailmate/maplibre/protomaps/glyphs/Noto Sans Regular/0-255.pbf",
            MapLibrePmTilesBundledStyleAssetManifestResolver.GLYPH_PROBE_ASSET_PATH
        )
        assertEquals(
            "Noto Sans Regular",
            MapLibrePmTilesBundledStyleAssetManifestResolver.LABEL_FONT_STACK
        )
    }
}
