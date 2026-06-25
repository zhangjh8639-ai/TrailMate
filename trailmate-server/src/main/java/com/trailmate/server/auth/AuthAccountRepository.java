package com.trailmate.server.auth;

public interface AuthAccountRepository {
    AuthAccount findOrCreateByPhone(String phoneNumber);

    AuthAccount findOrCreateByWechat(String openId, String unionId, String displayName);
}
