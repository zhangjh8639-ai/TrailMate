package com.trailmate.server.auth;

public record AuthSessionResponse(
    String userId,
    AuthProvider provider,
    String accessToken,
    String refreshToken,
    String expiresAt,
    String phoneNumber,
    String wechatOpenId,
    String displayName
) {
}
