package com.trailmate.server.auth;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "trailmate.auth.persistence", name = "mode", havingValue = "memory", matchIfMissing = true)
public class RandomAuthSessionIssuer implements AuthSessionIssuer {
    private static final long ACCESS_TOKEN_TTL_SECONDS = 7200;
    private final ConcurrentHashMap<String, RefreshSessionState> refreshSessions = new ConcurrentHashMap<>();

    @Override
    public AuthSessionResponse issueSession(AuthAccount account, AuthProvider provider, Instant now) {
        String refreshToken = token("refresh");
        AuthSessionResponse response = new AuthSessionResponse(
            account.userId(),
            provider,
            token("access"),
            refreshToken,
            now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS).toString(),
            account.phoneNumber(),
            account.wechatOpenId(),
            account.displayName()
        );
        refreshSessions.put(refreshToken, new RefreshSessionState(account, provider));
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

    private String token(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }

    private record RefreshSessionState(AuthAccount account, AuthProvider provider) {
    }
}
