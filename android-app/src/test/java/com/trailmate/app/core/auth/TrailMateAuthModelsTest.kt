package com.trailmate.app.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateAuthModelsTest {
    @Test
    fun mainlandPhoneInputNormalizesToE164() {
        assertEquals("+8613800138000", TrailMatePhoneNumber.normalizeMainlandChina("138 0013 8000"))
        assertEquals("+8613800138000", TrailMatePhoneNumber.normalizeMainlandChina("+86 138-0013-8000"))
    }

    @Test
    fun invalidPhoneInputIsRejectedBeforeRequestingCode() {
        assertNull(TrailMatePhoneNumber.normalizeMainlandChina("12345"))
        assertNull(TrailMatePhoneNumber.normalizeMainlandChina("23800138000"))
        assertNull(TrailMatePhoneNumber.normalizeMainlandChina(""))
    }

    @Test
    fun smsCodeMustBeFourToEightDigits() {
        assertTrue(TrailMateSmsCode.isValid("1234"))
        assertTrue(TrailMateSmsCode.isValid("12345678"))
        assertEquals(false, TrailMateSmsCode.isValid("12a4"))
        assertEquals(false, TrailMateSmsCode.isValid("123"))
        assertEquals(false, TrailMateSmsCode.isValid("123456789"))
    }

    @Test
    fun authSessionCapturesProviderAndSafeIdentityLabel() {
        val phoneSession = TrailMateAuthSession(
            userId = "usr-phone",
            provider = TrailMateAuthProvider.PHONE,
            accessToken = "access",
            refreshToken = "refresh",
            expiresAt = "2026-06-22T12:00:00Z",
            phoneNumber = "+8613800138000",
            wechatOpenId = null,
            displayName = null
        )
        val wechatSession = TrailMateAuthSession(
            userId = "usr-wechat",
            provider = TrailMateAuthProvider.WECHAT,
            accessToken = "access",
            refreshToken = "refresh",
            expiresAt = "2026-06-22T12:00:00Z",
            phoneNumber = null,
            wechatOpenId = "wx-open-id",
            displayName = "张三"
        )

        assertEquals("手机用户 138****8000", phoneSession.safeIdentityLabel())
        assertEquals("微信用户 张三", wechatSession.safeIdentityLabel())
    }
}
