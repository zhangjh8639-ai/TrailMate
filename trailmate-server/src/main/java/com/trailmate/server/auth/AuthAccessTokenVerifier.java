package com.trailmate.server.auth;

import java.time.Instant;

public interface AuthAccessTokenVerifier {
    AuthenticatedUser verifyAccessToken(String accessToken, Instant now);
}
