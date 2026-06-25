package com.trailmate.app.core.network

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class TrailMateHttpAuthApiClient(
    private val baseUrl: String,
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 10_000
) : TrailMateAuthApi {
    override fun requestPhoneCode(
        request: TrailMatePhoneCodeRequestDto
    ): TrailMateApiResult<TrailMatePhoneCodeResponseDto> =
        postJson(
            path = TrailMateServerApiContract.Endpoints.requestPhoneCode,
            body = """
                {"phoneNumber":${request.phoneNumber.json()},"scene":${request.scene.name.json()}}
            """.trimIndent(),
            parseSuccess = { json ->
                val response = TrailMateJson.objectOrEmpty(json)
                TrailMatePhoneCodeResponseDto(
                    phoneNumber = response.nullableString("phoneNumber").orEmpty(),
                    expiresInSeconds = response.nullableInt("expiresInSeconds") ?: 0,
                    retryAfterSeconds = response.nullableInt("retryAfterSeconds") ?: 0
                )
            }
        )

    override fun loginWithPhone(
        request: TrailMatePhoneLoginRequestDto
    ): TrailMateApiResult<TrailMateAuthSessionDto> =
        postJson(
            path = TrailMateServerApiContract.Endpoints.loginWithPhone,
            body = """
                {"phoneNumber":${request.phoneNumber.json()},"smsCode":${request.smsCode.json()}}
            """.trimIndent(),
            parseSuccess = ::parseAuthSession
        )

    override fun loginWithWechat(
        request: TrailMateWechatLoginRequestDto
    ): TrailMateApiResult<TrailMateAuthSessionDto> =
        postJson(
            path = TrailMateServerApiContract.Endpoints.loginWithWechat,
            body = """
                {"authCode":${request.authCode.json()},"state":${request.state.json()}}
            """.trimIndent(),
            parseSuccess = ::parseAuthSession
        )

    override fun refreshSession(
        request: TrailMateRefreshSessionRequestDto
    ): TrailMateApiResult<TrailMateAuthSessionDto> =
        postJson(
            path = TrailMateServerApiContract.Endpoints.refreshSession,
            body = """
                {"refreshToken":${request.refreshToken.json()}}
            """.trimIndent(),
            parseSuccess = ::parseAuthSession
        )

    override fun logout(
        request: TrailMateLogoutRequestDto
    ): TrailMateApiResult<Unit> =
        postJson(
            path = TrailMateServerApiContract.Endpoints.logout,
            body = """
                {"refreshToken":${request.refreshToken.json()}}
            """.trimIndent(),
            parseSuccess = { _: String -> }
        )

    private fun <T> postJson(
        path: String,
        body: String,
        parseSuccess: (String) -> T
    ): TrailMateApiResult<T> {
        val connection = URL(baseUrl.trimEnd('/') + TrailMateServerApiContract.BASE_PATH + path)
            .openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.doOutput = true
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

    private fun parseAuthSession(json: String): TrailMateAuthSessionDto {
        val response = TrailMateJson.objectOrEmpty(json)
        return TrailMateAuthSessionDto(
            userId = response.nullableString("userId").orEmpty(),
            accessToken = response.nullableString("accessToken").orEmpty(),
            refreshToken = response.nullableString("refreshToken").orEmpty(),
            expiresAt = response.nullableString("expiresAt").orEmpty(),
            provider = response.enumValue("provider", TrailMateAuthProviderDto.PHONE),
            phoneNumber = response.nullableString("phoneNumber"),
            wechatOpenId = response.nullableString("wechatOpenId"),
            displayName = response.nullableString("displayName")
        )
    }

    private fun String.toApiError(statusCode: Int): TrailMateApiError =
        TrailMateJson.apiError(this, statusCode)

    private fun String.compactJson(): String =
        lines().joinToString(separator = "") { line -> line.trim() }

    private fun String.json(): String =
        TrailMateJson.quote(this)
}
