package com.trailmate.server.auth;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "trailmate.auth.persistence", name = "mode", havingValue = "memory", matchIfMissing = true)
public class RandomAuthSessionIssuer implements AuthSessionIssuer, AuthAccessTokenVerifier {
    private static final long ACCESS_TOKEN_TTL_SECONDS = 7200;
    private final ConcurrentHashMap<String, RefreshSessionState> refreshSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AccessSessionState> accessSessions = new ConcurrentHashMap<>();

    @Override
    public AuthSessionResponse issueSession(AuthAccount account, AuthProvider provider, Instant now) {
        String accessToken = token("access");
        String refreshToken = token("refresh");
        AuthSessionResponse response = new AuthSessionResponse(
            account.userId(),
            provider,
            accessToken,
            refreshToken,
            now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS).toString(),
            account.phoneNumber(),
            account.wechatOpenId(),
            account.displayName()
        );
        refreshSessions.put(refreshToken, new RefreshSessionState(account, provider));
        accessSessions.put(accessToken, new AccessSessionState(account.userId(), now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS)));
        return response;
    }

    @Override
    public AuthSessionResponse refreshSession(String refreshToken, Instant now) {
        RefreshSessionState sessionState = refreshSessions.remove(refreshToken);
        if (sessionState == null) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }
        return issueSession(sessionState.account(), sessionState.provider(), now);
    }

    @Override
    public void revokeRefreshToken(String refreshToken, Instant now) {
        refreshSessions.remove(refreshToken);
    }

    @Override
    public AuthenticatedUser verifyAccessToken(String accessToken, Instant now) {
        AccessSessionState sessionState = accessSessions.get(accessToken);
        if (sessionState == null || !sessionState.expiresAt().isAfter(now)) {
            throw new IllegalArgumentException("Invalid access token.");
        }
        return new AuthenticatedUser(sessionState.userId());
    }

    private String token(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }

    private record RefreshSessionState(AuthAccount account, AuthProvider provider) {
    }

    private record AccessSessionState(String userId, Instant expiresAt) {
    }
}
