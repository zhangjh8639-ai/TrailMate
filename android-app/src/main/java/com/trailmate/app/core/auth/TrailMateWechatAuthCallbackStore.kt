package com.trailmate.app.core.auth

class TrailMateWechatAuthCallbackStore {
    @Volatile
    private var latestResult: TrailMateWechatAuthCodeResult? = null

    fun publish(result: TrailMateWechatAuthCodeResult) {
        latestResult = result
    }

    fun consume(): TrailMateWechatAuthCodeResult? {
        val result = latestResult
        latestResult = null
        return result
    }
}

interface TrailMateWechatAuthRequestLauncher {
    fun launchWechatAuth(): TrailMateWechatAuthLaunchResult
}

sealed interface TrailMateWechatAuthLaunchResult {
    data class Launched(val state: String) : TrailMateWechatAuthLaunchResult
    data object Unavailable : TrailMateWechatAuthLaunchResult
}

class TrailMateStoredWechatAuthCodeProvider(
    private val callbackStore: TrailMateWechatAuthCallbackStore,
    private val requestLauncher: TrailMateWechatAuthRequestLauncher
) : TrailMateWechatAuthCodeProvider {
    private var pendingState: String? = null

    override fun requestAuthCode(): TrailMateWechatAuthCodeResult {
        consumeAuthCode()?.let { result -> return result }
        return when (val result = requestLauncher.launchWechatAuth()) {
            is TrailMateWechatAuthLaunchResult.Launched -> {
                pendingState = result.state
                TrailMateWechatAuthCodeResult.Pending
            }
            TrailMateWechatAuthLaunchResult.Unavailable -> TrailMateWechatAuthCodeResult.Unavailable
        }
    }

    override fun consumeAuthCode(): TrailMateWechatAuthCodeResult? =
        callbackStore.consume()?.let(::validateCallbackState)

    private fun validateCallbackState(
        result: TrailMateWechatAuthCodeResult
    ): TrailMateWechatAuthCodeResult =
        when (result) {
            is TrailMateWechatAuthCodeResult.Success -> {
                val expectedState = pendingState
                pendingState = null
                if (expectedState != null && result.state == expectedState) {
                    result
                } else {
                    TrailMateWechatAuthCodeResult.StateMismatch
                }
            }
            TrailMateWechatAuthCodeResult.Cancelled,
            TrailMateWechatAuthCodeResult.Unavailable -> {
                pendingState = null
                result
            }
            TrailMateWechatAuthCodeResult.Pending,
            TrailMateWechatAuthCodeResult.StateMismatch -> result
        }
}

object TrailMateGlobalWechatAuthCallbackStore {
    val store = TrailMateWechatAuthCallbackStore()
}
