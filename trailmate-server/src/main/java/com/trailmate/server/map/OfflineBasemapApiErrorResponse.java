package com.trailmate.server.map;

public record OfflineBasemapApiErrorResponse(
    int status,
    String code,
    String message,
    String traceId
) { }
