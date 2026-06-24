package com.trailmate.server.auth;

import org.springframework.stereotype.Component;

@Component
public class NoopSmsCodeSender implements SmsCodeSender {
    @Override
    public void sendLoginCode(String phoneNumber, String code) {
        // Replace with a real SMS provider adapter before production launch.
    }
}
