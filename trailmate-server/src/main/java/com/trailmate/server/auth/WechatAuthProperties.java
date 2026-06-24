package com.trailmate.server.auth;

public record WechatAuthProperties(
    String appId,
    String appSecret,
    String apiBaseUrl
) {
    public WechatAuthProperties {
        if (appId == null || appId.isBlank()) {
            throw new IllegalArgumentException("WeChat appId is required.");
        }
        if (appSecret == null || appSecret.isBlank()) {
            throw new IllegalArgumentException("WeChat appSecret is required.");
        }
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            throw new IllegalArgumentException("WeChat apiBaseUrl is required.");
        }
        apiBaseUrl = apiBaseUrl.replaceAll("/+$", "");
    }
}
