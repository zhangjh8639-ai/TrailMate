package com.trailmate.server.auth;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RandomAuthUserIdGenerator implements AuthUserIdGenerator {
    @Override
    public String nextUserId() {
        return "usr_" + UUID.randomUUID();
    }
}
