package com.trailmate.app.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrailMateWechatAuthCallbackStoreTest {
    @Test
    fun consumeReturnsAndClearsLatestWechatCallback() {
        val store = TrailMateWechatAuthCallbackStore()

        store.publish(TrailMateWechatAuthCodeResult.Success("wx-code", "nonce"))

        assertEquals(TrailMateWechatAuthCodeResult.Success("wx-code", "nonce"), store.consume())
        assertNull(store.consume())
    }

    @Test
    fun pendingProviderSendsRequestWhenNoCallbackExists() {
        val store = TrailMateWechatAuthCallbackStore()
        val provider = TrailMateStoredWechatAuthCodeProvider(
            callbackStore = store,
            requestLauncher = FakeRequestLauncher(
                result = TrailMateWechatAuthLaunchResult.Launched("expected-state")
            )
        )

        assertEquals(TrailMateWechatAuthCodeResult.Pending, provider.requestAuthCode())
    }

    @Test
    fun providerReturnsMatchingStoredCallbackBeforeLaunchingNewRequest() {
        val store = TrailMateWechatAuthCallbackStore()
        val launcher = FakeRequestLauncher(
            result = TrailMateWechatAuthLaunchResult.Launched("expected-state")
        )
        val provider = TrailMateStoredWechatAuthCodeProvider(
            callbackStore = store,
            requestLauncher = launcher
        )
        assertEquals(TrailMateWechatAuthCodeResult.Pending, provider.requestAuthCode())
        store.publish(TrailMateWechatAuthCodeResult.Success("wx-code", "expected-state"))

        assertEquals(TrailMateWechatAuthCodeResult.Success("wx-code", "expected-state"), provider.consumeAuthCode())
        assertEquals(1, launcher.launchCount)
    }

    @Test
    fun providerRejectsStoredCallbackWhenStateDoesNotMatchLaunchState() {
        val store = TrailMateWechatAuthCallbackStore()
        val provider = TrailMateStoredWechatAuthCodeProvider(
            callbackStore = store,
            requestLauncher = FakeRequestLauncher(
                result = TrailMateWechatAuthLaunchResult.Launched("expected-state")
            )
        )

        assertEquals(TrailMateWechatAuthCodeResult.Pending, provider.requestAuthCode())
        store.publish(TrailMateWechatAuthCodeResult.Success("wx-code", "unexpected-state"))

        assertEquals(TrailMateWechatAuthCodeResult.StateMismatch, provider.consumeAuthCode())
    }

    @Test
    fun providerReturnsUnavailableWhenLauncherCannotOpenWechat() {
        val provider = TrailMateStoredWechatAuthCodeProvider(
            callbackStore = TrailMateWechatAuthCallbackStore(),
            requestLauncher = FakeRequestLauncher(TrailMateWechatAuthLaunchResult.Unavailable)
        )

        assertEquals(TrailMateWechatAuthCodeResult.Unavailable, provider.requestAuthCode())
    }

    private class FakeRequestLauncher(
        private val result: TrailMateWechatAuthLaunchResult
    ) : TrailMateWechatAuthRequestLauncher {
        var launchCount = 0

        override fun launchWechatAuth(): TrailMateWechatAuthLaunchResult {
            launchCount += 1
            return result
        }
    }
}
