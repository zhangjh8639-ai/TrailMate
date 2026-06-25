package com.trailmate.server.auth;

import java.util.UUID;

public class RandomAuthTokenGenerator implements AuthTokenGenerator, AuthTokenIdGenerator {
    @Override
    public String nextToken(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }

    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}
