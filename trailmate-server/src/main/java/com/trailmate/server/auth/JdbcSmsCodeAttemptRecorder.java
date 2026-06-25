package com.trailmate.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcSmsCodeAttemptRecorder implements SmsCodeAttemptRecorder {
    private final JdbcTemplate jdbcTemplate;

    public JdbcSmsCodeAttemptRecorder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void record(SmsCodeAttempt attempt) {
        jdbcTemplate.update(
            """
                insert into auth_sms_code_attempt (
                    id, phone_e164, scene, provider, delivery_status,
                    failure_code, failure_message, request_ip_hash, created_at, expires_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            UUID.randomUUID().toString(),
            attempt.phoneNumber(),
            attempt.scene(),
            attempt.provider(),
            attempt.deliveryStatus(),
            attempt.failureCode(),
            attempt.failureMessage(),
            hashOrNull(attempt.clientAddress()),
            Timestamp.from(attempt.createdAt()),
            Timestamp.from(attempt.expiresAt())
        );
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
