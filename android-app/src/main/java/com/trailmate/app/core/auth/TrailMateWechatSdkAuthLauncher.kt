package com.trailmate.app.core.auth

import android.content.Context
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.util.UUID

class TrailMateWechatSdkAuthLauncher(
    context: Context,
    private val appId: String,
    private val stateFactory: () -> String = { "trailmate-${UUID.randomUUID()}" }
) : TrailMateWechatAuthRequestLauncher {
    private val api = WXAPIFactory.createWXAPI(context.applicationContext, appId, true)

    override fun launchWechatAuth(): TrailMateWechatAuthLaunchResult {
        if (appId.isBlank()) {
            return TrailMateWechatAuthLaunchResult.Unavailable
        }
        api.registerApp(appId)
        if (!api.isWXAppInstalled) {
            return TrailMateWechatAuthLaunchResult.Unavailable
        }

        val state = stateFactory()
        val request = SendAuth.Req().apply {
            scope = "snsapi_userinfo"
            this.state = state
        }
        return if (api.sendReq(request)) {
            TrailMateWechatAuthLaunchResult.Launched(state)
        } else {
            TrailMateWechatAuthLaunchResult.Unavailable
        }
    }
}
