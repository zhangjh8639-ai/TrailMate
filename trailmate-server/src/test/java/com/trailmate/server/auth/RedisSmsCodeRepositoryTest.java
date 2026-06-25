package com.trailmate.server.auth;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class RedisSmsCodeRepositoryTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void saveHashesPhoneAndCodeWithRedisTtl() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeRepository repository = new RedisSmsCodeRepository(fixture.redis, clock);

        repository.save("+8613800138000", "123456", clock.instant().plusSeconds(300));

        fixture.verifyStored("+8613800138000", "123456", Duration.ofSeconds(300));
    }

    @Test
    void verifyAndConsumeAcceptsMatchingCodeOnlyOnce() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeRepository repository = new RedisSmsCodeRepository(fixture.redis, clock);
        repository.save("+8613800138000", "123456", clock.instant().plusSeconds(300));
        StoredRedisCode stored = fixture.verifyStored("+8613800138000", "123456", Duration.ofSeconds(300));
        clearInvocations(fixture.redis, fixture.values);
        when(fixture.values.get(stored.key())).thenReturn(stored.value()).thenReturn(null);
        when(fixture.redis.delete(stored.key())).thenReturn(true);

        assertThat(repository.verifyAndConsume("+8613800138000", "123456", clock.instant())).isTrue();
        assertThat(repository.verifyAndConsume("+8613800138000", "123456", clock.instant())).isFalse();
        verify(fixture.values, times(2)).get(stored.key());
        verify(fixture.redis).delete(stored.key());
    }

    @Test
    void verifyAndConsumeRejectsWrongCodeWithoutDeletingStoredCode() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeRepository repository = new RedisSmsCodeRepository(fixture.redis, clock);
        repository.save("+8613800138000", "123456", clock.instant().plusSeconds(300));
        StoredRedisCode stored = fixture.verifyStored("+8613800138000", "123456", Duration.ofSeconds(300));
        clearInvocations(fixture.redis, fixture.values);
        when(fixture.values.get(stored.key())).thenReturn(stored.value());

        assertThat(repository.verifyAndConsume("+8613800138000", "000000", clock.instant())).isFalse();

        verify(fixture.values).get(stored.key());
        verifyNoMoreInteractions(fixture.redis);
    }

    @Test
    void saveDeletesExpiredCodeInsteadOfWritingIt() {
        RedisFixture fixture = new RedisFixture();
        RedisSmsCodeRepository repository = new RedisSmsCodeRepository(fixture.redis, clock);

        repository.save("+8613800138000", "123456", clock.instant().minusSeconds(1));

        verify(fixture.redis).delete(fixture.expectedKey("+8613800138000"));
        verifyNoMoreInteractions(fixture.values);
    }

    private static final class RedisFixture {
        private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        private final ValueOperations<String, String> values = mock(ValueOperations.class);

        private RedisFixture() {
            when(redis.opsForValue()).thenReturn(values);
        }

        private StoredRedisCode verifyStored(String phoneNumber, String code, Duration ttl) {
            String key = expectedKey(phoneNumber);
            String value = sha256Hex(phoneNumber + ":" + code);
            verify(values).set(eq(key), eq(value), eq(ttl));
            assertThat(key).startsWith("auth:sms:code:");
            assertThat(key).doesNotContain(phoneNumber);
            assertThat(value).hasSize(64);
            assertThat(value).doesNotContain(code);
            return new StoredRedisCode(key, value);
        }

        private String expectedKey(String phoneNumber) {
            return "auth:sms:code:" + sha256Hex(phoneNumber);
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

    private record StoredRedisCode(String key, String value) {
    }
}
