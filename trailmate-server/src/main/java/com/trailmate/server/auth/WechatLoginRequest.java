package com.trailmate.server.auth;

import jakarta.validation.constraints.NotBlank;

public record WechatLoginRequest(
    @NotBlank String authCode,
    @NotBlank String state
) {
}
