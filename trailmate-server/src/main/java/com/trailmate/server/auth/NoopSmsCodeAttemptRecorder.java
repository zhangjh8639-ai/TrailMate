package com.trailmate.server.auth;

public class NoopSmsCodeAttemptRecorder implements SmsCodeAttemptRecorder {
    @Override
    public void record(SmsCodeAttempt attempt) {
    }
}
