package com.trailmate.server.auth;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void requestPhoneCodeStoresAndSendsGeneratedCode() {
        var repository = new InMemorySmsCodeRepository();
        var cooldownRepository = new InMemorySmsCodeCooldownRepository(clock);
        var sender = new CapturingSmsCodeSender();
        var service = new AuthService(
            repository,
            cooldownRepository,
            sender,
            () -> "654321",
            new FakeWechatAuthClient(),
            clock
        );

        PhoneCodeResponse response = service.requestPhoneCode(
            new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER)
        );

        assertEquals("+8613800138000", response.phoneNumber());
        assertEquals(300, response.expiresInSeconds());
        assertEquals(List.of("+8613800138000:654321"), sender.sentCodes);
        assertEquals(true, repository.verifyAndConsume("+8613800138000", "654321", clock.instant()));
    }

    @Test
    void requestPhoneCodeRejectsRepeatedRequestDuringCooldown() {
        var sender = new CapturingSmsCodeSender();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new InMemorySmsCodeCooldownRepository(clock),
            sender,
            () -> "654321",
            new FakeWechatAuthClient(),
            clock
        );
        service.requestPhoneCode(new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER));

        assertThrows(IllegalArgumentException.class, () ->
            service.requestPhoneCode(new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER))
        );
        assertEquals(List.of("+8613800138000:654321"), sender.sentCodes);
    }

    @Test
    void requestPhoneCodeRejectsWhenRateLimitIsExceeded() {
        var sender = new CapturingSmsCodeSender();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new InMemorySmsCodeCooldownRepository(clock),
            new DenyingSmsCodeRateLimiter(),
            sender,
            () -> "654321",
            new FakeWechatAuthClient(),
            clock
        );

        assertThrows(IllegalArgumentException.class, () ->
            service.requestPhoneCode(
                new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER),
                "203.0.113.8"
            )
        );
        assertEquals(List.of(), sender.sentCodes);
    }

    @Test
    void requestPhoneCodeRecordsAuditEvent() {
        var auditRecorder = new CapturingAuthAuditRecorder();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            new CapturingSmsCodeSender(),
            () -> "654321",
            new FakeWechatAuthClient(),
            new InMemoryAuthAccountRepository(new SequentialUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            auditRecorder,
            clock
        );

        service.requestPhoneCode(new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER));

        AuthAuditEvent event = auditRecorder.events.get(0);
        assertEquals("sms_code_requested", event.eventType());
        assertEquals(AuthProvider.PHONE, event.provider());
        assertEquals("success", event.outcome());
        assertEquals("+8613800138000", event.phoneNumber());
        assertEquals(clock.instant(), event.occurredAt());
    }

    @Test
    void requestPhoneCodeRecordsSmsDeliveryAttempt() {
        var attemptRecorder = new CapturingSmsCodeAttemptRecorder();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            new CapturingSmsCodeSender(),
            () -> "654321",
            new FakeWechatAuthClient(),
            new InMemoryAuthAccountRepository(new SequentialUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            new NoopAuthAuditRecorder(),
            attemptRecorder,
            clock
        );

        service.requestPhoneCode(
            new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER),
            "203.0.113.8"
        );

        SmsCodeAttempt attempt = attemptRecorder.attempts.get(0);
        assertEquals("+8613800138000", attempt.phoneNumber());
        assertEquals("login_or_register", attempt.scene());
        assertEquals("noop", attempt.provider());
        assertEquals("sent", attempt.deliveryStatus());
        assertEquals("203.0.113.8", attempt.clientAddress());
        assertEquals(clock.instant(), attempt.createdAt());
        assertEquals(clock.instant().plusSeconds(300), attempt.expiresAt());
    }

    @Test
    void requestPhoneCodeRecordsFailedSmsDeliveryAttempt() {
        var attemptRecorder = new CapturingSmsCodeAttemptRecorder();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            new ThrowingSmsCodeSender(),
            () -> "654321",
            new FakeWechatAuthClient(),
            new InMemoryAuthAccountRepository(new SequentialUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            new NoopAuthAuditRecorder(),
            attemptRecorder,
            clock
        );

        assertThrows(IllegalStateException.class, () ->
            service.requestPhoneCode(
                new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER),
                "203.0.113.8"
            )
        );

        SmsCodeAttempt attempt = attemptRecorder.attempts.get(0);
        assertEquals("failed", attempt.deliveryStatus());
        assertEquals("sender_exception", attempt.failureCode());
        assertEquals("provider unavailable", attempt.failureMessage());
    }

    @Test
    void phoneLoginRecordsSuccessAndFailureAuditEvents() {
        var auditRecorder = new CapturingAuthAuditRecorder();
        var repository = new InMemorySmsCodeRepository();
        var service = new AuthService(
            repository,
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            new InMemoryAuthAccountRepository(new SequentialUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            auditRecorder,
            clock
        );
        service.requestPhoneCode(new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER));

        assertThrows(IllegalArgumentException.class, () ->
            service.loginWithPhone(new PhoneLoginRequest("+8613800138000", "000000"))
        );
        repository.save("+8613800138000", "123456", clock.instant().plusSeconds(300));
        AuthSessionResponse session = service.loginWithPhone(
            new PhoneLoginRequest("+8613800138000", "123456")
        );

        AuthAuditEvent failed = auditRecorder.events.get(1);
        AuthAuditEvent succeeded = auditRecorder.events.get(2);
        assertEquals("phone_login_failed", failed.eventType());
        assertEquals("failure", failed.outcome());
        assertEquals("invalid_sms_code", failed.reasonCode());
        assertEquals("phone_login_succeeded", succeeded.eventType());
        assertEquals("success", succeeded.outcome());
        assertEquals(session.userId(), succeeded.userId());
    }

    @Test
    void wechatLoginRecordsSuccessAuditEvent() {
        var auditRecorder = new CapturingAuthAuditRecorder();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            new InMemoryAuthAccountRepository(new SequentialUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            auditRecorder,
            clock
        );

        AuthSessionResponse session = service.loginWithWechat(
            new WechatLoginRequest("wx-auth-code", "client-nonce")
        );

        AuthAuditEvent event = auditRecorder.events.get(0);
        assertEquals("wechat_login_succeeded", event.eventType());
        assertEquals(AuthProvider.WECHAT, event.provider());
        assertEquals("success", event.outcome());
        assertEquals(session.userId(), event.userId());
        assertEquals("openid-from-client", event.wechatOpenId());
    }

    @Test
    void phoneLoginRequiresMatchingRequestedCodeAndConsumesIt() {
        var repository = new InMemorySmsCodeRepository();
        var service = new AuthService(
            repository,
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            clock
        );
        service.requestPhoneCode(new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER));

        AuthSessionResponse session = service.loginWithPhone(
            new PhoneLoginRequest("+8613800138000", "123456")
        );

        assertEquals(AuthProvider.PHONE, session.provider());
        assertEquals("+8613800138000", session.phoneNumber());
        assertThrows(IllegalArgumentException.class, () ->
            service.loginWithPhone(new PhoneLoginRequest("+8613800138000", "123456"))
        );
    }

    @Test
    void phoneLoginCreatesAccountThroughAccountRepository() {
        var smsRepository = new InMemorySmsCodeRepository();
        var accountRepository = new InMemoryAuthAccountRepository(new SequentialUserIdGenerator());
        var sessionIssuer = new CapturingSessionIssuer();
        var service = new AuthService(
            smsRepository,
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            accountRepository,
            sessionIssuer,
            clock
        );
        service.requestPhoneCode(new PhoneCodeRequest("+8613800138000", PhoneAuthScene.LOGIN_OR_REGISTER));

        AuthSessionResponse session = service.loginWithPhone(
            new PhoneLoginRequest("+8613800138000", "123456")
        );

        assertEquals("usr_test_1", session.userId());
        assertEquals("usr_test_1", accountRepository.findByPhone("+8613800138000").userId());
        assertEquals("usr_test_1", sessionIssuer.issuedAccount.userId());
        assertEquals(AuthProvider.PHONE, sessionIssuer.issuedProvider);
        assertEquals(clock.instant(), sessionIssuer.issuedAt);
    }

    @Test
    void phoneLoginRejectsWrongOrMissingCode() {
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            clock
        );

        assertThrows(IllegalArgumentException.class, () ->
            service.loginWithPhone(new PhoneLoginRequest("+8613800138000", "000000"))
        );
    }

    @Test
    void wechatLoginUsesExchangedIdentity() {
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            clock
        );

        AuthSessionResponse session = service.loginWithWechat(
            new WechatLoginRequest("wx-auth-code", "client-nonce")
        );

        assertEquals(AuthProvider.WECHAT, session.provider());
        assertEquals("openid-from-client", session.wechatOpenId());
        assertEquals("TrailMate 微信用户", session.displayName());
    }

    @Test
    void wechatLoginReusesExistingAccountForSameOpenId() {
        var accountRepository = new InMemoryAuthAccountRepository(new SequentialUserIdGenerator());
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            accountRepository,
            clock
        );

        AuthSessionResponse firstSession = service.loginWithWechat(
            new WechatLoginRequest("first-code", "first-state")
        );
        AuthSessionResponse secondSession = service.loginWithWechat(
            new WechatLoginRequest("second-code", "second-state")
        );

        assertEquals("usr_test_1", firstSession.userId());
        assertEquals(firstSession.userId(), secondSession.userId());
        assertEquals("usr_test_1", accountRepository.findByWechatOpenId("openid-from-client").userId());
    }

    @Test
    void refreshSessionDelegatesToSessionIssuer() {
        var sessionIssuer = new CapturingSessionIssuer();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            new InMemoryAuthAccountRepository(new SequentialUserIdGenerator()),
            sessionIssuer,
            clock
        );

        AuthSessionResponse session = service.refreshSession(new RefreshSessionRequest("old-refresh"));

        assertEquals("old-refresh", sessionIssuer.refreshedToken);
        assertEquals(clock.instant(), sessionIssuer.refreshedAt);
        assertEquals("rotated-refresh", session.refreshToken());
    }

    @Test
    void logoutDelegatesToSessionIssuer() {
        var sessionIssuer = new CapturingSessionIssuer();
        var service = new AuthService(
            new InMemorySmsCodeRepository(),
            new CapturingSmsCodeSender(),
            () -> "123456",
            new FakeWechatAuthClient(),
            new InMemoryAuthAccountRepository(new SequentialUserIdGenerator()),
            sessionIssuer,
            clock
        );

        service.logout(new LogoutRequest("old-refresh"));

        assertEquals("old-refresh", sessionIssuer.revokedToken);
        assertEquals(clock.instant(), sessionIssuer.revokedAt);
    }

    private static final class CapturingSmsCodeSender implements SmsCodeSender {
        private final List<String> sentCodes = new ArrayList<>();

        @Override
        public void sendLoginCode(String phoneNumber, String code) {
            sentCodes.add(phoneNumber + ":" + code);
        }
    }

    private static final class ThrowingSmsCodeSender implements SmsCodeSender {
        @Override
        public void sendLoginCode(String phoneNumber, String code) {
            throw new IllegalStateException("provider unavailable");
        }
    }

    private static final class DenyingSmsCodeRateLimiter implements SmsCodeRateLimiter {
        @Override
        public boolean tryAcquire(String phoneNumber, String clientAddress, Instant now) {
            return false;
        }
    }

    private static final class CapturingAuthAuditRecorder implements AuthAuditRecorder {
        private final List<AuthAuditEvent> events = new ArrayList<>();

        @Override
        public void record(AuthAuditEvent event) {
            events.add(event);
        }
    }

    private static final class CapturingSmsCodeAttemptRecorder implements SmsCodeAttemptRecorder {
        private final List<SmsCodeAttempt> attempts = new ArrayList<>();

        @Override
        public void record(SmsCodeAttempt attempt) {
            attempts.add(attempt);
        }
    }

    private static final class FakeWechatAuthClient implements WechatAuthClient {
        @Override
        public WechatIdentity exchangeAuthCode(String authCode, String state) {
            return new WechatIdentity("openid-from-client", "unionid-from-client", "TrailMate 微信用户");
        }
    }

    private static final class CapturingSessionIssuer implements AuthSessionIssuer {
        private AuthAccount issuedAccount;
        private AuthProvider issuedProvider;
        private Instant issuedAt;
        private String refreshedToken;
        private Instant refreshedAt;
        private String revokedToken;
        private Instant revokedAt;

        @Override
        public AuthSessionResponse issueSession(AuthAccount account, AuthProvider provider, Instant now) {
            this.issuedAccount = account;
            this.issuedProvider = provider;
            this.issuedAt = now;
            return new AuthSessionResponse(
                account.userId(),
                provider,
                "issued-access",
                "issued-refresh",
                now.plusSeconds(7200).toString(),
                account.phoneNumber(),
                account.wechatOpenId(),
                account.displayName()
            );
        }

        @Override
        public AuthSessionResponse refreshSession(String refreshToken, Instant now) {
            this.refreshedToken = refreshToken;
            this.refreshedAt = now;
            return new AuthSessionResponse(
                "usr_test_1",
                AuthProvider.PHONE,
                "rotated-access",
                "rotated-refresh",
                now.plusSeconds(7200).toString(),
                "+8613800138000",
                null,
                null
            );
        }

        @Override
        public void revokeRefreshToken(String refreshToken, Instant now) {
            this.revokedToken = refreshToken;
            this.revokedAt = now;
        }
    }

    private static final class SequentialUserIdGenerator implements AuthUserIdGenerator {
        private int next = 1;

        @Override
        public String nextUserId() {
            return "usr_test_" + next++;
        }
    }
}
