package com.trailmate.app.core.map

import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object MapLibrePmTilesStyleFactory {
    private const val SOURCE_ID = "trailmate-pmtiles"

    fun pmTilesUri(file: File): String =
        "pmtiles://file://${file.path.toUriPath()}"

    fun buildStyleJson(file: File): String {
        val sourceUrl = pmTilesUri(file).jsonEscaped()
        return """
            {
              "version":8,
              "name":"TrailMate PMTiles Offline",
              "sources":{
                "$SOURCE_ID":{
                  "type":"vector",
                  "url":"$sourceUrl",
                  "attribution":"© OpenStreetMap contributors"
                }
              },
              "layers":[
                {
                  "id":"background",
                  "type":"background",
                  "paint":{"background-color":"#f5f3ee"}
                },
                {
                  "id":"water",
                  "type":"fill",
                  "source":"$SOURCE_ID",
                  "source-layer":"water",
                  "paint":{"fill-color":"#b8d7dd"}
                },
                {
                  "id":"landuse",
                  "type":"fill",
                  "source":"$SOURCE_ID",
                  "source-layer":"landuse",
                  "paint":{"fill-color":"#dce8d3","fill-opacity":0.55}
                },
                {
                  "id":"roads",
                  "type":"line",
                  "source":"$SOURCE_ID",
                  "source-layer":"roads",
                  "paint":{"line-color":"#ffffff","line-width":1.2}
                }
              ]
            }
        """.trimIndent().replace("\n", "")
    }

    private fun String.toUriPath(): String =
        replace("\\", "/")
            .split("/")
            .joinToString("/") { segment ->
                if (segment.isEmpty()) {
                    ""
                } else {
                    URLEncoder.encode(segment, StandardCharsets.UTF_8.name())
                        .replace("+", "%20")
                }
            }

    private fun String.jsonEscaped(): String =
        replace("\\", "\\\\").replace("\"", "\\\"")
}
