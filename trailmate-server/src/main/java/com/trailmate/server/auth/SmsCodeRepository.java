package com.trailmate.server.auth;

import java.time.Instant;

public interface SmsCodeRepository {
    void save(String phoneNumber, String code, Instant expiresAt);

    boolean verifyAndConsume(String phoneNumber, String code, Instant now);
}
