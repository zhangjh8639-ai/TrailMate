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
public class RedisSmsCodeRateLimiter implements SmsCodeRateLimiter {
    private static final String PHONE_KEY_PREFIX = "auth:rate:phone:";
    private static final String IP_KEY_PREFIX = "auth:rate:ip:";
    private static final int PHONE_LIMIT_PER_HOUR = 5;
    private static final int IP_LIMIT_PER_HOUR = 30;
    private static final Duration WINDOW = Duration.ofHours(1);
    private final StringRedisTemplate redis;
    private final ValueOperations<String, String> values;

    public RedisSmsCodeRateLimiter(StringRedisTemplate redis, Clock clock) {
        this.redis = redis;
        this.values = redis.opsForValue();
    }

    @Override
    public boolean tryAcquire(String phoneNumber, String clientAddress, Instant now) {
        long phoneCount = increment(phoneKey(phoneNumber));
        long ipCount = increment(ipKey(clientAddress));
        return phoneCount <= PHONE_LIMIT_PER_HOUR && ipCount <= IP_LIMIT_PER_HOUR;
    }

    private long increment(String key) {
        Long count = values.increment(key);
        if (count == null) {
            return Long.MAX_VALUE;
        }
        if (count == 1L) {
            redis.expire(key, WINDOW);
        }
        return count;
    }

    private static String phoneKey(String phoneNumber) {
        return PHONE_KEY_PREFIX + sha256Hex(phoneNumber);
    }

    private static String ipKey(String clientAddress) {
        return IP_KEY_PREFIX + sha256Hex(normalizeClientAddress(clientAddress));
    }

    private static String normalizeClientAddress(String clientAddress) {
        if (clientAddress == null || clientAddress.isBlank()) {
            return "unknown";
        }
        return clientAddress.trim();
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
