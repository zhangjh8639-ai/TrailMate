package com.trailmate.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "trailmate.auth.sms-code-store", name = "mode", havingValue = "redis")
public class RedisSmsCodeRepository implements SmsCodeRepository {
    private static final String KEY_PREFIX = "auth:sms:code:";
    private final StringRedisTemplate redis;
    private final ValueOperations<String, String> values;
    private final Clock clock;

    public RedisSmsCodeRepository(StringRedisTemplate redis, Clock clock) {
        this.redis = redis;
        this.values = redis.opsForValue();
        this.clock = clock;
    }

    @Override
    public void save(String phoneNumber, String code, Instant expiresAt) {
        String key = keyFor(phoneNumber);
        Duration ttl = Duration.between(clock.instant(), expiresAt);
        if (ttl.isZero() || ttl.isNegative()) {
            redis.delete(key);
            return;
        }
        values.set(key, codeHash(phoneNumber, code), ttl);
    }

    @Override
    public boolean verifyAndConsume(String phoneNumber, String code, Instant now) {
        String key = keyFor(phoneNumber);
        String storedCodeHash = values.get(key);
        if (storedCodeHash == null || !storedCodeHash.equals(codeHash(phoneNumber, code))) {
            return false;
        }
        return Boolean.TRUE.equals(redis.delete(key));
    }

    private static String keyFor(String phoneNumber) {
        return KEY_PREFIX + sha256Hex(phoneNumber);
    }

    private static String codeHash(String phoneNumber, String code) {
        return sha256Hex(phoneNumber + ":" + code);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
