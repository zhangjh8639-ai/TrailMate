package com.trailmate.server.auth;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisSmsCodeRateLimiterTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void allowsRequestAndWritesHashedPhoneAndIpCounters() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeRateLimiter limiter = new RedisSmsCodeRateLimiter(fixture.redis, clock);
        String phoneKey = fixture.expectedPhoneKey("+8613800138000");
        String ipKey = fixture.expectedIpKey("203.0.113.8");
        when(fixture.values.increment(phoneKey)).thenReturn(1L);
        when(fixture.values.increment(ipKey)).thenReturn(1L);

        boolean allowed = limiter.tryAcquire("+8613800138000", "203.0.113.8", clock.instant());

        assertThat(allowed).isTrue();
        verify(fixture.redis).expire(phoneKey, Duration.ofHours(1));
        verify(fixture.redis).expire(ipKey, Duration.ofHours(1));
        assertThat(phoneKey).startsWith("auth:rate:phone:");
        assertThat(phoneKey).doesNotContain("+8613800138000");
        assertThat(ipKey).startsWith("auth:rate:ip:");
        assertThat(ipKey).doesNotContain("203.0.113.8");
    }

    @Test
    void rejectsRequestWhenPhoneLimitIsExceeded() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeRateLimiter limiter = new RedisSmsCodeRateLimiter(fixture.redis, clock);
        when(fixture.values.increment(fixture.expectedPhoneKey("+8613800138000"))).thenReturn(6L);
        when(fixture.values.increment(fixture.expectedIpKey("203.0.113.8"))).thenReturn(1L);

        assertThat(limiter.tryAcquire("+8613800138000", "203.0.113.8", clock.instant())).isFalse();
    }

    @Test
    void rejectsRequestWhenIpLimitIsExceeded() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeRateLimiter limiter = new RedisSmsCodeRateLimiter(fixture.redis, clock);
        when(fixture.values.increment(fixture.expectedPhoneKey("+8613800138000"))).thenReturn(1L);
        when(fixture.values.increment(fixture.expectedIpKey("203.0.113.8"))).thenReturn(31L);

        assertThat(limiter.tryAcquire("+8613800138000", "203.0.113.8", clock.instant())).isFalse();
    }

    private static final class RedisFixture {
        private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        private final ValueOperations<String, String> values = mock(ValueOperations.class);

        private RedisFixture() {
            when(redis.opsForValue()).thenReturn(values);
        }

        private String expectedPhoneKey(String phoneNumber) {
            return "auth:rate:phone:" + sha256Hex(phoneNumber);
        }

        private String expectedIpKey(String clientAddress) {
            return "auth:rate:ip:" + sha256Hex(clientAddress);
        }

        private static String sha256Hex(String value) {
            try {
                byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
                StringBuilder hex = new StringBuilder(digest.length * 2);
                for (byte item : digest) {
                    hex.append(String.format("%02x", item));
                }
                return hex.toString();
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException("SHA-256 is unavailable.", exception);
            }
        }
    }
}
