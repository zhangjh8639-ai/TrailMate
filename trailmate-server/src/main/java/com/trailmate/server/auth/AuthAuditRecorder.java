package com.trailmate.server.auth;

public interface AuthAuditRecorder {
    void record(AuthAuditEvent event);
}
