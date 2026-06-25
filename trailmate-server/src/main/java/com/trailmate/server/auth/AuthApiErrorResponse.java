package com.trailmate.server.auth;

public record AuthApiErrorResponse(
    int status,
    String code,
    String message,
    String traceId
) {
}
