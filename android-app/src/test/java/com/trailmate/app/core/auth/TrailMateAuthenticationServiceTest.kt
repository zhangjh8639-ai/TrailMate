package com.trailmate.app.core.auth

import com.trailmate.app.core.network.TrailMateApiError
import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateAuthApi
import com.trailmate.app.core.network.TrailMateAuthProviderDto
import com.trailmate.app.core.network.TrailMateAuthSessionDto
import com.trailmate.app.core.network.TrailMateLogoutRequestDto
import com.trailmate.app.core.network.TrailMatePhoneAuthScene
import com.trailmate.app.core.network.TrailMatePhoneCodeRequestDto
import com.trailmate.app.core.network.TrailMatePhoneCodeResponseDto
import com.trailmate.app.core.network.TrailMatePhoneLoginRequestDto
import com.trailmate.app.core.network.TrailMateRefreshSessionRequestDto
import com.trailmate.app.core.network.TrailMateWechatLoginRequestDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateAuthenticationServiceTest {
    @Test
    fun requestPhoneCodeNormalizesPhoneAndCallsBackend() {
        val api = FakeAuthApi(
            phoneCodeResult = TrailMateApiResult.Success(
                TrailMatePhoneCodeResponseDto(
                    phoneNumber = "+8613800138000",
                    expiresInSeconds = 300,
                    retryAfterSeconds = 60
                )
            )
        )
        val service = TrailMateAuthenticationService(api)

        val result = service.requestPhoneCode("138 0013 8000")

        assertEquals(
            listOf(TrailMatePhoneCodeRequestDto("+8613800138000", TrailMatePhoneAuthScene.LOGIN_OR_REGISTER)),
            api.phoneCodeRequests
        )
        assertEquals(
            TrailMateAuthActionResult.Success(
                TrailMatePhoneCodeReceipt("+8613800138000", 300, 60)
            ),
            result
        )
    }

    @Test
    fun requestPhoneCodeRejectsInvalidPhoneWithoutCallingBackend() {
        val api = FakeAuthApi()
        val service = TrailMateAuthenticationService(api)

        val result = service.requestPhoneCode("12345")

        assertEquals(emptyList<TrailMatePhoneCodeRequestDto>(), api.phoneCodeRequests)
        assertEquals(TrailMateAuthActionResult.InvalidInput("请输入有效手机号"), result)
    }

    @Test
    fun loginWithPhoneMapsBackendSessionToDomainSession() {
        val api = FakeAuthApi(
            phoneLoginResult = TrailMateApiResult.Success(
                TrailMateAuthSessionDto(
                    userId = "usr-1",
                    provider = TrailMateAuthProviderDto.PHONE,
                    accessToken = "access",
                    refreshToken = "refresh",
                    expiresAt = "2026-06-22T12:00:00Z",
                    phoneNumber = "+8613800138000"
                )
            )
        )
        val service = TrailMateAuthenticationService(api)

        val result = service.loginWithPhone("13800138000", "123456")

        assertEquals(
            listOf(TrailMatePhoneLoginRequestDto("+8613800138000", "123456")),
            api.phoneLoginRequests
        )
        val session = (result as TrailMateAuthActionResult.Success).value
        assertEquals(TrailMateAuthProvider.PHONE, session.provider)
        assertEquals("+8613800138000", session.phoneNumber)
        assertEquals("usr-1", session.userId)
    }

    @Test
    fun loginWithPhoneRejectsInvalidCodeWithoutCallingBackend() {
        val api = FakeAuthApi()
        val service = TrailMateAuthenticationService(api)

        val result = service.loginWithPhone("13800138000", "abc")

        assertEquals(emptyList<TrailMatePhoneLoginRequestDto>(), api.phoneLoginRequests)
        assertEquals(TrailMateAuthActionResult.InvalidInput("请输入有效验证码"), result)
    }

    @Test
    fun loginWithWechatExchangesAuthCodeForDomainSession() {
        val api = FakeAuthApi(
            wechatLoginResult = TrailMateApiResult.Success(
                TrailMateAuthSessionDto(
                    userId = "usr-wx",
                    provider = TrailMateAuthProviderDto.WECHAT,
                    accessToken = "access",
                    refreshToken = "refresh",
                    expiresAt = "2026-06-22T12:00:00Z",
                    wechatOpenId = "openid",
                    displayName = "张三"
                )
            )
        )
        val service = TrailMateAuthenticationService(api)

        val result = service.loginWithWechat(authCode = "wx-code", state = "nonce")

        assertEquals(listOf(TrailMateWechatLoginRequestDto("wx-code", "nonce")), api.wechatLoginRequests)
        val session = (result as TrailMateAuthActionResult.Success).value
        assertEquals(TrailMateAuthProvider.WECHAT, session.provider)
        assertEquals("openid", session.wechatOpenId)
        assertEquals("微信用户 张三", session.safeIdentityLabel())
    }

    @Test
    fun refreshSessionMapsRotatedBackendSessionToDomainSession() {
        val api = FakeAuthApi(
            refreshSessionResult = TrailMateApiResult.Success(
                TrailMateAuthSessionDto(
                    userId = "usr-wx",
                    provider = TrailMateAuthProviderDto.WECHAT,
                    accessToken = "new-access",
                    refreshToken = "new-refresh",
                    expiresAt = "2026-06-22T13:00:00Z",
                    wechatOpenId = "openid",
                    displayName = "张三"
                )
            )
        )
        val service = TrailMateAuthenticationService(api)

        val result = service.refreshSession(" old-refresh ")

        assertEquals(listOf(TrailMateRefreshSessionRequestDto("old-refresh")), api.refreshSessionRequests)
        val session = (result as TrailMateAuthActionResult.Success).value
        assertEquals(TrailMateAuthProvider.WECHAT, session.provider)
        assertEquals("new-access", session.accessToken)
        assertEquals("new-refresh", session.refreshToken)
    }

    @Test
    fun logoutSendsRefreshTokenToBackend() {
        val api = FakeAuthApi(
            logoutResult = TrailMateApiResult.Success(Unit)
        )
        val service = TrailMateAuthenticationService(api)

        val result = service.logout(" refresh ")

        assertEquals(listOf(TrailMateLogoutRequestDto("refresh")), api.logoutRequests)
        assertEquals(TrailMateAuthActionResult.Success(Unit), result)
    }

    @Test
    fun refreshAndLogoutRejectBlankTokenWithoutCallingBackend() {
        val api = FakeAuthApi()
        val service = TrailMateAuthenticationService(api)

        assertEquals(
            TrailMateAuthActionResult.InvalidInput("登录状态已失效，请重新登录"),
            service.refreshSession(" ")
        )
        assertEquals(
            TrailMateAuthActionResult.InvalidInput("登录状态已失效，请重新登录"),
            service.logout("")
        )
        assertEquals(emptyList<TrailMateRefreshSessionRequestDto>(), api.refreshSessionRequests)
        assertEquals(emptyList<TrailMateLogoutRequestDto>(), api.logoutRequests)
    }

    @Test
    fun backendFailurePreservesServerMessageForUi() {
        val api = FakeAuthApi(
            phoneLoginResult = TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 401,
                    code = "SMS_CODE_INVALID",
                    message = "验证码不正确或已过期",
                    traceId = "trace-1"
                )
            )
        )
        val service = TrailMateAuthenticationService(api)

        val result = service.loginWithPhone("13800138000", "123456")

        assertTrue(result is TrailMateAuthActionResult.Failure)
        assertEquals("SMS_CODE_INVALID", (result as TrailMateAuthActionResult.Failure).code)
        assertEquals("验证码不正确或已过期", result.message)
    }

    private class FakeAuthApi(
        private val phoneCodeResult: TrailMateApiResult<TrailMatePhoneCodeResponseDto> =
            TrailMateApiResult.Failure(TrailMateApiError(500, "NOT_STUBBED", "not stubbed", null)),
        private val phoneLoginResult: TrailMateApiResult<TrailMateAuthSessionDto> =
            TrailMateApiResult.Failure(TrailMateApiError(500, "NOT_STUBBED", "not stubbed", null)),
        private val wechatLoginResult: TrailMateApiResult<TrailMateAuthSessionDto> =
            TrailMateApiResult.Failure(TrailMateApiError(500, "NOT_STUBBED", "not stubbed", null)),
        private val refreshSessionResult: TrailMateApiResult<TrailMateAuthSessionDto> =
            TrailMateApiResult.Failure(TrailMateApiError(500, "NOT_STUBBED", "not stubbed", null)),
        private val logoutResult: TrailMateApiResult<Unit> =
            TrailMateApiResult.Failure(TrailMateApiError(500, "NOT_STUBBED", "not stubbed", null))
    ) : TrailMateAuthApi {
        val phoneCodeRequests = mutableListOf<TrailMatePhoneCodeRequestDto>()
        val phoneLoginRequests = mutableListOf<TrailMatePhoneLoginRequestDto>()
        val wechatLoginRequests = mutableListOf<TrailMateWechatLoginRequestDto>()
        val refreshSessionRequests = mutableListOf<TrailMateRefreshSessionRequestDto>()
        val logoutRequests = mutableListOf<TrailMateLogoutRequestDto>()

        override fun requestPhoneCode(
            request: TrailMatePhoneCodeRequestDto
        ): TrailMateApiResult<TrailMatePhoneCodeResponseDto> {
            phoneCodeRequests += request
            return phoneCodeResult
        }

        override fun loginWithPhone(
            request: TrailMatePhoneLoginRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> {
            phoneLoginRequests += request
            return phoneLoginResult
        }

        override fun loginWithWechat(
            request: TrailMateWechatLoginRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> {
            wechatLoginRequests += request
            return wechatLoginResult
        }

        override fun refreshSession(
            request: TrailMateRefreshSessionRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> {
            refreshSessionRequests += request
            return refreshSessionResult
        }

        override fun logout(
            request: TrailMateLogoutRequestDto
        ): TrailMateApiResult<Unit> {
            logoutRequests += request
            return logoutResult
        }
    }
}
