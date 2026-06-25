package com.trailmate.server.auth;

public record PhoneCodeResponse(
    String phoneNumber,
    int expiresInSeconds,
    int retryAfterSeconds
) {
}
