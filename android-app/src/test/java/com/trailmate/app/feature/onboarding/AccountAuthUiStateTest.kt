package com.trailmate.app.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountAuthUiStateTest {
    @Test
    fun defaultAuthMethodPrioritizesWechat() {
        assertEquals(AccountAuthMethod.WECHAT, defaultAccountAuthMethod())
        assertEquals("使用微信登录或注册，授权后自动回到 TrailMate。", accountAuthMessageFor(defaultAccountAuthMethod()))
        assertEquals(listOf("微信"), accountAuthMethodLabels(wechatAvailable = true))
    }

    @Test
    fun defaultAuthMethodFallsBackToPhoneWhenWechatIsUnavailable() {
        assertEquals(AccountAuthMethod.PHONE, defaultAccountAuthMethod(wechatAvailable = false))
        assertEquals(listOf("手机号"), accountAuthMethodLabels(wechatAvailable = false))
        assertEquals(
            "当前构建未配置微信登录，先使用手机号登录。",
            AccountAuthUiState.initial(wechatAvailable = false).message
        )
    }

    @Test
    fun restoredWechatAvailabilityMovesBackToWechatOnlyPath() {
        val state = AccountAuthUiState.initial(wechatAvailable = false)
            .idle("验证码已发送", codeRequested = true)
            .withWechatAvailability(true)

        assertEquals(AccountAuthMethod.WECHAT, state.method)
        assertEquals("使用微信登录或注册，授权后自动回到 TrailMate。", state.message)
        assertEquals(false, state.codeRequested)
    }

    @Test
    fun wechatUnavailableStateDisablesWechatSubmit() {
        val state = AccountAuthUiState.initial(wechatAvailable = false)
            .withMethod(AccountAuthMethod.WECHAT)

        assertEquals(AccountAuthMethod.PHONE, state.method)
        assertEquals(false, state.canSubmitWechat)
        assertEquals("手机号登录 / 注册", state.primaryActionLabel)
    }

    @Test
    fun phoneMethodRemainsOnlyAsWechatUnavailableFallback() {
        assertEquals(AccountAuthMethod.PHONE, AccountAuthMethod.fromLabel("手机号", wechatAvailable = false))
        assertEquals(
            "当前构建未配置微信登录，先使用手机号登录。",
            accountAuthMessageFor(AccountAuthMethod.PHONE, wechatAvailable = false)
        )
    }

    @Test
    fun wechatWaitingStateDisablesPrimaryAction() {
        val state = AccountAuthUiState.initial()
            .waitingForWechatCallback()

        assertEquals(AccountAuthMethod.WECHAT, state.method)
        assertEquals(AccountAuthPhase.WAITING_WECHAT_CALLBACK, state.phase)
        assertEquals("等待微信授权返回", state.primaryActionLabel)
        assertEquals(false, state.canSubmitWechat)
        assertEquals("已打开微信授权，完成后返回 TrailMate。", state.message)
    }

    @Test
    fun returningWithoutWechatCallbackAllowsRetry() {
        val state = AccountAuthUiState.initial()
            .waitingForWechatCallback()
            .resumeWithoutWechatCallback()

        assertEquals(AccountAuthPhase.IDLE, state.phase)
        assertEquals("微信登录 / 注册", state.primaryActionLabel)
        assertEquals(true, state.canSubmitWechat)
        assertEquals("未收到微信授权结果，可以重新发起。", state.message)
    }

    @Test
    fun resumeCallbackCheckRunsOnlyAfterWechatWasOpened() {
        assertEquals(false, AccountAuthUiState.initial().shouldConsumeWechatCallbackOnResume)
        assertEquals(
            true,
            AccountAuthUiState.initial()
                .waitingForWechatCallback()
                .shouldConsumeWechatCallbackOnResume
        )
    }

    @Test
    fun methodSwitchingIsLockedDuringAuthProgress() {
        assertEquals(true, AccountAuthUiState.initial().canChangeMethod)
        assertEquals(false, AccountAuthUiState.initial().processing("正在打开微信...").canChangeMethod)
        assertEquals(false, AccountAuthUiState.initial().waitingForWechatCallback().canChangeMethod)
    }
}
