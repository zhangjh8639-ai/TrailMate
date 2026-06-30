package com.trailmate.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcAuthSessionIssuer implements AuthSessionIssuer, AuthAccessTokenVerifier {
    private static final long ACCESS_TOKEN_TTL_SECONDS = 7200;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 60L * 60L * 24L * 60L;
    private final JdbcTemplate jdbcTemplate;
    private final AuthTokenGenerator tokenGenerator;
    private final AuthTokenIdGenerator tokenIdGenerator;

    public JdbcAuthSessionIssuer(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new RandomAuthTokenGenerator(), new RandomAuthTokenGenerator());
    }

    public JdbcAuthSessionIssuer(
        JdbcTemplate jdbcTemplate,
        AuthTokenGenerator tokenGenerator,
        AuthTokenIdGenerator tokenIdGenerator
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.tokenGenerator = tokenGenerator;
        this.tokenIdGenerator = tokenIdGenerator;
    }

    @Override
    public AuthSessionResponse issueSession(AuthAccount account, AuthProvider provider, Instant now) {
        return issueSession(account, provider, now, tokenIdGenerator.nextId(), tokenIdGenerator.nextId(), null);
    }

    @Override
    public AuthSessionResponse refreshSession(String refreshToken, Instant now) {
        StoredRefreshToken storedToken = findActiveRefreshToken(refreshToken, now);
        jdbcTemplate.update(
            "update auth_refresh_token set rotated_at = ?, last_used_at = ? where id = ?",
            Timestamp.from(now),
            Timestamp.from(now),
            storedToken.id()
        );
        AuthAccount account = accountForRefreshToken(storedToken);
        return issueSession(
            account,
            storedToken.provider(),
            now,
            tokenIdGenerator.nextId(),
            storedToken.tokenFamilyId(),
            storedToken.id()
        );
    }

    @Override
    public void revokeRefreshToken(String refreshToken, Instant now) {
        jdbcTemplate.update(
            "update auth_refresh_token set revoked_at = ?, revoke_reason = 'logout' where token_hash = ? and revoked_at is null",
            Timestamp.from(now),
            hash(refreshToken)
        );
    }

    private AuthSessionResponse issueSession(
        AuthAccount account,
        AuthProvider provider,
        Instant now,
        String tokenId,
        String tokenFamilyId,
        String previousTokenId
    ) {
        String accessToken = tokenGenerator.nextToken("access");
        String refreshToken = tokenGenerator.nextToken("refresh");
        Instant accessTokenExpiresAt = now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS);
        jdbcTemplate.update(
            """
                insert into auth_access_token (
                    id, user_id, provider, token_hash, issued_at, expires_at
                ) values (?, ?, ?, ?, ?, ?)
                """,
            tokenId + "-access",
            account.userId(),
            providerValue(provider),
            hash(accessToken),
            Timestamp.from(now),
            Timestamp.from(accessTokenExpiresAt)
        );
        jdbcTemplate.update(
            """
                insert into auth_refresh_token (
                    id, user_id, provider, token_hash, token_family_id,
                    previous_token_id, issued_at, expires_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            tokenId,
            account.userId(),
            providerValue(provider),
            hash(refreshToken),
            tokenFamilyId,
            previousTokenId,
            Timestamp.from(now),
            Timestamp.from(now.plusSeconds(REFRESH_TOKEN_TTL_SECONDS))
        );

        return new AuthSessionResponse(
            account.userId(),
            provider,
            accessToken,
            refreshToken,
            accessTokenExpiresAt.toString(),
            account.phoneNumber(),
            account.wechatOpenId(),
            account.displayName()
        );
    }

    @Override
    public AuthenticatedUser verifyAccessToken(String accessToken, Instant now) {
        try {
            return jdbcTemplate.queryForObject(
                """
                    select t.user_id
                    from auth_access_token t
                    join app_user u on u.id = t.user_id
                    where t.token_hash = ?
                      and t.revoked_at is null
                      and t.expires_at > ?
                      and u.status = 'active'
                    """,
                (rs, rowNum) -> new AuthenticatedUser(rs.getString("user_id")),
                hash(accessToken),
                Timestamp.from(now)
            );
        } catch (EmptyResultDataAccessException ignored) {
            throw new IllegalArgumentException("Invalid access token.");
        }
    }

    private StoredRefreshToken findActiveRefreshToken(String refreshToken, Instant now) {
        try {
            return jdbcTemplate.queryForObject(
                """
                    select id, user_id, provider, token_family_id
                    from auth_refresh_token
                    where token_hash = ?
                      and rotated_at is null
                      and revoked_at is null
                      and expires_at > ?
                    """,
                (rs, rowNum) -> new StoredRefreshToken(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    providerFromValue(rs.getString("provider")),
                    rs.getString("token_family_id")
                ),
                hash(refreshToken),
                Timestamp.from(now)
            );
        } catch (EmptyResultDataAccessException ignored) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }
    }

    private AuthAccount accountForRefreshToken(StoredRefreshToken storedToken) {
        if (storedToken.provider() == AuthProvider.PHONE) {
            return jdbcTemplate.queryForObject(
                """
                    select u.id, p.phone_e164, u.display_name
                    from app_user u
                    join user_phone_identity p on p.user_id = u.id
                    where u.id = ?
                      and u.status = 'active'
                      and p.revoked_at is null
                    """,
                (rs, rowNum) -> new AuthAccount(
                    rs.getString("id"),
                    rs.getString("phone_e164"),
                    null,
                    rs.getString("display_name")
                ),
                storedToken.userId()
            );
        }

        return jdbcTemplate.queryForObject(
            """
                select u.id, w.open_id, w.nickname
                from app_user u
                join user_wechat_identity w on w.user_id = u.id
                where u.id = ?
                  and u.status = 'active'
                  and w.revoked_at is null
                """,
            (rs, rowNum) -> new AuthAccount(
                rs.getString("id"),
                null,
                rs.getString("open_id"),
                rs.getString("nickname")
            ),
            storedToken.userId()
        );
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for refresh token hashing.", exception);
        }
    }

    private String providerValue(AuthProvider provider) {
        return switch (provider) {
            case PHONE -> "phone";
            case WECHAT -> "wechat";
        };
    }

    private AuthProvider providerFromValue(String provider) {
        return switch (provider) {
            case "phone" -> AuthProvider.PHONE;
            case "wechat" -> AuthProvider.WECHAT;
            default -> throw new IllegalArgumentException("Unsupported auth provider.");
        };
    }

    private record StoredRefreshToken(
        String id,
        String userId,
        AuthProvider provider,
        String tokenFamilyId
    ) {
    }
}
