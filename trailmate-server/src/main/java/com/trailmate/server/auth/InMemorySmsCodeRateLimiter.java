package com.trailmate.server.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "trailmate.auth.sms-code-store", name = "mode", havingValue = "memory", matchIfMissing = true)
public class InMemorySmsCodeRateLimiter implements SmsCodeRateLimiter {
    private static final int PHONE_LIMIT_PER_HOUR = 5;
    private static final int IP_LIMIT_PER_HOUR = 30;
    private static final Duration WINDOW = Duration.ofHours(1);
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String phoneNumber, String clientAddress, Instant now) {
        long phoneCount = increment("phone:" + phoneNumber, now);
        long ipCount = increment("ip:" + normalizeClientAddress(clientAddress), now);
        return phoneCount <= PHONE_LIMIT_PER_HOUR && ipCount <= IP_LIMIT_PER_HOUR;
    }

    private long increment(String key, Instant now) {
        return counters.compute(key, (ignored, counter) -> {
            if (counter == null || !counter.expiresAt().isAfter(now)) {
                return new Counter(1, now.plus(WINDOW));
            }
            return new Counter(counter.count() + 1, counter.expiresAt());
        }).count();
    }

    private static String normalizeClientAddress(String clientAddress) {
        if (clientAddress == null || clientAddress.isBlank()) {
            return "unknown";
        }
        return clientAddress.trim();
    }

    private record Counter(long count, Instant expiresAt) {
    }
}
