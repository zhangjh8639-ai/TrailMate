package com.trailmate.server.auth;

public interface SmsCodeSender {
    void sendLoginCode(String phoneNumber, String code);
}
