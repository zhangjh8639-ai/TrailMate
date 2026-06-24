package com.trailmate.server.auth;

import java.time.Instant;

public interface SmsCodeCooldownRepository {
    boolean tryAcquire(String phoneNumber, Instant expiresAt);
}
