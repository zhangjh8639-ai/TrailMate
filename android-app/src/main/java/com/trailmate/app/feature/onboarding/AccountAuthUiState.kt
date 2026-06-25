package com.trailmate.app.feature.onboarding

enum class AccountAuthMethod(val label: String) {
    PHONE("手机号"),
    WECHAT("微信");

    companion object {
        fun fromLabel(label: String, wechatAvailable: Boolean = true): AccountAuthMethod {
            val requested = entries.firstOrNull { method -> method.label == label }
                ?: defaultAccountAuthMethod(wechatAvailable = wechatAvailable)
            return if (requested == WECHAT && !wechatAvailable) {
                PHONE
            } else {
                requested
            }
        }
    }
}

fun defaultAccountAuthMethod(wechatAvailable: Boolean = true): AccountAuthMethod =
    if (wechatAvailable) AccountAuthMethod.WECHAT else AccountAuthMethod.PHONE

enum class AccountAuthPhase {
    IDLE,
    PROCESSING,
    WAITING_WECHAT_CALLBACK
}

data class AccountAuthUiState(
    val method: AccountAuthMethod,
    val phase: AccountAuthPhase,
    val message: String,
    val codeRequested: Boolean = false,
    val wechatAvailable: Boolean = true
) {
    val primaryActionLabel: String
        get() = when {
            method == AccountAuthMethod.WECHAT && phase == AccountAuthPhase.WAITING_WECHAT_CALLBACK ->
                "等待微信授权返回"
            method == AccountAuthMethod.WECHAT && phase == AccountAuthPhase.PROCESSING ->
                "正在打开微信..."
            method == AccountAuthMethod.WECHAT ->
                "微信登录 / 注册"
            phase == AccountAuthPhase.PROCESSING ->
                "正在登录..."
            else ->
                "手机号登录 / 注册"
        }

    val phoneCodeActionLabel: String
        get() = when {
            phase == AccountAuthPhase.PROCESSING -> "处理中"
            codeRequested -> "重新获取"
            else -> "获取验证码"
        }

    val canSubmitWechat: Boolean
        get() = wechatAvailable && method == AccountAuthMethod.WECHAT && phase == AccountAuthPhase.IDLE

    val shouldConsumeWechatCallbackOnResume: Boolean
        get() = wechatAvailable &&
            method == AccountAuthMethod.WECHAT &&
            phase == AccountAuthPhase.WAITING_WECHAT_CALLBACK

    val canChangeMethod: Boolean
        get() = phase == AccountAuthPhase.IDLE

    fun withMethod(nextMethod: AccountAuthMethod): AccountAuthUiState {
        val resolvedMethod = if (nextMethod == AccountAuthMethod.WECHAT && !wechatAvailable) {
            AccountAuthMethod.PHONE
        } else {
            nextMethod
        }
        return copy(
            method = resolvedMethod,
            phase = AccountAuthPhase.IDLE,
            message = accountAuthMessageFor(resolvedMethod, wechatAvailable = wechatAvailable),
            codeRequested = false
        )
    }

    fun processing(message: String): AccountAuthUiState =
        copy(phase = AccountAuthPhase.PROCESSING, message = message)

    fun idle(message: String, codeRequested: Boolean = this.codeRequested): AccountAuthUiState =
        copy(phase = AccountAuthPhase.IDLE, message = message, codeRequested = codeRequested)

    fun waitingForWechatCallback(): AccountAuthUiState =
        copy(
            method = AccountAuthMethod.WECHAT,
            phase = AccountAuthPhase.WAITING_WECHAT_CALLBACK,
            message = "已打开微信授权，完成后返回 TrailMate。"
        )

    fun resumeWithoutWechatCallback(): AccountAuthUiState =
        if (phase == AccountAuthPhase.WAITING_WECHAT_CALLBACK) {
            copy(
                phase = AccountAuthPhase.IDLE,
                message = "未收到微信授权结果，可以重新发起。"
            )
        } else {
            this
        }

    fun withWechatAvailability(available: Boolean): AccountAuthUiState {
        val resolvedMethod = when {
            available -> AccountAuthMethod.WECHAT
            method == AccountAuthMethod.WECHAT -> AccountAuthMethod.PHONE
            else -> method
        }
        return copy(
            method = resolvedMethod,
            phase = AccountAuthPhase.IDLE,
            message = accountAuthMessageFor(resolvedMethod, wechatAvailable = available),
            codeRequested = if (available) false else codeRequested,
            wechatAvailable = available
        )
    }

    companion object {
        fun initial(
            method: AccountAuthMethod = defaultAccountAuthMethod(),
            wechatAvailable: Boolean = true
        ): AccountAuthUiState {
            val resolvedMethod = if (method == AccountAuthMethod.WECHAT && !wechatAvailable) {
                AccountAuthMethod.PHONE
            } else {
                method
            }
            return AccountAuthUiState(
                method = resolvedMethod,
                phase = AccountAuthPhase.IDLE,
                message = accountAuthMessageFor(resolvedMethod, wechatAvailable = wechatAvailable),
                wechatAvailable = wechatAvailable
            )
        }
    }
}

fun accountAuthMethodLabels(wechatAvailable: Boolean): List<String> =
    if (wechatAvailable) {
        listOf(AccountAuthMethod.WECHAT.label)
    } else {
        listOf(AccountAuthMethod.PHONE.label)
    }

fun accountAuthMessageFor(method: AccountAuthMethod, wechatAvailable: Boolean = true): String =
    when (method) {
        AccountAuthMethod.PHONE -> "当前构建未配置微信登录，先使用手机号登录。"
        AccountAuthMethod.WECHAT -> "使用微信登录或注册，授权后自动回到 TrailMate。"
    }
