package com.trailmate.server.auth;

import jakarta.validation.constraints.NotBlank;

public record PhoneLoginRequest(
    @NotBlank String phoneNumber,
    @NotBlank String smsCode
) {
}
