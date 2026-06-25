package com.trailmate.server.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshSessionRequest(
    @NotBlank String refreshToken
) {
}
