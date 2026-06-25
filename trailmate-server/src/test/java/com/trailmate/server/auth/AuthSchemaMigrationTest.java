package com.trailmate.server.auth;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSchemaMigrationTest {
    private static final Path MIGRATION = Path.of(
        "src/main/resources/db/migration/V1__create_auth_schema.sql"
    );

    @Test
    void migrationCreatesAuthSourceOfTruthTables() throws Exception {
        assertTrue(Files.exists(MIGRATION), "Auth schema migration must exist.");
        String sql = Files.readString(MIGRATION);

        for (String table : List.of(
            "app_user",
            "user_phone_identity",
            "user_wechat_identity",
            "auth_refresh_token",
            "auth_sms_code_attempt",
            "user_consent",
            "auth_audit_event"
        )) {
            assertTrue(sql.contains("create table " + table), "Missing table: " + table);
        }
    }

    @Test
    void migrationProtectsAccountIdentityUniqueness() throws Exception {
        assertTrue(Files.exists(MIGRATION), "Auth schema migration must exist.");
        String sql = Files.readString(MIGRATION);

        assertTrue(sql.contains("constraint uq_user_phone_identity_phone unique (phone_e164)"));
        assertTrue(sql.contains("constraint uq_user_wechat_identity_openid unique (app_id, open_id)"));
        assertTrue(sql.contains("create unique index uq_user_wechat_identity_unionid"));
        assertTrue(sql.contains("constraint uq_auth_refresh_token_hash unique (token_hash)"));
        assertTrue(sql.contains("provider text not null"));
        assertTrue(sql.contains("check (provider in ('phone', 'wechat'))"));
    }

    @Test
    void migrationUsesTextIdsForApplicationLevelIdentifiers() throws Exception {
        assertTrue(Files.exists(MIGRATION), "Auth schema migration must exist.");
        String sql = Files.readString(MIGRATION);

        assertTrue(sql.contains("id text primary key"));
        assertTrue(sql.contains("user_id text not null references app_user(id)"));
        assertFalse(sql.contains("id uuid primary key"));
    }

    @Test
    void migrationKeepsSensitiveShortLivedSecretsOutOfDurableTables() throws Exception {
        assertTrue(Files.exists(MIGRATION), "Auth schema migration must exist.");
        String sql = Files.readString(MIGRATION);

        assertFalse(sql.contains("sms_code text"), "Plain SMS codes must not be durable columns.");
        assertFalse(sql.contains("refresh_token text"), "Plain refresh tokens must not be durable columns.");
        assertTrue(sql.contains("token_hash text not null"));
        assertTrue(sql.contains("phone_e164_hash text"));
        assertTrue(sql.contains("wechat_open_id_hash text"));
    }
}
