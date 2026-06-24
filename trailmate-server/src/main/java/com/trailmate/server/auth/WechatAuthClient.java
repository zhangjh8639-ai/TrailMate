package com.trailmate.server.auth;

public interface WechatAuthClient {
    WechatIdentity exchangeAuthCode(String authCode, String state);
}
