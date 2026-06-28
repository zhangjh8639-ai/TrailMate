package com.trailmate.app.core.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateWechatTextSharePolicyTest {
    @Test
    fun readyWechatUsesWechatAsPreferredChannel() {
        val decision = TrailMateWechatTextSharePolicy.resolve(
            appIdConfigured = true,
            wechatInstalled = true,
            text = "TrailMate 安全分享"
        )

        assertEquals(TrailMateTextShareChannel.WECHAT, decision.preferredChannel)
        assertEquals("发送到微信", decision.primaryActionLabel)
        assertFalse(decision.requiresSystemFallback)
    }

    @Test
    fun missingAppIdFallsBackToSystemShare() {
        val decision = TrailMateWechatTextSharePolicy.resolve(
            appIdConfigured = false,
            wechatInstalled = true,
            text = "TrailMate 安全分享"
        )

        assertEquals(TrailMateTextShareChannel.SYSTEM, decision.preferredChannel)
        assertEquals("微信未配置", decision.statusLabel)
        assertTrue(decision.requiresSystemFallback)
    }

    @Test
    fun missingWechatFallsBackToSystemShare() {
        val decision = TrailMateWechatTextSharePolicy.resolve(
            appIdConfigured = true,
            wechatInstalled = false,
            text = "TrailMate 安全分享"
        )

        assertEquals(TrailMateTextShareChannel.SYSTEM, decision.preferredChannel)
        assertEquals("未安装微信", decision.statusLabel)
        assertTrue(decision.requiresSystemFallback)
    }

    @Test
    fun blankTextFallsBackToSystemShare() {
        val decision = TrailMateWechatTextSharePolicy.resolve(
            appIdConfigured = true,
            wechatInstalled = true,
            text = "   "
        )

        assertEquals(TrailMateTextShareChannel.SYSTEM, decision.preferredChannel)
        assertEquals("内容为空", decision.statusLabel)
        assertTrue(decision.requiresSystemFallback)
    }

    @Test
    fun failedWechatSendRequiresSystemFallback() {
        val decision = TrailMateWechatTextSharePolicy.resolve(
            appIdConfigured = true,
            wechatInstalled = true,
            text = "TrailMate 安全分享",
            lastSendStatus = TrailMateWechatTextShareSendStatus.REQUEST_REJECTED
        )

        assertEquals(TrailMateTextShareChannel.SYSTEM, decision.preferredChannel)
        assertEquals("微信发送未确认", decision.statusLabel)
        assertTrue(decision.requiresSystemFallback)
        assertTrue(decision.caption.contains("系统分享"))
    }

    @Test
    fun manualShareCopyDoesNotPromiseDeliveryOrTracking() {
        val decision = TrailMateWechatTextSharePolicy.resolve(
            appIdConfigured = true,
            wechatInstalled = true,
            text = "TrailMate 安全分享"
        )
        val copy = listOf(
            decision.primaryActionLabel,
            decision.statusLabel,
            decision.caption
        ).joinToString(separator = " ")

        assertTrue(copy.contains("手动"))
        assertFalse(copy.contains("已送达"))
        assertFalse(copy.contains("实时跟踪"))
    }
}
