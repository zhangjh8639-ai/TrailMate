package com.trailmate.app.core.auth

enum class TrailMateAuthProvider {
    PHONE,
    WECHAT
}

data class TrailMateAuthSession(
    val userId: String,
    val provider: TrailMateAuthProvider,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String,
    val phoneNumber: String?,
    val wechatOpenId: String?,
    val displayName: String?
) {
    companion object {
        fun localPhoneSession(
            phoneNumber: String,
            nowEpochMillis: Long
        ): TrailMateAuthSession =
            TrailMateAuthSession(
                userId = "local-phone-${phoneNumber.takeLast(4)}",
                provider = TrailMateAuthProvider.PHONE,
                accessToken = "local-access-$nowEpochMillis",
                refreshToken = "local-refresh-$nowEpochMillis",
                expiresAt = LOCAL_SESSION_EXPIRES_AT,
                phoneNumber = phoneNumber,
                wechatOpenId = null,
                displayName = null
            )

        fun localWechatSession(nowEpochMillis: Long): TrailMateAuthSession =
            TrailMateAuthSession(
                userId = "local-wechat-$nowEpochMillis",
                provider = TrailMateAuthProvider.WECHAT,
                accessToken = "local-access-$nowEpochMillis",
                refreshToken = "local-refresh-$nowEpochMillis",
                expiresAt = LOCAL_SESSION_EXPIRES_AT,
                phoneNumber = null,
                wechatOpenId = "local-wechat-openid",
                displayName = "微信用户"
            )

        private const val LOCAL_SESSION_EXPIRES_AT = "2099-12-31T23:59:59Z"
    }

    fun safeIdentityLabel(): String =
        when (provider) {
            TrailMateAuthProvider.PHONE -> "手机用户 ${phoneNumber.maskMainlandPhone()}"
            TrailMateAuthProvider.WECHAT -> "微信用户 ${displayName.orEmpty().ifBlank { "已授权" }}"
        }

    private fun String?.maskMainlandPhone(): String {
        val digits = this.orEmpty().removePrefix("+86")
        return if (digits.length == 11) {
            "${digits.take(3)}****${digits.takeLast(4)}"
        } else {
            "已绑定"
        }
    }
}

object TrailMatePhoneNumber {
    private val MainlandMobilePattern = Regex("^1\\d{10}$")

    fun normalizeMainlandChina(input: String): String? {
        val compact = input
            .filter { char -> char.isDigit() || char == '+' }
            .removePrefix("+86")
            .removePrefix("86")

        return compact
            .takeIf { MainlandMobilePattern.matches(it) }
            ?.let { "+86$it" }
    }
}

object TrailMateSmsCode {
    private val Pattern = Regex("^\\d{4,8}$")

    fun isValid(input: String): Boolean =
        Pattern.matches(input.trim())
}
