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

class JdbcAuthAccountRepositoryTest {
    private JdbcTemplate jdbcTemplate;
    private JdbcAuthAccountRepository repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:trailmate_auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();
        repository = new JdbcAuthAccountRepository(
            jdbcTemplate,
            new SequentialUserIdGenerator(List.of(
                "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000002"
            )),
            Clock.fixed(Instant.parse("2026-06-22T10:00:00Z"), ZoneOffset.UTC),
            "wx-app-id"
        );
    }

    @Test
    void findOrCreateByPhoneCreatesAndReusesAccount() {
        AuthAccount first = repository.findOrCreateByPhone("+8613800138000");
        AuthAccount second = repository.findOrCreateByPhone("+8613800138000");

        assertEquals(first.userId(), second.userId());
        assertEquals("+8613800138000", first.phoneNumber());
        assertEquals(1, countRows("app_user"));
        assertEquals(1, countRows("user_phone_identity"));
    }

    @Test
    void findOrCreateByWechatCreatesAndReusesAccountForSameAppOpenId() {
        AuthAccount first = repository.findOrCreateByWechat("openid-1", "unionid-1", "微信用户");
        AuthAccount second = repository.findOrCreateByWechat("openid-1", "unionid-1", "更新昵称");

        assertEquals(first.userId(), second.userId());
        assertEquals("openid-1", first.wechatOpenId());
        assertEquals("微信用户", first.displayName());
        assertEquals("unionid-1", findWechatUnionId("openid-1"));
        assertEquals(1, countRows("app_user"));
        assertEquals(1, countRows("user_wechat_identity"));
    }

    private String findWechatUnionId(String openId) {
        return jdbcTemplate.queryForObject(
            "select union_id from user_wechat_identity where open_id = ?",
            String.class,
            openId
        );
    }

    private int countRows(String tableName) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
        return count == null ? 0 : count;
    }

    private void createSchema() {
        jdbcTemplate.execute("drop table if exists user_phone_identity");
        jdbcTemplate.execute("drop table if exists user_wechat_identity");
        jdbcTemplate.execute("drop table if exists app_user");
        jdbcTemplate.execute("""
            create table app_user (
                id varchar(80) primary key,
                status varchar(20) not null default 'active',
                display_name varchar(120),
                avatar_url varchar(500),
                locale varchar(20) not null default 'zh-CN',
                timezone varchar(80) not null default 'Asia/Shanghai',
                onboarding_status varchar(40) not null default 'account_created',
                created_at timestamp not null,
                updated_at timestamp not null,
                disabled_at timestamp,
                deleted_at timestamp
            )
            """);
        jdbcTemplate.execute("""
            create table user_phone_identity (
                id varchar(80) primary key,
                user_id varchar(80) not null references app_user(id),
                phone_e164 varchar(32) not null unique,
                phone_country_code varchar(8) not null default '+86',
                verified_at timestamp not null,
                last_login_at timestamp,
                created_at timestamp not null,
                updated_at timestamp not null,
                revoked_at timestamp
            )
            """);
        jdbcTemplate.execute("""
            create table user_wechat_identity (
                id varchar(80) primary key,
                user_id varchar(80) not null references app_user(id),
                app_id varchar(120) not null,
                open_id varchar(160) not null,
                union_id varchar(160),
                nickname varchar(120),
                avatar_url varchar(500),
                scope varchar(120),
                verified_at timestamp not null,
                last_login_at timestamp,
                created_at timestamp not null,
                updated_at timestamp not null,
                revoked_at timestamp,
                constraint uq_user_wechat_identity_openid unique (app_id, open_id)
            )
            """);
    }

    private static final class SequentialUserIdGenerator implements AuthUserIdGenerator {
        private final List<String> userIds;
        private int index = 0;

        private SequentialUserIdGenerator(List<String> userIds) {
            this.userIds = userIds;
        }

        @Override
        public String nextUserId() {
            return userIds.get(index++);
        }
    }
}
