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
public class RedisSmsCodeCooldownRepository implements SmsCodeCooldownRepository {
    private static final String KEY_PREFIX = "auth:sms:cooldown:";
    private final ValueOperations<String, String> values;
    private final Clock clock;

    public RedisSmsCodeCooldownRepository(StringRedisTemplate redis, Clock clock) {
        this.values = redis.opsForValue();
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String phoneNumber, Instant expiresAt) {
        Duration ttl = Duration.between(clock.instant(), expiresAt);
        if (ttl.isZero() || ttl.isNegative()) {
            return false;
        }
        return Boolean.TRUE.equals(values.setIfAbsent(keyFor(phoneNumber), "1", ttl));
    }

    private static String keyFor(String phoneNumber) {
        return KEY_PREFIX + sha256Hex(phoneNumber);
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
