package com.trailmate.server.auth;

import java.time.Instant;

public record AuthAuditEvent(
    String eventType,
    AuthProvider provider,
    String outcome,
    String userId,
    String reasonCode,
    String phoneNumber,
    String wechatOpenId,
    String clientAddress,
    Instant occurredAt
) {
}
