package com.trailmate.app.core.map

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapLibrePmTilesStyleFactoryTest {
    @Test
    fun buildsFileBackedPmtilesUriWithEncodedPath() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/杭州 西湖.pmtiles")

        val uri = MapLibrePmTilesStyleFactory.pmTilesUri(file)

        assertTrue(uri.startsWith("pmtiles://file://"))
        assertTrue(uri.contains("%E6%9D%AD%E5%B7%9E%20%E8%A5%BF%E6%B9%96.pmtiles"))
        assertFalse(uri.contains(" "))
    }

    @Test
    fun buildsMinimalStyleJsonWithPmtilesVectorSourceAndOsmAttribution() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")

        val styleJson = MapLibrePmTilesStyleFactory.buildStyleJson(file)

        assertTrue(styleJson.contains("\"version\":8"))
        assertTrue(styleJson.contains("\"trailmate-pmtiles\""))
        assertTrue(styleJson.contains("\"type\":\"vector\""))
        assertTrue(styleJson.contains("\"url\":\"pmtiles://file:///data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles\""))
        assertTrue(styleJson.contains("© OpenStreetMap contributors"))
        assertTrue(styleJson.contains("\"background\""))
        assertTrue(styleJson.contains("\"source-layer\":\"water\""))
        assertTrue(styleJson.contains("\"source-layer\":\"roads\""))
        assertTrue(styleJson.contains("\"source-layer\":\"landuse\""))
    }

    @Test
    fun defaultOfflineStyleDoesNotUseTextOrIconsWithoutBundledAssets() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")

        val styleJson = MapLibrePmTilesStyleFactory.buildStyleJson(file)

        assertFalse(styleJson.contains("\"text-field\""))
        assertFalse(styleJson.contains("\"icon-image\""))
        assertFalse(styleJson.contains("\"glyphs\""))
        assertFalse(styleJson.contains("\"sprite\""))
    }

    @Test
    fun incompleteAssetsKeepGeometryOnlyStyle() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")
        val styleJson = MapLibrePmTilesStyleFactory.buildStyleJson(
            file = file,
            styleAssets = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
                MapLibrePmTilesStyleAssetManifest(
                    glyphsUrl = null,
                    spriteJsonUrl = "asset://trailmate/maplibre/protomaps/sprite.json",
                    spriteImageUrl = "asset://trailmate/maplibre/protomaps/sprite.png"
                )
            )
        )

        assertTrue(styleJson.contains("\"source-layer\":\"water\""))
        assertTrue(styleJson.contains("\"source-layer\":\"roads\""))
        assertFalse(styleJson.contains("\"text-field\""))
        assertFalse(styleJson.contains("\"icon-image\""))
        assertFalse(styleJson.contains("\"glyphs\""))
        assertFalse(styleJson.contains("\"sprite\""))
    }

    @Test
    fun completeAssetsBuildLabeledOfflineStyle() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")
        val styleJson = MapLibrePmTilesStyleFactory.buildStyleJson(
            file = file,
            styleAssets = MapLibrePmTilesStyleAssetReadinessEngine.resolve(completeManifest())
        )

        assertTrue(
            styleJson.contains(
                "\"glyphs\":\"asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf\""
            )
        )
        assertTrue(styleJson.contains("\"sprite\":\"asset://trailmate/maplibre/protomaps/sprite\""))
        assertTrue(styleJson.contains("\"text-field\""))
        assertTrue(styleJson.contains("\"text-font\":[\"Noto Sans Regular\"]"))
        assertTrue(styleJson.contains("\"icon-image\""))
        assertFalse(styleJson.contains("https://"))
        assertFalse(styleJson.contains("http://"))
    }

    @Test
    fun networkAssetsKeepGeometryOnlyStyle() {
        val file = File("/data/user/0/com.trailmate.app/files/pmtiles-basemaps/longjing.pmtiles")
        val styleJson = MapLibrePmTilesStyleFactory.buildStyleJson(
            file = file,
            styleAssets = MapLibrePmTilesStyleAssetReadinessEngine.resolve(
                MapLibrePmTilesStyleAssetManifest(
                    glyphsUrl = "https://cdn.example.com/glyphs/{fontstack}/{range}.pbf",
                    spriteJsonUrl = "https://cdn.example.com/sprite.json",
                    spriteImageUrl = "https://cdn.example.com/sprite.png"
                )
            )
        )

        assertFalse(styleJson.contains("\"glyphs\""))
        assertFalse(styleJson.contains("\"sprite\""))
        assertFalse(styleJson.contains("\"text-field\""))
        assertFalse(styleJson.contains("\"icon-image\""))
        assertFalse(styleJson.contains("https://"))
        assertFalse(styleJson.contains("http://"))
    }

    private fun completeManifest(): MapLibrePmTilesStyleAssetManifest =
        MapLibrePmTilesStyleAssetManifest(
            glyphsUrl = "asset://trailmate/maplibre/protomaps/glyphs/{fontstack}/{range}.pbf",
            spriteJsonUrl = "asset://trailmate/maplibre/protomaps/sprite.json",
            spriteImageUrl = "asset://trailmate/maplibre/protomaps/sprite.png"
        )
}
