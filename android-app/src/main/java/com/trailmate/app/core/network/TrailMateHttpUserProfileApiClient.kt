package com.trailmate.app.core.network

import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.TypicalDuration
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class TrailMateHttpUserProfileApiClient(
    private val baseUrl: String,
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 10_000
) : TrailMateUserProfileApi {
    override fun saveOnboardingProfile(
        userId: String,
        profile: BaselineProfile
    ): TrailMateApiResult<TrailMateOnboardingProfileDto> =
        requestJson(
            method = "PUT",
            path = TrailMateServerApiContract.Endpoints.userProfile,
            userId = userId,
            body = profile.toJson(),
            parseSuccess = { json -> json.toOnboardingProfile() }
        )

    private fun <T> requestJson(
        method: String,
        path: String,
        userId: String,
        body: String,
        parseSuccess: (String) -> T
    ): TrailMateApiResult<T> {
        val url = URL(baseUrl.trimEnd('/') + TrailMateServerApiContract.BASE_PATH + path)
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("X-TrailMate-User-Id", userId)
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body)
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
                TrailMateApiResult.Failure(responseBody.toApiError(responseCode))
            }
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

    private fun BaselineProfile.toJson(): String =
        "{" +
            "\"exerciseFrequency\":\"${exerciseFrequency.name}\"," +
            "\"typicalDuration\":\"${typicalDuration.name}\"," +
            "\"experienceLevel\":\"${experienceLevel.name}\"," +
            "\"ascentExperience\":\"${ascentExperience.name}\"," +
            "\"heightCm\":${heightCm.jsonNullable()}," +
            "\"weightKg\":${weightKg.jsonNullable()}," +
            "\"commonPackWeightKg\":${commonPackWeightKg.jsonNullable()}" +
            "}"

    private fun String.toOnboardingProfile(): TrailMateOnboardingProfileDto {
        val response = TrailMateJson.objectOrEmpty(this)
        return TrailMateOnboardingProfileDto(
            userId = response.nullableString("userId").orEmpty(),
            exerciseFrequency = response.enumValue("exerciseFrequency", ExerciseFrequency.RARELY),
            typicalDuration = response.enumValue("typicalDuration", TypicalDuration.UNDER_30),
            experienceLevel = response.enumValue("experienceLevel", ExperienceLevel.BEGINNER),
            ascentExperience = response.enumValue("ascentExperience", AscentExperience.UNDER_300),
            heightCm = response.nullableInt("heightCm"),
            weightKg = response.nullableInt("weightKg"),
            commonPackWeightKg = response.nullableInt("commonPackWeightKg"),
            updatedAt = response.nullableString("updatedAt")
        )
    }

    private fun String.toApiError(statusCode: Int): TrailMateApiError =
        TrailMateJson.apiError(this, statusCode)

    private fun Int?.jsonNullable(): String =
        this?.toString() ?: "null"
}
