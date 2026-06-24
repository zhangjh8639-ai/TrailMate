package com.trailmate.app.core.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

class TrailMateHttpGearCatalogApiClientTest {
    private lateinit var server: HttpServer
    private lateinit var client: TrailMateHttpGearCatalogApiClient
    private val requests = mutableListOf<CapturedRequest>()

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
        client = TrailMateHttpGearCatalogApiClient(
            baseUrl = "http://127.0.0.1:${server.address.port}"
        )
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun listCategoriesCallsServerCatalogEndpoint() {
        server.respondJson(
            path = "/api/v1/gear/catalog/categories",
            status = 200,
            body = """["头灯","登山杖"]"""
        )

        val result = client.listGearCatalogCategories()

        assertEquals(TrailMateApiResult.Success(listOf("头灯", "登山杖")), result)
        assertEquals("GET", requests.single().method)
        assertEquals("/api/v1/gear/catalog/categories", requests.single().path)
    }

    @Test
    fun searchCatalogSendsCategoryAndQueryThenParsesItems() {
        server.respondJson(
            path = "/api/v1/gear/catalog/search",
            status = 200,
            body = """
                [
                  {
                    "catalogItemId": "cat_rain_arcteryx_beta_lt",
                    "category": "雨衣（防水透气）",
                    "brand": "Arc'teryx",
                    "model": "Beta LT Jacket",
                    "displayName": "Arc'teryx Beta LT Jacket",
                    "weightGrams": 395,
                    "tags": ["防水", "硬壳"],
                    "imageUrl": "/gear-thumbnails/arcteryx-beta-lt.png",
                    "imageAttribution": "TrailMate hosted catalog thumbnail",
                    "source": "seed"
                  }
                ]
            """.trimIndent()
        )

        val result = client.searchGearCatalog("雨衣（防水透气）", "beta")

        val items = (result as TrailMateApiResult.Success<List<TrailMateGearCatalogItemDto>>).value
        assertEquals("cat_rain_arcteryx_beta_lt", items.single().catalogItemId)
        assertEquals("Arc'teryx Beta LT Jacket", items.single().displayName)
        assertEquals("http://127.0.0.1:${server.address.port}/gear-thumbnails/arcteryx-beta-lt.png", items.single().imageUrl)
        assertEquals("TrailMate hosted catalog thumbnail", items.single().imageAttribution)
        assertEquals(listOf("防水", "硬壳"), items.single().tags)
        assertTrue(requests.single().rawQuery.contains("category=%E9%9B%A8%E8%A1%A3"))
        assertTrue(requests.single().rawQuery.contains("q=beta"))
    }

    @Test
    fun searchCatalogParsesCompactMultiItemResponse() {
        server.respondJson(
            path = "/api/v1/gear/catalog/search",
            status = 200,
            body = """[{"catalogItemId":"cat_poles_leki_legacy_lite","category":"登山杖","brand":"Leki","model":"Legacy Lite AS","displayName":"Leki Legacy Lite AS","weightGrams":510,"tags":["长距离","下坡","稳定"],"imageUrl":"https://cdn.trailmate.local/gear/leki-legacy-lite-as.png","imageAttribution":"TrailMate catalog seed image","source":"seed"},{"catalogItemId":"cat_headlamp_bd_spot_400","category":"头灯","brand":"Black Diamond","model":"Spot 400","displayName":"Black Diamond Spot 400","weightGrams":78,"tags":["夜间","备用电池","安全"],"imageUrl":"https://cdn.trailmate.local/gear/black-diamond-spot-400.png","imageAttribution":"TrailMate catalog seed image","source":"seed"}]"""
        )

        val result = client.searchGearCatalog("", "")

        val items = (result as TrailMateApiResult.Success<List<TrailMateGearCatalogItemDto>>).value
        assertEquals(listOf("Leki Legacy Lite AS", "Black Diamond Spot 400"), items.map { it.displayName })
        assertEquals(listOf("长距离", "下坡", "稳定"), items.first().tags)
        assertTrue(requests.single().rawQuery.contains("category="))
        assertTrue(requests.single().rawQuery.contains("q="))
    }

    @Test
    fun searchCatalogIgnoresUnknownNestedFieldsAndDecodesEscapedStrings() {
        server.respondJson(
            path = "/api/v1/gear/catalog/search",
            status = 200,
            body = """
                [
                  {
                    "catalogItemId": "cat_headlamp_nitecore_nu25",
                    "category": "头灯",
                    "brand": "Nitecore",
                    "model": "NU25",
                    "displayName": "Nitecore \"NU25\"\n轻量头灯",
                    "weightGrams": 45,
                    "tags": ["USB-C", "夜间\n备用"],
                    "imageUrl": "/gear-thumbnails/nitecore-nu25.png",
                    "imageAttribution": "TrailMate hosted catalog thumbnail",
                    "source": "seed",
                    "metadata": {
                      "rank": 1,
                      "sourceNotes": ["server-owned field"]
                    }
                  }
                ]
            """.trimIndent()
        )

        val result = client.searchGearCatalog("头灯", "nu25")

        val item = (result as TrailMateApiResult.Success<List<TrailMateGearCatalogItemDto>>).value.single()
        assertEquals("cat_headlamp_nitecore_nu25", item.catalogItemId)
        assertEquals("Nitecore \"NU25\"\n轻量头灯", item.displayName)
        assertEquals(listOf("USB-C", "夜间\n备用"), item.tags)
        assertEquals("http://127.0.0.1:${server.address.port}/gear-thumbnails/nitecore-nu25.png", item.imageUrl)
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
                rawQuery = exchange.requestURI.rawQuery.orEmpty(),
                body = exchange.requestBody.bufferedReader(Charsets.UTF_8).readText()
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
        val rawQuery: String,
        val body: String
    )
}
