package com.trailmate.server.gear;

public record GearApiErrorResponse(
    int status,
    String code,
    String message,
    String traceId
) { }
