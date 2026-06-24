package com.trailmate.server.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {
    private final AuthService authService = new AuthService(
        new InMemorySmsCodeRepository(),
        (phoneNumber, code) -> { },
        () -> "123456",
        new PreviewWechatAuthClient(),
        Clock.fixed(Instant.parse("2026-06-22T10:00:00Z"), ZoneOffset.UTC)
    );
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new AuthController(authService))
        .build();

    @Test
    void requestPhoneCodeReturnsExpiryAndRetryWindow() throws Exception {
        mockMvc.perform(
                post("/api/v1/auth/phone/code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "phoneNumber": "+8613800138000",
                          "scene": "LOGIN_OR_REGISTER"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.phoneNumber").value("+8613800138000"))
            .andExpect(jsonPath("$.expiresInSeconds").value(300))
            .andExpect(jsonPath("$.retryAfterSeconds").value(60));
    }

    @Test
    void requestPhoneCodeUsesFirstForwardedAddress() throws Exception {
        CapturingAuthService capturingAuthService = new CapturingAuthService();
        MockMvc forwardingMockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(capturingAuthService))
            .build();

        forwardingMockMvc.perform(
                post("/api/v1/auth/phone/code")
                    .header("X-Forwarded-For", "203.0.113.8, 10.0.0.5")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "phoneNumber": "+8613800138000",
                          "scene": "LOGIN_OR_REGISTER"
                        }
                        """)
            )
            .andExpect(status().isOk());

        assertEquals("203.0.113.8", capturingAuthService.clientAddress);
    }

    @Test
    void phoneLoginCreatesOrReturnsSession() throws Exception {
        authService.requestPhoneCode(new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER));

        mockMvc.perform(
                post("/api/v1/auth/phone/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "phoneNumber": "+8613800138000",
                          "smsCode": "123456"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.provider").value("PHONE"))
            .andExpect(jsonPath("$.phoneNumber").value("+8613800138000"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void wechatLoginExchangesAuthCodeForSession() throws Exception {
        mockMvc.perform(
                post("/api/v1/auth/wechat/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "authCode": "wx-auth-code",
                          "state": "client-nonce"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.provider").value("WECHAT"))
            .andExpect(jsonPath("$.wechatOpenId").value("wx_openid_preview"))
            .andExpect(jsonPath("$.displayName").value("微信用户"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void invalidPhoneLoginReturnsBadRequest() throws Exception {
        mockMvc.perform(
                post("/api/v1/auth/phone/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "phoneNumber": "12345",
                          "smsCode": "123456"
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("AUTH_INVALID_REQUEST"))
            .andExpect(jsonPath("$.message").value("Invalid phone number."))
            .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void refreshSessionRotatesRefreshToken() throws Exception {
        authService.requestPhoneCode(new PhoneCodeRequest("+8613800138001", PhoneAuthScene.LOGIN_OR_REGISTER));
        AuthSessionResponse session = authService.loginWithPhone(
            new PhoneLoginRequest("+8613800138001", "123456")
        );

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(session.refreshToken()))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.provider").value("PHONE"))
            .andExpect(jsonPath("$.userId").value(session.userId()))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").value(not(session.refreshToken())));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        authService.requestPhoneCode(new PhoneCodeRequest("+8613800138002", PhoneAuthScene.LOGIN_OR_REGISTER));
        AuthSessionResponse session = authService.loginWithPhone(
            new PhoneLoginRequest("+8613800138002", "123456")
        );

        mockMvc.perform(
                post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(session.refreshToken()))
            )
            .andExpect(status().isNoContent());

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(session.refreshToken()))
            )
            .andExpect(status().isBadRequest());
    }

    private static final class CapturingAuthService extends AuthService {
        private String clientAddress;

        @Override
        public PhoneCodeResponse requestPhoneCode(PhoneCodeRequest request, String clientAddress) {
            this.clientAddress = clientAddress;
            return new PhoneCodeResponse(request.phoneNumber(), 300, 60);
        }
    }
}
