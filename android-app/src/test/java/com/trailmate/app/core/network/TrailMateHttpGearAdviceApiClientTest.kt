package com.trailmate.app.core.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.trailmate.app.core.model.AiGearAdvisorContract
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.RouteAssessmentEngine
import com.trailmate.app.core.model.RouteGearAdvisorEngine
import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

class TrailMateHttpGearAdviceApiClientTest {
    private lateinit var server: HttpServer
    private lateinit var client: TrailMateHttpGearAdviceApiClient
    private val requests = mutableListOf<CapturedRequest>()
    private val assessment = RouteAssessmentEngine.assess(
        profile = TrailMateSampleData.baselineProfile,
        route = TrailMateSampleData.importedTargetRoute
    )
    private val request = AiGearAdvisorContract.buildRequest(
        route = TrailMateSampleData.importedTargetRoute,
        profile = TrailMateSampleData.baselineProfile,
        assessment = assessment,
        fallbackRecommendations = RouteGearAdvisorEngine.recommend(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = assessment
        )
    )

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
        client = TrailMateHttpGearAdviceApiClient(
            baseUrl = "http://127.0.0.1:${server.address.port}",
            accessTokenProvider = { "access-token-123" },
            userIdProvider = { "usr-1" }
        )
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun requestGearAdvicePostsRouteAssessmentPayloadWithBearerTokenAndParsesRecommendations() {
        server.respondJson(
            path = "/api/v1/plans/plan-123/gear-advice",
            status = 200,
            body = """
                {
                  "assessmentFingerprint": ${TrailMateJson.quote(request.assessmentFingerprint)},
                  "recommendations": [
                    {
                      "category": "头灯",
                      "status": "CHECK",
                      "rationale": "预计耗时较长，确认电量并准备备用照明。",
                      "matchedGearItemId": "cat_headlamp_bd_spot_400"
                    }
                  ]
                }
            """.trimIndent()
        )

        val result = client.requestGearAdvice(planId = "plan-123", request = request)

        val response = (result as TrailMateApiResult.Success).value
        assertEquals(request.assessmentFingerprint, response.assessmentFingerprint)
        assertEquals("头灯", response.recommendations.single().category)
        assertEquals(GearStatus.CHECK, response.recommendations.single().status)
        assertEquals("cat_headlamp_bd_spot_400", response.recommendations.single().matchedGearItemId)
        assertEquals("POST", requests.single().method)
        assertEquals("Bearer access-token-123", requests.single().authorization)
        assertEquals("usr-1", requests.single().userId)
        assertTrue(requests.single().body.contains("\"assessmentFingerprint\""))
        assertTrue(requests.single().body.contains("\"fallbackRecommendations\""))
    }

    @Test
    fun requestGearAdviceFailsBeforeNetworkWhenAuthTokenIsMissing() {
        client = TrailMateHttpGearAdviceApiClient(
            baseUrl = "http://127.0.0.1:${server.address.port}",
            accessTokenProvider = { null }
        )

        val result = client.requestGearAdvice(planId = "plan-123", request = request)

        val error = (result as TrailMateApiResult.Failure).error
        assertEquals(401, error.status)
        assertEquals("AUTH_REQUIRED", error.code)
        assertEquals(emptyList<CapturedRequest>(), requests)
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
                authorization = exchange.requestHeaders.getFirst("Authorization").orEmpty(),
                userId = exchange.requestHeaders.getFirst("X-TrailMate-User-Id").orEmpty(),
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
        val authorization: String,
        val userId: String,
        val body: String
    )
}
