package com.trailmate.server.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "trailmate.auth.sms-code-store", name = "mode", havingValue = "memory", matchIfMissing = true)
public class InMemorySmsCodeCooldownRepository implements SmsCodeCooldownRepository {
    private final ConcurrentHashMap<String, Instant> cooldowns = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemorySmsCodeCooldownRepository() {
        this(Clock.systemUTC());
    }

    InMemorySmsCodeCooldownRepository(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String phoneNumber, Instant expiresAt) {
        Instant now = clock.instant();
        if (!expiresAt.isAfter(now)) {
            return false;
        }

        AtomicBoolean acquired = new AtomicBoolean(false);
        cooldowns.compute(phoneNumber, (key, existingExpiresAt) -> {
            if (existingExpiresAt == null || !existingExpiresAt.isAfter(now)) {
                acquired.set(true);
                return expiresAt;
            }
            return existingExpiresAt;
        });
        return acquired.get();
    }
}
