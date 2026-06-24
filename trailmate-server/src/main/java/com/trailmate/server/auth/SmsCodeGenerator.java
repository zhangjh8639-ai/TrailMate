package com.trailmate.server.auth;

@FunctionalInterface
public interface SmsCodeGenerator {
    String nextCode();
}
