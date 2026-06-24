package com.trailmate.server.auth;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "trailmate.auth.persistence", name = "mode", havingValue = "memory", matchIfMissing = true)
public class InMemoryAuthAccountRepository implements AuthAccountRepository {
    private final AuthUserIdGenerator userIdGenerator;
    private final ConcurrentHashMap<String, AuthAccount> accountsByPhone = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AuthAccount> accountsByWechatOpenId = new ConcurrentHashMap<>();

    public InMemoryAuthAccountRepository(AuthUserIdGenerator userIdGenerator) {
        this.userIdGenerator = userIdGenerator;
    }

    @Override
    public AuthAccount findOrCreateByPhone(String phoneNumber) {
        return accountsByPhone.computeIfAbsent(
            phoneNumber,
            key -> new AuthAccount(userIdGenerator.nextUserId(), key, null, null)
        );
    }

    @Override
    public AuthAccount findOrCreateByWechat(String openId, String unionId, String displayName) {
        return accountsByWechatOpenId.computeIfAbsent(
            openId,
            key -> new AuthAccount(userIdGenerator.nextUserId(), null, key, displayName)
        );
    }

    public AuthAccount findByPhone(String phoneNumber) {
        return accountsByPhone.get(phoneNumber);
    }

    public AuthAccount findByWechatOpenId(String openId) {
        return accountsByWechatOpenId.get(openId);
    }
}
