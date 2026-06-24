package com.trailmate.server.auth;

public record WechatIdentity(
    String openId,
    String unionId,
    String displayName
) {
}
