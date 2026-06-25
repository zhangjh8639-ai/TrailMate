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

class RedisSmsCodeCooldownRepositoryTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void tryAcquireStoresHashedPhoneWithRedisTtl() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeCooldownRepository repository = new RedisSmsCodeCooldownRepository(fixture.redis, clock);
        String key = fixture.expectedKey("+8613800138000");
        when(fixture.values.setIfAbsent(key, "1", Duration.ofSeconds(60))).thenReturn(true);

        boolean acquired = repository.tryAcquire("+8613800138000", clock.instant().plusSeconds(60));

        assertThat(acquired).isTrue();
        verify(fixture.values).setIfAbsent(eq(key), eq("1"), eq(Duration.ofSeconds(60)));
        assertThat(key).startsWith("auth:sms:cooldown:");
        assertThat(key).doesNotContain("+8613800138000");
    }

    @Test
    void tryAcquireReturnsFalseWhenCooldownAlreadyExists() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeCooldownRepository repository = new RedisSmsCodeCooldownRepository(fixture.redis, clock);
        String key = fixture.expectedKey("+8613800138000");
        when(fixture.values.setIfAbsent(key, "1", Duration.ofSeconds(60))).thenReturn(false);

        assertThat(repository.tryAcquire("+8613800138000", clock.instant().plusSeconds(60))).isFalse();
    }

    @Test
    void tryAcquireRejectsExpiredCooldownWindow() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeCooldownRepository repository = new RedisSmsCodeCooldownRepository(fixture.redis, clock);

        assertThat(repository.tryAcquire("+8613800138000", clock.instant().minusSeconds(1))).isFalse();
    }

    private static final class RedisFixture {
        private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        private final ValueOperations<String, String> values = mock(ValueOperations.class);

        private RedisFixture() {
            when(redis.opsForValue()).thenReturn(values);
        }

        private String expectedKey(String phoneNumber) {
            return "auth:sms:cooldown:" + sha256Hex(phoneNumber);
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
