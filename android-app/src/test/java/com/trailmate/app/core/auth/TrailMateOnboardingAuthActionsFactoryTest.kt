package com.trailmate.app.core.auth

import com.trailmate.app.core.network.TrailMateAuthApi
import com.trailmate.app.core.network.TrailMateAuthSessionDto
import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateLogoutRequestDto
import com.trailmate.app.core.network.TrailMatePhoneCodeRequestDto
import com.trailmate.app.core.network.TrailMatePhoneCodeResponseDto
import com.trailmate.app.core.network.TrailMatePhoneLoginRequestDto
import com.trailmate.app.core.network.TrailMateRefreshSessionRequestDto
import com.trailmate.app.core.network.TrailMateWechatLoginRequestDto
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateOnboardingAuthActionsFactoryTest {
    @Test
    fun blankBackendUrlUsesLocalPreviewActions() {
        val actions = TrailMateOnboardingAuthActionsFactory.create(
            backendBaseUrl = "",
            backendApiFactory = { error("should not create backend api") }
        )

        assertTrue(actions is TrailMateLocalOnboardingAuthActions)
    }

    @Test
    fun configuredBackendUrlUsesBackendActions() {
        val actions = TrailMateOnboardingAuthActionsFactory.create(
            backendBaseUrl = "http://127.0.0.1:8080",
            backendApiFactory = { FakeAuthApi }
        )

        assertTrue(actions is TrailMateBackendOnboardingAuthActions)
    }

    private object FakeAuthApi : TrailMateAuthApi {
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
        ): TrailMateApiResult<TrailMateAuthSessionDto> =
            error("not used")

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
