package com.trailmate.server.auth;

public class NoopAuthAuditRecorder implements AuthAuditRecorder {
    @Override
    public void record(AuthAuditEvent event) {
    }
}
