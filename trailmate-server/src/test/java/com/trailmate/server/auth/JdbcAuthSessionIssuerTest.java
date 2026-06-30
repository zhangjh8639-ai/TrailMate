package com.trailmate.server.auth;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcAuthSessionIssuerTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T10:00:00Z"), ZoneOffset.UTC);
    private JdbcTemplate jdbcTemplate;
    private JdbcAuthSessionIssuer issuer;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:trailmate_auth_session;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();
        insertPhoneAccount();
        issuer = new JdbcAuthSessionIssuer(
            jdbcTemplate,
            new SequentialTokenGenerator(List.of(
                "access-1",
                "refresh-1",
                "access-2",
                "refresh-2"
            )),
            new SequentialTokenIdGenerator(List.of(
                "token-row-1",
                "family-1",
                "token-row-2"
            ))
        );
    }

    @Test
    void issueSessionStoresOnlyRefreshTokenHash() {
        AuthSessionResponse session = issuer.issueSession(
            new AuthAccount("usr_1", "+8613800138000", null, null),
            AuthProvider.PHONE,
            clock.instant()
        );

        assertEquals("access-1", session.accessToken());
        assertEquals("refresh-1", session.refreshToken());
        assertNotEquals("access-1", storedAccessTokenHash("token-row-1-access"));
        assertNotEquals("refresh-1", storedTokenHash("token-row-1"));
        assertEquals("phone", storedProvider("token-row-1"));
    }

    @Test
    void verifyAccessTokenReturnsStoredUserOnlyBeforeExpiry() {
        AuthSessionResponse session = issuer.issueSession(
            new AuthAccount("usr_1", "+8613800138000", null, null),
            AuthProvider.PHONE,
            clock.instant()
        );

        AuthenticatedUser user = issuer.verifyAccessToken(session.accessToken(), clock.instant().plusSeconds(60));

        assertEquals("usr_1", user.userId());
        assertThrows(IllegalArgumentException.class, () ->
            issuer.verifyAccessToken(session.accessToken(), clock.instant().plusSeconds(7201))
        );
    }

    @Test
    void refreshSessionRotatesTokenAndRejectsReusingOldToken() {
        AuthSessionResponse first = issuer.issueSession(
            new AuthAccount("usr_1", "+8613800138000", null, null),
            AuthProvider.PHONE,
            clock.instant()
        );

        AuthSessionResponse rotated = issuer.refreshSession(first.refreshToken(), clock.instant().plusSeconds(30));

        assertEquals("access-2", rotated.accessToken());
        assertEquals("refresh-2", rotated.refreshToken());
        assertEquals("token-row-1", previousTokenId("token-row-2"));
        assertEquals(1, countRotatedTokens());
        assertThrows(IllegalArgumentException.class, () ->
            issuer.refreshSession(first.refreshToken(), clock.instant().plusSeconds(60))
        );
    }

    @Test
    void revokeRefreshTokenPreventsFutureRefresh() {
        AuthSessionResponse session = issuer.issueSession(
            new AuthAccount("usr_1", "+8613800138000", null, null),
            AuthProvider.PHONE,
            clock.instant()
        );

        issuer.revokeRefreshToken(session.refreshToken(), clock.instant().plusSeconds(30));

        assertEquals(1, countRevokedTokens());
        assertThrows(IllegalArgumentException.class, () ->
            issuer.refreshSession(session.refreshToken(), clock.instant().plusSeconds(60))
        );
    }

    private String storedTokenHash(String id) {
        return jdbcTemplate.queryForObject(
            "select token_hash from auth_refresh_token where id = ?",
            String.class,
            id
        );
    }

    private String storedAccessTokenHash(String id) {
        return jdbcTemplate.queryForObject(
            "select token_hash from auth_access_token where id = ?",
            String.class,
            id
        );
    }

    private String storedProvider(String id) {
        return jdbcTemplate.queryForObject(
            "select provider from auth_refresh_token where id = ?",
            String.class,
            id
        );
    }

    private String previousTokenId(String id) {
        return jdbcTemplate.queryForObject(
            "select previous_token_id from auth_refresh_token where id = ?",
            String.class,
            id
        );
    }

    private int countRotatedTokens() {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from auth_refresh_token where rotated_at is not null",
            Integer.class
        );
        return count == null ? 0 : count;
    }

    private int countRevokedTokens() {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from auth_refresh_token where revoked_at is not null",
            Integer.class
        );
        return count == null ? 0 : count;
    }

    private void insertPhoneAccount() {
        jdbcTemplate.update(
            """
                insert into app_user (
                    id, status, display_name, locale, timezone,
                    onboarding_status, created_at, updated_at
                ) values ('usr_1', 'active', null, 'zh-CN', 'Asia/Shanghai', 'account_created', ?, ?)
                """,
            java.sql.Timestamp.from(clock.instant()),
            java.sql.Timestamp.from(clock.instant())
        );
        jdbcTemplate.update(
            """
                insert into user_phone_identity (
                    id, user_id, phone_e164, verified_at, created_at, updated_at
                ) values ('phone_1', 'usr_1', '+8613800138000', ?, ?, ?)
                """,
            java.sql.Timestamp.from(clock.instant()),
            java.sql.Timestamp.from(clock.instant()),
            java.sql.Timestamp.from(clock.instant())
        );
    }

    private void createSchema() {
        jdbcTemplate.execute("drop table if exists auth_access_token");
        jdbcTemplate.execute("drop table if exists auth_refresh_token");
        jdbcTemplate.execute("drop table if exists user_phone_identity");
        jdbcTemplate.execute("drop table if exists app_user");
        jdbcTemplate.execute("""
            create table app_user (
                id varchar(80) primary key,
                status varchar(20) not null default 'active',
                display_name varchar(120),
                locale varchar(20) not null default 'zh-CN',
                timezone varchar(80) not null default 'Asia/Shanghai',
                onboarding_status varchar(40) not null default 'account_created',
                created_at timestamp not null,
                updated_at timestamp not null
            )
            """);
        jdbcTemplate.execute("""
            create table user_phone_identity (
                id varchar(80) primary key,
                user_id varchar(80) not null references app_user(id),
                phone_e164 varchar(32) not null unique,
                verified_at timestamp not null,
                created_at timestamp not null,
                updated_at timestamp not null,
                revoked_at timestamp
            )
            """);
        jdbcTemplate.execute("""
            create table auth_access_token (
                id varchar(80) primary key,
                user_id varchar(80) not null references app_user(id),
                provider varchar(20) not null,
                token_hash varchar(128) not null unique,
                issued_at timestamp not null,
                expires_at timestamp not null,
                revoked_at timestamp
            )
            """);
        jdbcTemplate.execute("""
            create table auth_refresh_token (
                id varchar(80) primary key,
                user_id varchar(80) not null references app_user(id),
                provider varchar(20) not null,
                token_hash varchar(128) not null unique,
                token_family_id varchar(80) not null,
                previous_token_id varchar(80),
                issued_at timestamp not null,
                expires_at timestamp not null,
                rotated_at timestamp,
                revoked_at timestamp,
                revoke_reason varchar(80),
                last_used_at timestamp
            )
            """);
    }

    private static final class SequentialTokenGenerator implements AuthTokenGenerator {
        private final List<String> tokens;
        private int index = 0;

        private SequentialTokenGenerator(List<String> tokens) {
            this.tokens = tokens;
        }

        @Override
        public String nextToken(String prefix) {
            return tokens.get(index++);
        }
    }

    private static final class SequentialTokenIdGenerator implements AuthTokenIdGenerator {
        private final List<String> ids;
        private int index = 0;

        private SequentialTokenIdGenerator(List<String> ids) {
            this.ids = ids;
        }

        @Override
        public String nextId() {
            return ids.get(index++);
        }
    }
}
