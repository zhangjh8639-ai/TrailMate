package com.trailmate.server.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhoneCodeRequest(
    @NotBlank String phoneNumber,
    @NotNull PhoneAuthScene scene
) {
}
