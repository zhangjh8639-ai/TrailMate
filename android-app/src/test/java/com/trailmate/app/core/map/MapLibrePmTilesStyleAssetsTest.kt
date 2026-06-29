package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapLibrePmTilesStyleAssetsTest {
    @Test
    fun missingGlyphsKeepsRouteGeometryAvailableWithoutLabels() {
        val readiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = null,
                spriteJsonUrl = "asset://sprites/trailmate.json",
                spriteImageUrl = "asset://sprites/trailmate.png"
            )
        )

        assertEquals(MapLibrePmTilesStyleAssetStatus.MISSING_GLYPHS, readiness.status)
        assertFalse(readiness.readyForLabels)
        assertEquals("离线地图标注资源待补齐", readiness.title)
        assertLabelsMissingCaption(readiness.caption)
        assertNull(readiness.glyphsUrl)
        assertNull(readiness.spriteUrl)
    }

    @Test
    fun missingSpriteJsonKeepsRouteGeometryAvailableWithoutLabels() {
        val readiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = null,
                spriteImageUrl = "asset://sprites/trailmate.png"
            )
        )

        assertEquals(MapLibrePmTilesStyleAssetStatus.MISSING_SPRITE_JSON, readiness.status)
        assertFalse(readiness.readyForLabels)
        assertEquals("离线地图标注资源待补齐", readiness.title)
        assertLabelsMissingCaption(readiness.caption)
        assertNull(readiness.glyphsUrl)
        assertNull(readiness.spriteUrl)
    }

    @Test
    fun missingSpriteImageKeepsRouteGeometryAvailableWithoutLabels() {
        val readiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = "asset://sprites/trailmate.json",
                spriteImageUrl = null
            )
        )

        assertEquals(MapLibrePmTilesStyleAssetStatus.MISSING_SPRITE_IMAGE, readiness.status)
        assertFalse(readiness.readyForLabels)
        assertEquals("离线地图标注资源待补齐", readiness.title)
        assertLabelsMissingCaption(readiness.caption)
        assertNull(readiness.glyphsUrl)
        assertNull(readiness.spriteUrl)
    }

    @Test
    fun blankStringsAreTreatedAsMissingInReadinessOrder() {
        val missingGlyphs = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = " ",
                spriteJsonUrl = "asset://sprites/trailmate.json",
                spriteImageUrl = "asset://sprites/trailmate.png"
            )
        )
        val missingSpriteJson = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = "\t",
                spriteImageUrl = "asset://sprites/trailmate.png"
            )
        )
        val missingSpriteImage = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = "asset://sprites/trailmate.json",
                spriteImageUrl = ""
            )
        )

        assertEquals(MapLibrePmTilesStyleAssetStatus.MISSING_GLYPHS, missingGlyphs.status)
        assertEquals(MapLibrePmTilesStyleAssetStatus.MISSING_SPRITE_JSON, missingSpriteJson.status)
        assertEquals(MapLibrePmTilesStyleAssetStatus.MISSING_SPRITE_IMAGE, missingSpriteImage.status)
        assertFalse(missingGlyphs.readyForLabels)
        assertFalse(missingSpriteJson.readyForLabels)
        assertFalse(missingSpriteImage.readyForLabels)
    }

    @Test
    fun completeAssetsAreReadyForLabelsAndExposeStyleAssetUrls() {
        val readiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = "asset://sprites/trailmate.json",
                spriteImageUrl = "asset://sprites/trailmate.png"
            )
        )

        assertEquals(MapLibrePmTilesStyleAssetStatus.READY, readiness.status)
        assertTrue(readiness.readyForLabels)
        assertEquals("离线地图标注资源已就绪", readiness.title)
        assertTrue(readiness.caption.contains("标注"))
        assertEquals("asset://fonts/{fontstack}/{range}.pbf", readiness.glyphsUrl)
        assertEquals("asset://sprites/trailmate", readiness.spriteUrl)

        val readinessWithoutJsonSuffix = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = "asset://sprites/trailmate",
                spriteImageUrl = "asset://sprites/trailmate@2x.png"
            )
        )

        assertEquals("asset://sprites/trailmate", readinessWithoutJsonSuffix.spriteUrl)
    }

    @Test
    fun mismatchedSpriteJsonAndImageDoNotEnableLabels() {
        val readiness = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = "asset://sprites/trailmate.json",
                spriteImageUrl = "asset://sprites/other.png"
            )
        )

        assertEquals(MapLibrePmTilesStyleAssetStatus.MISMATCHED_SPRITE_ASSETS, readiness.status)
        assertFalse(readiness.readyForLabels)
        assertEquals("离线地图标注资源待补齐", readiness.title)
        assertLabelsMissingCaption(readiness.caption)
        assertNull(readiness.glyphsUrl)
        assertNull(readiness.spriteUrl)
    }

    @Test
    fun networkAssetUrlsDoNotEnableOfflineLabels() {
        val networkGlyphs = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "https://cdn.example.com/glyphs/{fontstack}/{range}.pbf",
                spriteJsonUrl = "asset://sprites/trailmate.json",
                spriteImageUrl = "asset://sprites/trailmate.png"
            )
        )
        val networkSprite = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
            MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://fonts/{fontstack}/{range}.pbf",
                spriteJsonUrl = "https://cdn.example.com/sprite.json",
                spriteImageUrl = "https://cdn.example.com/sprite.png"
            )
        )

        assertEquals(MapLibrePmTilesStyleAssetStatus.NETWORK_ASSET_URL, networkGlyphs.status)
        assertEquals(MapLibrePmTilesStyleAssetStatus.NETWORK_ASSET_URL, networkSprite.status)
        assertFalse(networkGlyphs.readyForLabels)
        assertFalse(networkSprite.readyForLabels)
        assertNull(networkGlyphs.glyphsUrl)
        assertNull(networkSprite.spriteUrl)
    }

    private fun assertLabelsMissingCaption(caption: String) {
        assertTrue(caption.contains("路线几何"))
        assertTrue(caption.contains("仍可查看"))
        assertTrue(caption.contains("标注"))
        assertFalse(caption.contains("安全"))
    }
}
