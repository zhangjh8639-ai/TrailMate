package com.trailmate.app.core.auth

import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateAuthApi
import com.trailmate.app.core.network.TrailMateAuthProviderDto
import com.trailmate.app.core.network.TrailMateAuthSessionDto
import com.trailmate.app.core.network.TrailMateLogoutRequestDto
import com.trailmate.app.core.network.TrailMatePhoneCodeRequestDto
import com.trailmate.app.core.network.TrailMatePhoneCodeResponseDto
import com.trailmate.app.core.network.TrailMatePhoneLoginRequestDto
import com.trailmate.app.core.network.TrailMateRefreshSessionRequestDto
import com.trailmate.app.core.network.TrailMateWechatLoginRequestDto
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateOnboardingAuthActionsTest {
    @Test
    fun localActionsKeepPrototypeUsableButStillValidatePhoneAndCode() {
        val actions = TrailMateLocalOnboardingAuthActions(nowEpochMillis = { 42L })

        val codeResult = actions.requestPhoneCode("13800138000")
        val loginResult = actions.loginWithPhone("13800138000", "123456")

        assertEquals(
            TrailMateAuthActionResult.Success(TrailMatePhoneCodeReceipt("+8613800138000", 300, 60)),
            codeResult
        )
        val session = (loginResult as TrailMateAuthActionResult.Success<*>).value as TrailMateAuthSession
        assertEquals(TrailMateAuthProvider.PHONE, session.provider)
        assertEquals("+8613800138000", session.phoneNumber)
        assertEquals("local-phone-8000", session.userId)
    }

    @Test
    fun localActionsRejectInvalidPhoneBeforePrototypeLogin() {
        val actions = TrailMateLocalOnboardingAuthActions(nowEpochMillis = { 42L })

        assertEquals(
            TrailMateAuthActionResult.InvalidInput("请输入有效手机号"),
            actions.requestPhoneCode("12345")
        )
        assertEquals(
            TrailMateAuthActionResult.InvalidInput("请输入有效手机号"),
            actions.loginWithPhone("12345", "123456")
        )
    }

    @Test
    fun localActionsDoNotFakeWechatLogin() {
        val actions = TrailMateLocalOnboardingAuthActions(nowEpochMillis = { 42L })

        assertEquals(
            TrailMateAuthActionResult.Failure(
                code = "WECHAT_BACKEND_NOT_CONFIGURED",
                message = "请先配置 TrailMate 后端和微信 AppID",
                traceId = null
            ),
            actions.loginWithWechat()
        )
    }

    @Test
    fun backendActionsExchangeWechatAuthCodeThroughService() {
        val api = FakeWechatAuthApi()
        val actions = TrailMateBackendOnboardingAuthActions(
            service = TrailMateAuthenticationService(api),
            wechatAuthCodeProvider = FakeWechatAuthCodeProvider(
                TrailMateWechatAuthCodeResult.Success(
                    authCode = "wx-code",
                    state = "nonce"
                )
            )
        )

        val result = actions.loginWithWechat()

        assertEquals(listOf(TrailMateWechatLoginRequestDto("wx-code", "nonce")), api.wechatRequests)
        val session = (result as TrailMateAuthActionResult.Success<*>).value as TrailMateAuthSession
        assertEquals(TrailMateAuthProvider.WECHAT, session.provider)
        assertEquals("openid", session.wechatOpenId)
    }

    @Test
    fun backendActionsSurfaceWechatCancellationWithoutCallingServer() {
        val api = FakeWechatAuthApi()
        val actions = TrailMateBackendOnboardingAuthActions(
            service = TrailMateAuthenticationService(api),
            wechatAuthCodeProvider = FakeWechatAuthCodeProvider(TrailMateWechatAuthCodeResult.Cancelled)
        )

        val result = actions.loginWithWechat()

        assertEquals(emptyList<TrailMateWechatLoginRequestDto>(), api.wechatRequests)
        assertEquals(TrailMateAuthActionResult.InvalidInput("已取消微信授权"), result)
    }

    @Test
    fun backendActionsSurfaceWechatPendingWithoutCallingServer() {
        val api = FakeWechatAuthApi()
        val actions = TrailMateBackendOnboardingAuthActions(
            service = TrailMateAuthenticationService(api),
            wechatAuthCodeProvider = FakeWechatAuthCodeProvider(TrailMateWechatAuthCodeResult.Pending)
        )

        val result = actions.loginWithWechat()

        assertEquals(emptyList<TrailMateWechatLoginRequestDto>(), api.wechatRequests)
        assertEquals(
            TrailMateAuthActionResult.InvalidInput("已打开微信授权，完成后返回 TrailMate"),
            result
        )
    }

    @Test
    fun backendActionsConsumeStoredWechatCallbackWithoutLaunchingWechatAgain() {
        val api = FakeWechatAuthApi()
        val launcher = FakeWechatRequestLauncher(
            TrailMateWechatAuthLaunchResult.Launched("wx-state")
        )
        val callbackStore = TrailMateWechatAuthCallbackStore()
        val actions = TrailMateBackendOnboardingAuthActions(
            service = TrailMateAuthenticationService(api),
            wechatAuthCodeProvider = TrailMateStoredWechatAuthCodeProvider(
                callbackStore = callbackStore,
                requestLauncher = launcher
            )
        )
        assertEquals(TrailMateAuthActionResult.InvalidInput("已打开微信授权，完成后返回 TrailMate"), actions.loginWithWechat())
        callbackStore.publish(TrailMateWechatAuthCodeResult.Success("wx-return-code", "wx-state"))

        val result = actions.consumeWechatCallback()

        assertEquals(listOf(TrailMateWechatLoginRequestDto("wx-return-code", "wx-state")), api.wechatRequests)
        assertEquals(1, launcher.launchCount)
        val session = (result as TrailMateAuthActionResult.Success<*>).value as TrailMateAuthSession
        assertEquals(TrailMateAuthProvider.WECHAT, session.provider)
    }

    @Test
    fun backendActionsRejectWechatCallbackWhenStateDoesNotMatchLaunchState() {
        val api = FakeWechatAuthApi()
        val callbackStore = TrailMateWechatAuthCallbackStore()
        val actions = TrailMateBackendOnboardingAuthActions(
            service = TrailMateAuthenticationService(api),
            wechatAuthCodeProvider = TrailMateStoredWechatAuthCodeProvider(
                callbackStore = callbackStore,
                requestLauncher = FakeWechatRequestLauncher(
                    TrailMateWechatAuthLaunchResult.Launched("expected-state")
                )
            )
        )

        assertEquals(TrailMateAuthActionResult.InvalidInput("已打开微信授权，完成后返回 TrailMate"), actions.loginWithWechat())
        callbackStore.publish(TrailMateWechatAuthCodeResult.Success("wx-return-code", "wrong-state"))

        val result = actions.consumeWechatCallback()

        assertEquals(emptyList<TrailMateWechatLoginRequestDto>(), api.wechatRequests)
        assertEquals(TrailMateAuthActionResult.InvalidInput("微信授权已过期，请重新登录"), result)
    }

    private class FakeWechatAuthCodeProvider(
        private val result: TrailMateWechatAuthCodeResult
    ) : TrailMateWechatAuthCodeProvider {
        override fun requestAuthCode(): TrailMateWechatAuthCodeResult = result
    }

    private class FakeWechatRequestLauncher(
        private val result: TrailMateWechatAuthLaunchResult
    ) : TrailMateWechatAuthRequestLauncher {
        var launchCount = 0

        override fun launchWechatAuth(): TrailMateWechatAuthLaunchResult {
            launchCount += 1
            return result
        }
    }

    private class FakeWechatAuthApi : TrailMateAuthApi {
        val wechatRequests = mutableListOf<TrailMateWechatLoginRequestDto>()

        override fun requestPhoneCode(
            request: TrailMatePhoneCodeRequestDto
        ): TrailMateApiResult<TrailMatePhoneCodeResponseDto> =
            error("not used")

        override fun loginWithPhone(
            request: TrailMatePhoneLoginRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> =
            error("not used")

        override fun loginWithWechat(
            request: TrailMateWechatLoginRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> {
            wechatRequests += request
            return TrailMateApiResult.Success(
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
        }

        override fun refreshSession(
            request: TrailMateRefreshSessionRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> =
            error("not used")

        override fun logout(
            request: TrailMateLogoutRequestDto
        ): TrailMateApiResult<Unit> =
            error("not used")
    }
}
