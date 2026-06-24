package com.trailmate.server.auth;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcSmsCodeAttemptRecorderTest {
    private JdbcTemplate jdbcTemplate;
    private JdbcSmsCodeAttemptRecorder recorder;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:trailmate_sms_attempt;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();
        recorder = new JdbcSmsCodeAttemptRecorder(jdbcTemplate);
    }

    @Test
    void recordStoresSmsDeliveryAttemptWithoutPlainIp() {
        recorder.record(new SmsCodeAttempt(
            "+8613800138000",
            "login_or_register",
            "noop",
            "sent",
            null,
            null,
            "203.0.113.8",
            Instant.parse("2026-06-22T10:00:00Z"),
            Instant.parse("2026-06-22T10:05:00Z")
        ));

        assertThat(countRows()).isEqualTo(1);
        assertThat(stringColumn("phone_e164")).isEqualTo("+8613800138000");
        assertThat(stringColumn("scene")).isEqualTo("login_or_register");
        assertThat(stringColumn("provider")).isEqualTo("noop");
        assertThat(stringColumn("delivery_status")).isEqualTo("sent");
        assertThat(stringColumn("request_ip_hash")).hasSize(64);
        assertThat(stringColumn("request_ip_hash")).doesNotContain("203.0.113.8");
    }

    @Test
    void recordStoresDeliveryFailureReason() {
        recorder.record(new SmsCodeAttempt(
            "+8613800138000",
            "login_or_register",
            "noop",
            "failed",
            "sender_exception",
            "provider unavailable",
            "203.0.113.8",
            Instant.parse("2026-06-22T10:00:00Z"),
            Instant.parse("2026-06-22T10:05:00Z")
        ));

        assertThat(stringColumn("delivery_status")).isEqualTo("failed");
        assertThat(stringColumn("failure_code")).isEqualTo("sender_exception");
        assertThat(stringColumn("failure_message")).isEqualTo("provider unavailable");
    }

    private int countRows() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from auth_sms_code_attempt", Integer.class);
        return count == null ? 0 : count;
    }

    private String stringColumn(String column) {
        return jdbcTemplate.queryForObject("select " + column + " from auth_sms_code_attempt", String.class);
    }

    private void createSchema() {
        jdbcTemplate.execute("drop table if exists auth_sms_code_attempt");
        jdbcTemplate.execute("""
            create table auth_sms_code_attempt (
                id varchar(80) primary key,
                phone_e164 varchar(32) not null,
                scene varchar(40) not null,
                provider varchar(80) not null,
                delivery_status varchar(20) not null,
                failure_code varchar(120),
                failure_message varchar(500),
                request_ip_hash varchar(128),
                device_id varchar(120),
                created_at timestamp not null,
                expires_at timestamp not null
            )
            """);
    }
}
