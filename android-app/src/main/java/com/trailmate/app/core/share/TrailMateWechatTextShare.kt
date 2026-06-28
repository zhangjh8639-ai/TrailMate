package com.trailmate.app.core.share

import android.content.Context
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.util.UUID

enum class TrailMateTextShareChannel {
    WECHAT,
    SYSTEM
}

enum class TrailMateWechatTextShareSendStatus {
    REQUEST_ACCEPTED,
    REQUEST_REJECTED,
    UNAVAILABLE
}

data class TrailMateWechatTextShareDecision(
    val preferredChannel: TrailMateTextShareChannel,
    val primaryActionLabel: String,
    val statusLabel: String,
    val caption: String,
    val requiresSystemFallback: Boolean
)

object TrailMateWechatTextSharePolicy {
    fun resolve(
        appIdConfigured: Boolean,
        wechatInstalled: Boolean,
        text: String,
        lastSendStatus: TrailMateWechatTextShareSendStatus? = null
    ): TrailMateWechatTextShareDecision {
        if (text.isBlank()) {
            return systemDecision(
                statusLabel = "内容为空",
                caption = "安全信息为空时不会发送，保留系统分享入口。"
            )
        }
        if (lastSendStatus == TrailMateWechatTextShareSendStatus.REQUEST_REJECTED) {
            return systemDecision(
                statusLabel = "微信发送未确认",
                caption = "微信未接受本次手动分享请求，已改用系统分享。"
            )
        }
        if (!appIdConfigured) {
            return systemDecision(
                statusLabel = "微信未配置",
                caption = "微信 AppID 未配置时使用系统分享，用户仍可手动选择发送渠道。"
            )
        }
        if (!wechatInstalled) {
            return systemDecision(
                statusLabel = "未安装微信",
                caption = "未检测到微信时使用系统分享，用户仍可手动选择发送渠道。"
            )
        }

        return TrailMateWechatTextShareDecision(
            preferredChannel = TrailMateTextShareChannel.WECHAT,
            primaryActionLabel = "发送到微信",
            statusLabel = "微信可用",
            caption = "将打开微信聊天会话，用户需要手动选择联系人并确认发送。",
            requiresSystemFallback = false
        )
    }

    private fun systemDecision(
        statusLabel: String,
        caption: String
    ): TrailMateWechatTextShareDecision =
        TrailMateWechatTextShareDecision(
            preferredChannel = TrailMateTextShareChannel.SYSTEM,
            primaryActionLabel = "系统分享",
            statusLabel = statusLabel,
            caption = caption,
            requiresSystemFallback = true
        )
}

class TrailMateWechatTextShareLauncher(
    context: Context,
    private val appId: String,
    private val transactionFactory: () -> String = { "trailmate-text-${UUID.randomUUID()}" }
) {
    private val api = WXAPIFactory.createWXAPI(context.applicationContext, appId, true)

    fun shareText(text: String): TrailMateWechatTextShareSendStatus {
        if (text.isBlank() || appId.isBlank()) {
            return TrailMateWechatTextShareSendStatus.UNAVAILABLE
        }

        api.registerApp(appId)
        if (!api.isWXAppInstalled) {
            return TrailMateWechatTextShareSendStatus.UNAVAILABLE
        }

        val textObject = WXTextObject().apply {
            this.text = text
        }
        val message = WXMediaMessage().apply {
            mediaObject = textObject
            description = text.take(512)
        }
        val request = SendMessageToWX.Req().apply {
            transaction = transactionFactory()
            this.message = message
            scene = SendMessageToWX.Req.WXSceneSession
        }

        return if (api.sendReq(request)) {
            TrailMateWechatTextShareSendStatus.REQUEST_ACCEPTED
        } else {
            TrailMateWechatTextShareSendStatus.REQUEST_REJECTED
        }
    }
}
