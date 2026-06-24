package com.trailmate.server.auth;

import java.time.Instant;

public record SmsCodeAttempt(
    String phoneNumber,
    String scene,
    String provider,
    String deliveryStatus,
    String failureCode,
    String failureMessage,
    String clientAddress,
    Instant createdAt,
    Instant expiresAt
) {
}
