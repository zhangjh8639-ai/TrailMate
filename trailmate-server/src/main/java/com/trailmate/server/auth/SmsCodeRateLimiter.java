package com.trailmate.server.auth;

import java.time.Instant;

public interface SmsCodeRateLimiter {
    boolean tryAcquire(String phoneNumber, String clientAddress, Instant now);
}
