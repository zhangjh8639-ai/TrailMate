package com.trailmate.server.auth;

import java.time.Instant;

public interface AuthSessionIssuer {
    AuthSessionResponse issueSession(AuthAccount account, AuthProvider provider, Instant now);

    AuthSessionResponse refreshSession(String refreshToken, Instant now);

    void revokeRefreshToken(String refreshToken, Instant now);
}
