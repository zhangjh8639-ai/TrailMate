package com.trailmate.server.auth;

public interface SmsCodeAttemptRecorder {
    void record(SmsCodeAttempt attempt);
}
