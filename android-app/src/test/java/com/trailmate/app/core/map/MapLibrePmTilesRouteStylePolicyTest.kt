package com.trailmate.app.core.map

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapLibrePmTilesRouteStylePolicyTest {
    @Test
    fun defaultRuntimeStyleUsesGeometryOnlyWhenAssetsAreUnavailable() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")

        val styleJson = MapLibrePmTilesRouteStylePolicy.buildStyleJson(file)

        assertTrue(styleJson.contains("\"source-layer\":\"water\""))
        assertTrue(styleJson.contains("\"source-layer\":\"roads\""))
        assertFalse(styleJson.contains("\"glyphs\""))
        assertFalse(styleJson.contains("\"sprite\""))
        assertFalse(styleJson.contains("\"text-field\""))
        assertFalse(styleJson.contains("\"icon-image\""))
    }

    @Test
    fun completeLocalRuntimeAssetsEnableLabeledOfflineStyle() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")

        val styleJson = MapLibrePmTilesRouteStylePolicy.buildStyleJson(
            pmTilesFile = file,
            styleAssetManifest = MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf",
                spriteJsonUrl = "asset://trailmate/maplibre/protomaps/sprite.json",
                spriteImageUrl = "asset://trailmate/maplibre/protomaps/sprite.png"
            )
        )

        assertTrue(
            styleJson.contains(
                "\"glyphs\":\"asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf\""
            )
        )
        assertTrue(styleJson.contains("\"sprite\":\"asset://trailmate/maplibre/protomaps/sprite\""))
        assertTrue(styleJson.contains("\"text-field\""))
        assertTrue(styleJson.contains("\"icon-image\""))
        assertFalse(styleJson.contains("https://"))
        assertFalse(styleJson.contains("http://"))
    }

    @Test
    fun networkRuntimeAssetsKeepGeometryOnlyStyle() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")

        val styleJson = MapLibrePmTilesRouteStylePolicy.buildStyleJson(
            pmTilesFile = file,
            styleAssetManifest = MapLibrePmTilesStyleAssetManifest(
                glyphsUrl = "https://cdn.example.com/glyphs/{fontstack}/{range}.pbf",
                spriteJsonUrl = "https://cdn.example.com/sprite.json",
                spriteImageUrl = "https://cdn.example.com/sprite.png"
            )
        )

        assertTrue(styleJson.contains("\"source-layer\":\"water\""))
        assertTrue(styleJson.contains("\"source-layer\":\"roads\""))
        assertFalse(styleJson.contains("\"glyphs\""))
        assertFalse(styleJson.contains("\"sprite\""))
        assertFalse(styleJson.contains("\"text-field\""))
        assertFalse(styleJson.contains("\"icon-image\""))
        assertFalse(styleJson.contains("https://"))
        assertFalse(styleJson.contains("http://"))
    }
}
