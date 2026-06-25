package com.trailmate.server.auth;

public record AuthAccount(
    String userId,
    String phoneNumber,
    String wechatOpenId,
    String displayName
) {
}
