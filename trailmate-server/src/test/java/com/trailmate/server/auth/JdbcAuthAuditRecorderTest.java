package com.trailmate.server.auth;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcAuthAuditRecorderTest {
    private JdbcTemplate jdbcTemplate;
    private JdbcAuthAuditRecorder recorder;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:trailmate_auth_audit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();
        recorder = new JdbcAuthAuditRecorder(jdbcTemplate);
    }

    @Test
    void recordStoresPhoneAuditWithoutPlainPhoneNumber() {
        recorder.record(new AuthAuditEvent(
            "phone_login_failed",
            AuthProvider.PHONE,
            "failure",
            null,
            "invalid_sms_code",
            "+8613800138000",
            null,
            "203.0.113.8",
            Instant.parse("2026-06-22T10:00:00Z")
        ));

        assertThat(countRows()).isEqualTo(1);
        assertThat(stringColumn("event_type")).isEqualTo("phone_login_failed");
        assertThat(stringColumn("provider")).isEqualTo("phone");
        assertThat(stringColumn("outcome")).isEqualTo("failure");
        assertThat(stringColumn("reason_code")).isEqualTo("invalid_sms_code");
        assertThat(stringColumn("phone_e164_hash")).hasSize(64);
        assertThat(stringColumn("phone_e164_hash")).doesNotContain("+8613800138000");
        assertThat(stringColumn("ip_hash")).hasSize(64);
        assertThat(stringColumn("ip_hash")).doesNotContain("203.0.113.8");
    }

    @Test
    void recordStoresWechatAuditWithoutPlainOpenId() {
        recorder.record(new AuthAuditEvent(
            "wechat_login_succeeded",
            AuthProvider.WECHAT,
            "success",
            "usr_1",
            null,
            null,
            "openid-from-client",
            "203.0.113.8",
            Instant.parse("2026-06-22T10:00:00Z")
        ));

        assertThat(countRows()).isEqualTo(1);
        assertThat(stringColumn("event_type")).isEqualTo("wechat_login_succeeded");
        assertThat(stringColumn("provider")).isEqualTo("wechat");
        assertThat(stringColumn("outcome")).isEqualTo("success");
        assertThat(stringColumn("user_id")).isEqualTo("usr_1");
        assertThat(stringColumn("wechat_open_id_hash")).hasSize(64);
        assertThat(stringColumn("wechat_open_id_hash")).doesNotContain("openid-from-client");
    }

    private int countRows() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from auth_audit_event", Integer.class);
        return count == null ? 0 : count;
    }

    private String stringColumn(String column) {
        return jdbcTemplate.queryForObject("select " + column + " from auth_audit_event", String.class);
    }

    private void createSchema() {
        jdbcTemplate.execute("drop table if exists auth_audit_event");
        jdbcTemplate.execute("""
            create table auth_audit_event (
                id varchar(80) primary key,
                user_id varchar(80),
                event_type varchar(80) not null,
                provider varchar(20),
                outcome varchar(20) not null,
                reason_code varchar(120),
                phone_e164_hash varchar(128),
                wechat_open_id_hash varchar(128),
                ip_hash varchar(128),
                device_id varchar(120),
                user_agent_hash varchar(128),
                metadata_json varchar(1000) not null default '{}',
                created_at timestamp not null
            )
            """);
    }
}
