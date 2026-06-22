package com.trailmate.app.core.map

data class AmapPrivacyConsent(
    val accepted: Boolean = false,
    val acceptedAtEpochMillis: Long? = null,
    val policyVersion: String = CURRENT_POLICY_VERSION
) {
    companion object {
        const val CURRENT_POLICY_VERSION = "amap-privacy-v1"

        fun accepted(nowEpochMillis: Long): AmapPrivacyConsent =
            AmapPrivacyConsent(
                accepted = true,
                acceptedAtEpochMillis = nowEpochMillis,
                policyVersion = CURRENT_POLICY_VERSION
            )
    }
}
