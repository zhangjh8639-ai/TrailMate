package com.trailmate.server.auth;

import java.security.SecureRandom;

public class RandomSmsCodeGenerator implements SmsCodeGenerator {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String nextCode() {
        return "%06d".formatted(random.nextInt(1_000_000));
    }
}
