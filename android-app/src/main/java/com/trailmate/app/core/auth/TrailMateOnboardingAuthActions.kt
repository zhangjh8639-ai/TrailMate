package com.trailmate.app.core.auth

interface TrailMateOnboardingAuthActions {
    fun requestPhoneCode(rawPhoneNumber: String): TrailMateAuthActionResult<TrailMatePhoneCodeReceipt>

    fun loginWithPhone(
        rawPhoneNumber: String,
        smsCode: String
    ): TrailMateAuthActionResult<TrailMateAuthSession>

    fun loginWithWechat(): TrailMateAuthActionResult<TrailMateAuthSession>
}

interface TrailMateWechatCallbackAuthActions {
    fun consumeWechatCallback(): TrailMateAuthActionResult<TrailMateAuthSession>?
}

class TrailMateLocalOnboardingAuthActions(
    private val nowEpochMillis: () -> Long = { System.currentTimeMillis() }
) : TrailMateOnboardingAuthActions {
    override fun requestPhoneCode(rawPhoneNumber: String): TrailMateAuthActionResult<TrailMatePhoneCodeReceipt> {
        val phoneNumber = TrailMatePhoneNumber.normalizeMainlandChina(rawPhoneNumber)
            ?: return TrailMateAuthActionResult.InvalidInput("请输入有效手机号")

        return TrailMateAuthActionResult.Success(
            TrailMatePhoneCodeReceipt(
                phoneNumber = phoneNumber,
                expiresInSeconds = 300,
                retryAfterSeconds = 60
            )
        )
    }

    override fun loginWithPhone(
        rawPhoneNumber: String,
        smsCode: String
    ): TrailMateAuthActionResult<TrailMateAuthSession> {
        val phoneNumber = TrailMatePhoneNumber.normalizeMainlandChina(rawPhoneNumber)
            ?: return TrailMateAuthActionResult.InvalidInput("请输入有效手机号")
        if (!TrailMateSmsCode.isValid(smsCode)) {
            return TrailMateAuthActionResult.InvalidInput("请输入有效验证码")
        }

        return TrailMateAuthActionResult.Success(
            TrailMateAuthSession.localPhoneSession(
                phoneNumber = phoneNumber,
                nowEpochMillis = nowEpochMillis()
            )
        )
    }

    override fun loginWithWechat(): TrailMateAuthActionResult<TrailMateAuthSession> =
        TrailMateAuthActionResult.Failure(
            code = "WECHAT_BACKEND_NOT_CONFIGURED",
            message = "请先配置 TrailMate 后端和微信 AppID",
            traceId = null
        )
}

class TrailMateBackendOnboardingAuthActions(
    private val service: TrailMateAuthenticationService,
    private val wechatAuthCodeProvider: TrailMateWechatAuthCodeProvider
) : TrailMateOnboardingAuthActions, TrailMateWechatCallbackAuthActions {
    override fun requestPhoneCode(rawPhoneNumber: String): TrailMateAuthActionResult<TrailMatePhoneCodeReceipt> =
        service.requestPhoneCode(rawPhoneNumber)

    override fun loginWithPhone(
        rawPhoneNumber: String,
        smsCode: String
    ): TrailMateAuthActionResult<TrailMateAuthSession> =
        service.loginWithPhone(rawPhoneNumber = rawPhoneNumber, smsCode = smsCode)

    override fun loginWithWechat(): TrailMateAuthActionResult<TrailMateAuthSession> =
        handleWechatCodeResult(wechatAuthCodeProvider.requestAuthCode())

    override fun consumeWechatCallback(): TrailMateAuthActionResult<TrailMateAuthSession>? =
        wechatAuthCodeProvider.consumeAuthCode()?.let(::handleWechatCodeResult)

    private fun handleWechatCodeResult(
        result: TrailMateWechatAuthCodeResult
    ): TrailMateAuthActionResult<TrailMateAuthSession> =
        when (result) {
            is TrailMateWechatAuthCodeResult.Success -> service.loginWithWechat(
                authCode = result.authCode,
                state = result.state
            )
            TrailMateWechatAuthCodeResult.Cancelled ->
                TrailMateAuthActionResult.InvalidInput("已取消微信授权")
            TrailMateWechatAuthCodeResult.Pending ->
                TrailMateAuthActionResult.InvalidInput("已打开微信授权，完成后返回 TrailMate")
            TrailMateWechatAuthCodeResult.StateMismatch ->
                TrailMateAuthActionResult.InvalidInput("微信授权已过期，请重新登录")
            TrailMateWechatAuthCodeResult.Unavailable ->
                TrailMateAuthActionResult.Failure(
                    code = "WECHAT_UNAVAILABLE",
                    message = "当前设备无法发起微信授权",
                    traceId = null
                )
        }
}

interface TrailMateWechatAuthCodeProvider {
    fun requestAuthCode(): TrailMateWechatAuthCodeResult

    fun consumeAuthCode(): TrailMateWechatAuthCodeResult? = null
}

sealed interface TrailMateWechatAuthCodeResult {
    data class Success(
        val authCode: String,
        val state: String
    ) : TrailMateWechatAuthCodeResult

    data object Cancelled : TrailMateWechatAuthCodeResult
    data object Pending : TrailMateWechatAuthCodeResult
    data object StateMismatch : TrailMateWechatAuthCodeResult
    data object Unavailable : TrailMateWechatAuthCodeResult
}
