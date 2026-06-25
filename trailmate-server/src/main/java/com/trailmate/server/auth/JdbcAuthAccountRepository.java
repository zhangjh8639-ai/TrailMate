package com.trailmate.server.auth;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcAuthAccountRepository implements AuthAccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private final AuthUserIdGenerator userIdGenerator;
    private final Clock clock;
    private final String wechatAppId;

    public JdbcAuthAccountRepository(
        JdbcTemplate jdbcTemplate,
        AuthUserIdGenerator userIdGenerator,
        Clock clock,
        String wechatAppId
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.userIdGenerator = userIdGenerator;
        this.clock = clock;
        this.wechatAppId = wechatAppId;
    }

    @Override
    public AuthAccount findOrCreateByPhone(String phoneNumber) {
        AuthAccount existingAccount = findByPhone(phoneNumber);
        if (existingAccount != null) {
            touchPhoneIdentity(phoneNumber);
            return existingAccount;
        }

        String userId = userIdGenerator.nextUserId();
        Instant now = clock.instant();
        try {
            insertUser(userId, null, now);
            jdbcTemplate.update(
                """
                    insert into user_phone_identity (
                        id, user_id, phone_e164, phone_country_code,
                        verified_at, last_login_at, created_at, updated_at
                    ) values (?, ?, ?, '+86', ?, ?, ?, ?)
                    """,
                UUID.randomUUID().toString(),
                userId,
                phoneNumber,
                Timestamp.from(now),
                Timestamp.from(now),
                Timestamp.from(now),
                Timestamp.from(now)
            );
            return new AuthAccount(userId, phoneNumber, null, null);
        } catch (DuplicateKeyException ignored) {
            return findRequiredByPhone(phoneNumber);
        }
    }

    @Override
    public AuthAccount findOrCreateByWechat(String openId, String unionId, String displayName) {
        AuthAccount existingAccount = findByWechatOpenId(openId);
        if (existingAccount != null) {
            touchWechatIdentity(openId);
            return existingAccount;
        }

        String userId = userIdGenerator.nextUserId();
        Instant now = clock.instant();
        try {
            insertUser(userId, displayName, now);
            jdbcTemplate.update(
                """
                    insert into user_wechat_identity (
                        id, user_id, app_id, open_id, union_id, nickname,
                        verified_at, last_login_at, created_at, updated_at
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                UUID.randomUUID().toString(),
                userId,
                wechatAppId,
                openId,
                unionId,
                displayName,
                Timestamp.from(now),
                Timestamp.from(now),
                Timestamp.from(now),
                Timestamp.from(now)
            );
            return new AuthAccount(userId, null, openId, displayName);
        } catch (DuplicateKeyException ignored) {
            return findRequiredByWechatOpenId(openId);
        }
    }

    private AuthAccount findByPhone(String phoneNumber) {
        try {
            return jdbcTemplate.queryForObject(
                """
                    select u.id, p.phone_e164, u.display_name
                    from user_phone_identity p
                    join app_user u on u.id = p.user_id
                    where p.phone_e164 = ?
                      and p.revoked_at is null
                      and u.status = 'active'
                    """,
                (rs, rowNum) -> new AuthAccount(
                    rs.getString("id"),
                    rs.getString("phone_e164"),
                    null,
                    rs.getString("display_name")
                ),
                phoneNumber
            );
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private AuthAccount findByWechatOpenId(String openId) {
        try {
            return jdbcTemplate.queryForObject(
                """
                    select u.id, w.open_id, w.nickname
                    from user_wechat_identity w
                    join app_user u on u.id = w.user_id
                    where w.app_id = ?
                      and w.open_id = ?
                      and w.revoked_at is null
                      and u.status = 'active'
                    """,
                (rs, rowNum) -> new AuthAccount(
                    rs.getString("id"),
                    null,
                    rs.getString("open_id"),
                    rs.getString("nickname")
                ),
                wechatAppId,
                openId
            );
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private AuthAccount findRequiredByPhone(String phoneNumber) {
        AuthAccount account = findByPhone(phoneNumber);
        if (account == null) {
            throw new IllegalStateException("Phone identity was not created.");
        }
        return account;
    }

    private AuthAccount findRequiredByWechatOpenId(String openId) {
        AuthAccount account = findByWechatOpenId(openId);
        if (account == null) {
            throw new IllegalStateException("WeChat identity was not created.");
        }
        return account;
    }

    private void insertUser(String userId, String displayName, Instant now) {
        jdbcTemplate.update(
            """
                insert into app_user (
                    id, status, display_name, locale, timezone,
                    onboarding_status, created_at, updated_at
                ) values (?, 'active', ?, 'zh-CN', 'Asia/Shanghai', 'account_created', ?, ?)
                """,
            userId,
            displayName,
            Timestamp.from(now),
            Timestamp.from(now)
        );
    }

    private void touchPhoneIdentity(String phoneNumber) {
        jdbcTemplate.update(
            "update user_phone_identity set last_login_at = ?, updated_at = ? where phone_e164 = ?",
            Timestamp.from(clock.instant()),
            Timestamp.from(clock.instant()),
            phoneNumber
        );
    }

    private void touchWechatIdentity(String openId) {
        jdbcTemplate.update(
            "update user_wechat_identity set last_login_at = ?, updated_at = ? where app_id = ? and open_id = ?",
            Timestamp.from(clock.instant()),
            Timestamp.from(clock.instant()),
            wechatAppId,
            openId
        );
    }
}
