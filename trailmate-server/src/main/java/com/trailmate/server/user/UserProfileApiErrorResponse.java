package com.trailmate.server.user;

public record UserProfileApiErrorResponse(
    int status,
    String code,
    String message,
    String traceId
) { }
