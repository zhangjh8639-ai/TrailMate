package com.trailmate.app.core.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

class TrailMateHttpAuthApiClientTest {
    private lateinit var server: HttpServer
    private lateinit var client: TrailMateHttpAuthApiClient
    private val requests = mutableListOf<CapturedRequest>()

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
        client = TrailMateHttpAuthApiClient(
            baseUrl = "http://127.0.0.1:${server.address.port}"
        )
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun requestPhoneCodePostsContractJsonAndParsesReceipt() {
        server.respondJson(
            path = "/api/v1/auth/phone/code",
            status = 200,
            body = """
                {
                  "phoneNumber": "+8613800138000",
                  "expiresInSeconds": 300,
                  "retryAfterSeconds": 60
                }
            """.trimIndent()
        )

        val result = client.requestPhoneCode(
            TrailMatePhoneCodeRequestDto(
                phoneNumber = "+8613800138000",
                scene = TrailMatePhoneAuthScene.LOGIN_OR_REGISTER
            )
        )

        assertEquals(TrailMateApiResult.Success(TrailMatePhoneCodeResponseDto("+8613800138000", 300, 60)), result)
        assertEquals("POST", requests.single().method)
        assertEquals("/api/v1/auth/phone/code", requests.single().path)
        assertTrue(requests.single().body.contains("\"phoneNumber\":\"+8613800138000\""))
        assertTrue(requests.single().body.contains("\"scene\":\"LOGIN_OR_REGISTER\""))
    }

    @Test
    fun loginWithPhoneParsesAuthSession() {
        server.respondJson(
            path = "/api/v1/auth/phone/login",
            status = 200,
            body = """
                {
                  "userId": "usr_phone_8000",
                  "provider": "PHONE",
                  "accessToken": "access",
                  "refreshToken": "refresh",
                  "expiresAt": "2026-06-22T12:00:00Z",
                  "phoneNumber": "+8613800138000",
                  "wechatOpenId": null,
                  "displayName": null
                }
            """.trimIndent()
        )

        val result = client.loginWithPhone(
            TrailMatePhoneLoginRequestDto("+8613800138000", "123456")
        )

        val session = (result as TrailMateApiResult.Success<TrailMateAuthSessionDto>).value
        assertEquals(TrailMateAuthProviderDto.PHONE, session.provider)
        assertEquals("+8613800138000", session.phoneNumber)
        assertEquals("access", session.accessToken)
        assertTrue(requests.single().body.contains("\"smsCode\":\"123456\""))
    }

    @Test
    fun loginWithWechatParsesWechatSession() {
        server.respondJson(
            path = "/api/v1/auth/wechat/login",
            status = 200,
            body = """
                {
                  "userId": "usr_wechat",
                  "provider": "WECHAT",
                  "accessToken": "access",
                  "refreshToken": "refresh",
                  "expiresAt": "2026-06-22T12:00:00Z",
                  "phoneNumber": null,
                  "wechatOpenId": "openid",
                  "displayName": "张三"
                }
            """.trimIndent()
        )

        val result = client.loginWithWechat(
            TrailMateWechatLoginRequestDto("wx-code", "nonce")
        )

        val session = (result as TrailMateApiResult.Success<TrailMateAuthSessionDto>).value
        assertEquals(TrailMateAuthProviderDto.WECHAT, session.provider)
        assertEquals("openid", session.wechatOpenId)
        assertTrue(requests.single().body.contains("\"authCode\":\"wx-code\""))
        assertTrue(requests.single().body.contains("\"state\":\"nonce\""))
    }

    @Test
    fun loginWithWechatDecodesEscapedDisplayName() {
        server.respondJson(
            path = "/api/v1/auth/wechat/login",
            status = 200,
            body = """
                {
                  "userId": "usr_wechat",
                  "provider": "WECHAT",
                  "accessToken": "access",
                  "refreshToken": "refresh",
                  "expiresAt": "2026-06-22T12:00:00Z",
                  "phoneNumber": null,
                  "wechatOpenId": "openid",
                  "displayName": "TrailMate \"测试\"\n微信用户"
                }
            """.trimIndent()
        )

        val result = client.loginWithWechat(
            TrailMateWechatLoginRequestDto("wx-code", "nonce")
        )

        val session = (result as TrailMateApiResult.Success<TrailMateAuthSessionDto>).value
        assertEquals("TrailMate \"测试\"\n微信用户", session.displayName)
    }

    @Test
    fun refreshSessionPostsRefreshTokenAndParsesRotatedSession() {
        server.respondJson(
            path = "/api/v1/auth/refresh",
            status = 200,
            body = """
                {
                  "userId": "usr_wechat",
                  "provider": "WECHAT",
                  "accessToken": "new-access",
                  "refreshToken": "new-refresh",
                  "expiresAt": "2026-06-22T13:00:00Z",
                  "phoneNumber": null,
                  "wechatOpenId": "openid",
                  "displayName": "张三"
                }
            """.trimIndent()
        )

        val result = client.refreshSession(
            TrailMateRefreshSessionRequestDto("old-refresh")
        )

        val session = (result as TrailMateApiResult.Success<TrailMateAuthSessionDto>).value
        assertEquals(TrailMateAuthProviderDto.WECHAT, session.provider)
        assertEquals("new-access", session.accessToken)
        assertEquals("new-refresh", session.refreshToken)
        assertTrue(requests.single().body.contains("\"refreshToken\":\"old-refresh\""))
    }

    @Test
    fun logoutPostsRefreshTokenAndAcceptsNoContent() {
        server.respondJson(
            path = "/api/v1/auth/logout",
            status = 204,
            body = ""
        )

        val result = client.logout(
            TrailMateLogoutRequestDto("refresh-to-revoke")
        )

        assertEquals(TrailMateApiResult.Success(Unit), result)
        assertEquals("/api/v1/auth/logout", requests.single().path)
        assertTrue(requests.single().body.contains("\"refreshToken\":\"refresh-to-revoke\""))
    }

    @Test
    fun errorResponseMapsToApiFailure() {
        server.respondJson(
            path = "/api/v1/auth/phone/login",
            status = 401,
            body = """
                {
                  "status": 401,
                  "code": "SMS_CODE_INVALID",
                  "message": "验证码不正确或已过期",
                  "traceId": "trace-1"
                }
            """.trimIndent()
        )

        val result = client.loginWithPhone(
            TrailMatePhoneLoginRequestDto("+8613800138000", "000000")
        )

        val error = (result as TrailMateApiResult.Failure).error
        assertEquals(401, error.status)
        assertEquals("SMS_CODE_INVALID", error.code)
        assertEquals("验证码不正确或已过期", error.message)
        assertEquals("trace-1", error.traceId)
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
        val body: String
    )
}
