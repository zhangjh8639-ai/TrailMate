package com.trailmate.app.core.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.trailmate.app.core.map.PmTilesLatLngBounds
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

class TrailMateHttpOfflineBasemapCatalogApiClientTest {
    private lateinit var server: HttpServer
    private lateinit var client: TrailMateHttpOfflineBasemapCatalogApiClient
    private val requests = mutableListOf<CapturedRequest>()

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
        client = TrailMateHttpOfflineBasemapCatalogApiClient(
            baseUrl = "http://127.0.0.1:${server.address.port}"
        )
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun listPmTilesBasemapsSendsBoundsAndParsesCatalogItems() {
        server.respondJson(
            path = "/api/v1/offline-basemaps/pmtiles/catalog",
            status = 200,
            body = """
                [
                  {
                    "packId": "pmtiles_hangzhou_westlake_osm_v1",
                    "regionName": "杭州市 · 西湖区",
                    "downloadUrl": "/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles",
                    "sizeBytes": 120000000,
                    "sha256": "abc123",
                    "tileType": "MVT",
                    "minZoom": 10,
                    "maxZoom": 14,
                    "minLongitude": 120.0,
                    "minLatitude": 30.05,
                    "maxLongitude": 120.3,
                    "maxLatitude": 30.4,
                    "attribution": "OpenStreetMap contributors",
                    "source": "OSM / Protomaps"
                  }
                ]
            """.trimIndent()
        )

        val result = client.listPmTilesBasemaps(
            PmTilesLatLngBounds(
                minLongitude = 120.05,
                minLatitude = 30.10,
                maxLongitude = 120.25,
                maxLatitude = 30.35
            )
        )

        val items = (result as TrailMateApiResult.Success<List<TrailMatePmTilesBasemapCatalogItemDto>>).value
        assertEquals("pmtiles_hangzhou_westlake_osm_v1", items.single().packId)
        assertEquals("杭州市 · 西湖区", items.single().regionName)
        assertEquals(
            "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles",
            items.single().downloadUrl
        )
        assertEquals("OSM / Protomaps", items.single().source)
        assertEquals("GET", requests.single().method)
        assertEquals("/api/v1/offline-basemaps/pmtiles/catalog", requests.single().path)
        assertTrue(requests.single().rawQuery.contains("minLongitude=120.05"))
        assertTrue(requests.single().rawQuery.contains("minLatitude=30.1"))
        assertTrue(requests.single().rawQuery.contains("maxLongitude=120.25"))
        assertTrue(requests.single().rawQuery.contains("maxLatitude=30.35"))
    }

    @Test
    fun listPmTilesBasemapsIgnoresUnknownNestedFieldsAndDecodesEscapedStrings() {
        server.respondJson(
            path = "/api/v1/offline-basemaps/pmtiles/catalog",
            status = 200,
            body = """
                [
                  {
                    "packId": "pmtiles_hangzhou_osm_v2",
                    "regionName": "杭州市\n户外底图",
                    "downloadUrl": "/offline-basemaps/pmtiles/hangzhou.pmtiles",
                    "sizeBytes": 220000000,
                    "sha256": "def456",
                    "tileType": "MVT",
                    "minZoom": 9,
                    "maxZoom": 15,
                    "minLongitude": 119.8,
                    "minLatitude": 29.9,
                    "maxLongitude": 120.5,
                    "maxLatitude": 30.6,
                    "attribution": "OpenStreetMap contributors\nProtomaps",
                    "source": "OSM / Protomaps",
                    "metadata": {
                      "generation": "2026-06",
                      "layers": ["roads", "water", "contours"]
                    }
                  }
                ]
            """.trimIndent()
        )

        val result = client.listPmTilesBasemaps(
            PmTilesLatLngBounds(
                minLongitude = 119.90,
                minLatitude = 30.00,
                maxLongitude = 120.40,
                maxLatitude = 30.50
            )
        )

        val item = (result as TrailMateApiResult.Success<List<TrailMatePmTilesBasemapCatalogItemDto>>).value.single()
        assertEquals("pmtiles_hangzhou_osm_v2", item.packId)
        assertEquals("杭州市\n户外底图", item.regionName)
        assertEquals("OpenStreetMap contributors\nProtomaps", item.attribution)
        assertEquals("http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/hangzhou.pmtiles", item.downloadUrl)
    }

    private fun HttpServer.respondJson(
        path: String,
        status: Int,
        body: String
    ) {
        createContext(path) { exchange ->
            requests += CapturedRequest(
                method = exchange.requestMethod,
                path = exchange.requestURI.path,
                rawQuery = exchange.requestURI.rawQuery.orEmpty()
            )
            exchange.sendJson(status, body)
        }
    }

    private fun HttpExchange.sendJson(status: Int, body: String) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        responseHeaders.add("Content-Type", "application/json; charset=utf-8")
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.use { output -> output.write(bytes) }
    }

    private data class CapturedRequest(
        val method: String,
        val path: String,
        val rawQuery: String
    )
}
