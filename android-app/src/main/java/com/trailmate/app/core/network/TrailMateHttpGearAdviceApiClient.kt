package com.trailmate.app.core.network

import com.trailmate.app.core.model.AiGearAdvisorRequest
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteAssessmentSummary
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

class TrailMateHttpGearAdviceApiClient(
    private val baseUrl: String,
    private val accessTokenProvider: () -> String?,
    private val userIdProvider: () -> String? = { null },
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 10_000
) : TrailMateGearAdviceApi {
    private val normalizedBaseUrl = baseUrl.trim().trimEnd('/')

    override fun requestGearAdvice(
        planId: String,
        request: AiGearAdvisorRequest
    ): TrailMateApiResult<AiGearAdvisorResponse> {
        val accessToken = accessTokenProvider()?.trim().orEmpty()
        if (accessToken.isBlank()) {
            return TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 401,
                    code = "AUTH_REQUIRED",
                    message = "Login is required before requesting AI gear advice.",
                    traceId = null
                )
            )
        }

        return postJson(
            path = TrailMateServerApiContract.Endpoints.gearAdvice.replace(
                oldValue = "{planId}",
                newValue = planId.pathSegmentEncoded()
            ),
            bearerToken = accessToken,
            userId = userIdProvider()?.trim().orEmpty(),
            body = request.toJson(),
            parseSuccess = ::parseGearAdviceResponse
        )
    }

    private fun <T> postJson(
        path: String,
        bearerToken: String,
        userId: String,
        body: String,
        parseSuccess: (String) -> T
    ): TrailMateApiResult<T> {
        val connection = URL(normalizedBaseUrl + TrailMateServerApiContract.BASE_PATH + path)
            .openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            if (userId.isNotBlank()) {
                connection.setRequestProperty("X-TrailMate-User-Id", userId)
            }
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.compactJson())
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
            } else {
                connection.errorStream?.bufferedReader(Charsets.UTF_8)?.readText().orEmpty()
            }
            if (responseCode in 200..299) {
                TrailMateApiResult.Success(parseSuccess(responseBody))
            } else {
                TrailMateApiResult.Failure(TrailMateJson.apiError(responseBody, responseCode))
            }
        } catch (exception: SocketTimeoutException) {
            TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 0,
                    code = "NETWORK_TIMEOUT",
                    message = exception.message ?: "Timed out while requesting AI gear advice.",
                    traceId = null
                )
            )
        } catch (exception: Exception) {
            TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 0,
                    code = "NETWORK_ERROR",
                    message = exception.message ?: "Network request failed.",
                    traceId = null
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun parseGearAdviceResponse(json: String): AiGearAdvisorResponse {
        val response = TrailMateJson.objectOrEmpty(json)
        return AiGearAdvisorResponse(
            assessmentFingerprint = response.nullableString("assessmentFingerprint").orEmpty(),
            recommendations = response
                .optJSONArray("recommendations")
                ?.objectList()
                ?.mapNotNull { item -> item.toGearRecommendation() }
                .orEmpty()
        )
    }

    private fun JSONObject.toGearRecommendation(): GearRecommendation? {
        val category = nullableString("category")?.takeIf { it.isNotBlank() } ?: return null
        val rationale = nullableString("rationale")?.takeIf { it.isNotBlank() } ?: return null
        return GearRecommendation(
            category = category,
            status = enumValue("status", GearStatus.CHECK),
            rationale = rationale,
            matchedGearItemId = nullableString("matchedGearItemId")?.ifBlank { null }
        )
    }

    private fun AiGearAdvisorRequest.toJson(): String =
        """
        {
          "assessmentFingerprint": ${assessmentFingerprint.json()},
          "route": ${route.toJson()},
          "profile": ${profile.toJson()},
          "assessment": ${assessment.toJson()},
          "fallbackRecommendations": ${fallbackRecommendations.toJson()},
          "guardrails": ${guardrails.toJsonArray()}
        }
        """.trimIndent()

    private fun ImportedRoute.toJson(): String =
        """
        {
          "routeName": ${routeName.json()},
          "fileName": ${fileName.json()},
          "distanceKm": $distanceKm,
          "ascentMeters": $ascentMeters,
          "pointCount": $pointCount,
          "durationMinutes": ${durationMinutes?.toString() ?: "null"}
        }
        """.trimIndent()

    private fun BaselineProfile.toJson(): String =
        """
        {
          "exerciseFrequency": ${exerciseFrequency.name.json()},
          "typicalDuration": ${typicalDuration.name.json()},
          "experienceLevel": ${experienceLevel.name.json()},
          "ascentExperience": ${ascentExperience.name.json()},
          "heightCm": ${heightCm?.toString() ?: "null"},
          "weightKg": ${weightKg?.toString() ?: "null"},
          "commonPackWeightKg": ${commonPackWeightKg?.toString() ?: "null"}
        }
        """.trimIndent()

    private fun RouteAssessmentSummary.toJson(): String =
        """
        {
          "routeName": ${routeName.json()},
          "distanceKm": $distanceKm,
          "ascentMeters": $ascentMeters,
          "matchLevel": ${matchLevel.name.json()},
          "confidenceLevel": ${confidenceLevel.name.json()},
          "estimatedDurationRange": ${estimatedDurationRange.json()},
          "risks": ${risks.toJsonArray()}
        }
        """.trimIndent()

    private fun List<GearRecommendation>.toJson(): String =
        joinToString(prefix = "[", postfix = "]") { recommendation ->
            """
            {
              "category": ${recommendation.category.json()},
              "status": ${recommendation.status.name.json()},
              "rationale": ${recommendation.rationale.json()},
              "matchedGearItemId": ${recommendation.matchedGearItemId?.json() ?: "null"}
            }
            """.trimIndent()
        }

    private fun List<String>.toJsonArray(): String =
        joinToString(prefix = "[", postfix = "]") { value -> value.json() }

    private fun String.compactJson(): String =
        lines().joinToString(separator = "") { line -> line.trim() }

    private fun String.pathSegmentEncoded(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")

    private fun String.json(): String =
        TrailMateJson.quote(this)
}
