package com.trailmate.server.auth;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "trailmate.auth.sms-code-store", name = "mode", havingValue = "memory", matchIfMissing = true)
public class InMemorySmsCodeRepository implements SmsCodeRepository {
    private final ConcurrentHashMap<String, StoredCode> codes = new ConcurrentHashMap<>();

    @Override
    public void save(String phoneNumber, String code, Instant expiresAt) {
        codes.put(phoneNumber, new StoredCode(code, expiresAt));
    }

    @Override
    public boolean verifyAndConsume(String phoneNumber, String code, Instant now) {
        StoredCode storedCode = codes.get(phoneNumber);
        if (storedCode == null || storedCode.expiresAt().isBefore(now) || !storedCode.code().equals(code)) {
            return false;
        }
        return codes.remove(phoneNumber, storedCode);
    }

    private record StoredCode(String code, Instant expiresAt) {
    }
}
