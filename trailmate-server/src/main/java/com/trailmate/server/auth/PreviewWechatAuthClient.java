package com.trailmate.server.auth;

import org.springframework.stereotype.Component;

@Component
public class PreviewWechatAuthClient implements WechatAuthClient {
    @Override
    public WechatIdentity exchangeAuthCode(String authCode, String state) {
        return new WechatIdentity("wx_openid_preview", null, "微信用户");
    }
}
