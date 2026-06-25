package com.trailmate.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcAuthAuditRecorder implements AuthAuditRecorder {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthAuditRecorder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void record(AuthAuditEvent event) {
        jdbcTemplate.update(
            """
                insert into auth_audit_event (
                    id, user_id, event_type, provider, outcome, reason_code,
                    phone_e164_hash, wechat_open_id_hash, ip_hash, created_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            UUID.randomUUID().toString(),
            event.userId(),
            event.eventType(),
            providerValue(event.provider()),
            event.outcome(),
            event.reasonCode(),
            hashOrNull(event.phoneNumber()),
            hashOrNull(event.wechatOpenId()),
            hashOrNull(event.clientAddress()),
            Timestamp.from(event.occurredAt())
        );
    }

    private static String providerValue(AuthProvider provider) {
        if (provider == null) {
            return null;
        }
        return switch (provider) {
            case PHONE -> "phone";
            case WECHAT -> "wechat";
        };
    }

    private static String hashOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
