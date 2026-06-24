package com.trailmate.app.core.auth

import com.trailmate.app.core.network.TrailMateAuthApi
import com.trailmate.app.core.network.TrailMateHttpAuthApiClient

object TrailMateOnboardingAuthActionsFactory {
    fun create(
        backendBaseUrl: String,
        backendApiFactory: (String) -> TrailMateAuthApi = { baseUrl ->
            TrailMateHttpAuthApiClient(baseUrl = baseUrl)
        },
        wechatAuthCodeProvider: TrailMateWechatAuthCodeProvider = TrailMateUnavailableWechatAuthCodeProvider
    ): TrailMateOnboardingAuthActions {
        val normalizedBaseUrl = backendBaseUrl.trim()
        if (normalizedBaseUrl.isBlank()) {
            return TrailMateLocalOnboardingAuthActions()
        }

        return TrailMateBackendOnboardingAuthActions(
            service = TrailMateAuthenticationService(backendApiFactory(normalizedBaseUrl)),
            wechatAuthCodeProvider = wechatAuthCodeProvider
        )
    }
}

object TrailMateUnavailableWechatAuthCodeProvider : TrailMateWechatAuthCodeProvider {
    override fun requestAuthCode(): TrailMateWechatAuthCodeResult =
        TrailMateWechatAuthCodeResult.Unavailable
}
