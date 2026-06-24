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

data class TrailMatePhoneCodeReceipt(
    val phoneNumber: String,
    val expiresInSeconds: Int,
    val retryAfterSeconds: Int
)

sealed interface TrailMateAuthActionResult<out T> {
    data class Success<out T>(val value: T) : TrailMateAuthActionResult<T>
    data class InvalidInput(val message: String) : TrailMateAuthActionResult<Nothing>
    data class Failure(
        val code: String,
        val message: String,
        val traceId: String?
    ) : TrailMateAuthActionResult<Nothing>
}

class TrailMateAuthenticationService(
    private val api: TrailMateAuthApi
) {
    fun requestPhoneCode(rawPhoneNumber: String): TrailMateAuthActionResult<TrailMatePhoneCodeReceipt> {
        val phoneNumber = TrailMatePhoneNumber.normalizeMainlandChina(rawPhoneNumber)
            ?: return TrailMateAuthActionResult.InvalidInput("请输入有效手机号")

        return when (
            val result = api.requestPhoneCode(
                TrailMatePhoneCodeRequestDto(
                    phoneNumber = phoneNumber,
                    scene = TrailMatePhoneAuthScene.LOGIN_OR_REGISTER
                )
            )
        ) {
            is TrailMateApiResult.Success -> TrailMateAuthActionResult.Success(result.value.toReceipt())
            is TrailMateApiResult.Failure -> result.error.toAuthFailure()
        }
    }

    fun loginWithPhone(
        rawPhoneNumber: String,
        smsCode: String
    ): TrailMateAuthActionResult<TrailMateAuthSession> {
        val phoneNumber = TrailMatePhoneNumber.normalizeMainlandChina(rawPhoneNumber)
            ?: return TrailMateAuthActionResult.InvalidInput("请输入有效手机号")
        val normalizedCode = smsCode.trim()
        if (!TrailMateSmsCode.isValid(normalizedCode)) {
            return TrailMateAuthActionResult.InvalidInput("请输入有效验证码")
        }

        return when (
            val result = api.loginWithPhone(
                TrailMatePhoneLoginRequestDto(
                    phoneNumber = phoneNumber,
                    smsCode = normalizedCode
                )
            )
        ) {
            is TrailMateApiResult.Success -> TrailMateAuthActionResult.Success(result.value.toAuthSession())
            is TrailMateApiResult.Failure -> result.error.toAuthFailure()
        }
    }

    fun loginWithWechat(
        authCode: String,
        state: String
    ): TrailMateAuthActionResult<TrailMateAuthSession> {
        if (authCode.isBlank() || state.isBlank()) {
            return TrailMateAuthActionResult.InvalidInput("微信授权信息无效")
        }

        return when (
            val result = api.loginWithWechat(
                TrailMateWechatLoginRequestDto(
                    authCode = authCode,
                    state = state
                )
            )
        ) {
            is TrailMateApiResult.Success -> TrailMateAuthActionResult.Success(result.value.toAuthSession())
            is TrailMateApiResult.Failure -> result.error.toAuthFailure()
        }
    }

    fun refreshSession(refreshToken: String): TrailMateAuthActionResult<TrailMateAuthSession> {
        val normalizedRefreshToken = refreshToken.trim()
        if (normalizedRefreshToken.isBlank()) {
            return TrailMateAuthActionResult.InvalidInput(EXPIRED_SESSION_MESSAGE)
        }

        return when (
            val result = api.refreshSession(
                TrailMateRefreshSessionRequestDto(normalizedRefreshToken)
            )
        ) {
            is TrailMateApiResult.Success -> TrailMateAuthActionResult.Success(result.value.toAuthSession())
            is TrailMateApiResult.Failure -> result.error.toAuthFailure()
        }
    }

    fun logout(refreshToken: String): TrailMateAuthActionResult<Unit> {
        val normalizedRefreshToken = refreshToken.trim()
        if (normalizedRefreshToken.isBlank()) {
            return TrailMateAuthActionResult.InvalidInput(EXPIRED_SESSION_MESSAGE)
        }

        return when (
            val result = api.logout(
                TrailMateLogoutRequestDto(normalizedRefreshToken)
            )
        ) {
            is TrailMateApiResult.Success -> TrailMateAuthActionResult.Success(Unit)
            is TrailMateApiResult.Failure -> result.error.toAuthFailure()
        }
    }

    private fun TrailMatePhoneCodeResponseDto.toReceipt(): TrailMatePhoneCodeReceipt =
        TrailMatePhoneCodeReceipt(
            phoneNumber = phoneNumber,
            expiresInSeconds = expiresInSeconds,
            retryAfterSeconds = retryAfterSeconds
        )

    private fun TrailMateAuthSessionDto.toAuthSession(): TrailMateAuthSession =
        TrailMateAuthSession(
            userId = userId,
            provider = when (provider) {
                TrailMateAuthProviderDto.WECHAT -> TrailMateAuthProvider.WECHAT
                TrailMateAuthProviderDto.EMAIL,
                TrailMateAuthProviderDto.PHONE -> TrailMateAuthProvider.PHONE
            },
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            phoneNumber = phoneNumber,
            wechatOpenId = wechatOpenId,
            displayName = displayName
        )

    private fun TrailMateApiError.toAuthFailure(): TrailMateAuthActionResult.Failure =
        TrailMateAuthActionResult.Failure(
            code = code,
            message = message,
            traceId = traceId
        )

    private companion object {
        const val EXPIRED_SESSION_MESSAGE = "登录状态已失效，请重新登录"
    }
}
