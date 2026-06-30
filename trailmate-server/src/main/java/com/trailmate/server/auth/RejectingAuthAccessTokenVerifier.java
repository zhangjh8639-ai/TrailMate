package com.trailmate.server.auth;

import java.time.Instant;

public class RejectingAuthAccessTokenVerifier implements AuthAccessTokenVerifier {
    @Override
    public AuthenticatedUser verifyAccessToken(String accessToken, Instant now) {
        throw new IllegalArgumentException("Invalid access token.");
    }
}
