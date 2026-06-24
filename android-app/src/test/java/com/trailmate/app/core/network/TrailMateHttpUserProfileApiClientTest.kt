package com.trailmate.app.core.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.TypicalDuration
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

class TrailMateHttpUserProfileApiClientTest {
    private lateinit var server: HttpServer
    private lateinit var client: TrailMateHttpUserProfileApiClient
    private val requests = mutableListOf<CapturedRequest>()

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
        client = TrailMateHttpUserProfileApiClient(
            baseUrl = "http://127.0.0.1:${server.address.port}"
        )
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun saveOnboardingProfileSendsUserHeaderAndProfileEvidence() {
        server.respondJson(
            path = "/api/v1/users/me/profile",
            status = 200,
            body = """
                {
                  "userId": "usr-phone",
                  "exerciseFrequency": "ONE_TO_TWO_PER_WEEK",
                  "typicalDuration": "OVER_60",
                  "experienceLevel": "REGULAR",
                  "ascentExperience": "M300_TO_800",
                  "heightCm": 178,
                  "weightKg": 70,
                  "commonPackWeightKg": 6,
                  "updatedAt": "2026-06-23T08:00:00Z"
                }
            """.trimIndent()
        )

        val result = client.saveOnboardingProfile(
            userId = "usr-phone",
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.ONE_TO_TWO_PER_WEEK,
                typicalDuration = TypicalDuration.OVER_60,
                experienceLevel = ExperienceLevel.REGULAR,
                ascentExperience = AscentExperience.M300_TO_800,
                heightCm = 178,
                weightKg = 70,
                commonPackWeightKg = 6
            )
        )

        val response = (result as TrailMateApiResult.Success<TrailMateOnboardingProfileDto>).value
        assertEquals("usr-phone", response.userId)
        assertEquals(ExerciseFrequency.ONE_TO_TWO_PER_WEEK, response.exerciseFrequency)
        assertEquals(TypicalDuration.OVER_60, response.typicalDuration)
        assertEquals(ExperienceLevel.REGULAR, response.experienceLevel)
        assertEquals(AscentExperience.M300_TO_800, response.ascentExperience)
        assertEquals(178, response.heightCm)
        assertEquals("2026-06-23T08:00:00Z", response.updatedAt)
        assertEquals("PUT", requests.single().method)
        assertEquals("/api/v1/users/me/profile", requests.single().path)
        assertEquals("usr-phone", requests.single().userIdHeader)
        assertTrue(requests.single().body.contains("\"exerciseFrequency\":\"ONE_TO_TWO_PER_WEEK\""))
        assertTrue(requests.single().body.contains("\"commonPackWeightKg\":6"))
    }

    @Test
    fun errorResponseMapsEscapedMessageToApiFailure() {
        server.respondJson(
            path = "/api/v1/users/me/profile",
            status = 400,
            body = """
                {
                  "status": 400,
                  "code": "PROFILE_INVALID",
                  "message": "资料\"不完整\"\n请重试",
                  "traceId": "trace-profile"
                }
            """.trimIndent()
        )

        val result = client.saveOnboardingProfile(
            userId = "usr-phone",
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.RARELY,
                typicalDuration = TypicalDuration.UNDER_30,
                experienceLevel = ExperienceLevel.BEGINNER,
                ascentExperience = AscentExperience.UNDER_300,
                heightCm = null,
                weightKg = null,
                commonPackWeightKg = null
            )
        )

        val error = (result as TrailMateApiResult.Failure).error
        assertEquals(400, error.status)
        assertEquals("PROFILE_INVALID", error.code)
        assertEquals("资料\"不完整\"\n请重试", error.message)
        assertEquals("trace-profile", error.traceId)
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
                userIdHeader = exchange.requestHeaders.getFirst("X-TrailMate-User-Id"),
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
        val userIdHeader: String?,
        val body: String
    )
}
