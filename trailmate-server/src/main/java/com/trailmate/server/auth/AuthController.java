package com.trailmate.server.auth;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/phone/code")
    public PhoneCodeResponse requestPhoneCode(
        @Valid @RequestBody PhoneCodeRequest request,
        HttpServletRequest httpRequest
    ) {
        return authService.requestPhoneCode(request, clientAddress(httpRequest));
    }

    @PostMapping("/phone/login")
    public AuthSessionResponse loginWithPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return authService.loginWithPhone(request);
    }

    @PostMapping("/wechat/login")
    public AuthSessionResponse loginWithWechat(@Valid @RequestBody WechatLoginRequest request) {
        return authService.loginWithWechat(request);
    }

    @PostMapping("/refresh")
    public AuthSessionResponse refreshSession(@Valid @RequestBody RefreshSessionRequest request) {
        return authService.refreshSession(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new AuthApiErrorResponse(
                400,
                "AUTH_INVALID_REQUEST",
                exception.getMessage() == null ? "Invalid auth request." : exception.getMessage(),
                UUID.randomUUID().toString()
            ));
    }

    private String clientAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
