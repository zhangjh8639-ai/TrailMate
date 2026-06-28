package com.trailmate.app.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.trailmate.app.BuildConfig
import com.trailmate.app.core.auth.TrailMateGlobalWechatAuthCallbackStore
import com.trailmate.app.core.auth.TrailMateWechatAuthCodeResult

class WXEntryActivity : Activity(), IWXAPIEventHandler {
    private val api by lazy {
        WXAPIFactory.createWXAPI(this, BuildConfig.TRAILMATE_WECHAT_APP_ID, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq?) = Unit

    override fun onResp(resp: BaseResp?) {
        if (resp == null) {
            TrailMateGlobalWechatAuthCallbackStore.store.publish(TrailMateWechatAuthCodeResult.Unavailable)
            finish()
            return
        }
        if (resp !is SendAuth.Resp || resp.type != ConstantsAPI.COMMAND_SENDAUTH) {
            finish()
            return
        }

        val result = when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK ->
                resp.code
                    ?.takeIf { code -> code.isNotBlank() }
                    ?.let { code ->
                        TrailMateWechatAuthCodeResult.Success(
                            authCode = code,
                            state = resp.state.orEmpty()
                        )
                    }
                    ?: TrailMateWechatAuthCodeResult.Unavailable
            BaseResp.ErrCode.ERR_USER_CANCEL -> TrailMateWechatAuthCodeResult.Cancelled
            else -> TrailMateWechatAuthCodeResult.Unavailable
        }
        TrailMateGlobalWechatAuthCallbackStore.store.publish(result)
        finish()
    }
}
