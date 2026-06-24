package com.trailmate.server.auth;

import java.time.Instant;
import java.time.Clock;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Pattern MAINLAND_PHONE = Pattern.compile("^\\+861\\d{10}$");
    private static final Pattern SMS_CODE = Pattern.compile("^\\d{4,8}$");
    private static final int CODE_EXPIRES_SECONDS = 300;
    private static final int CODE_RETRY_AFTER_SECONDS = 60;
    private final SmsCodeRepository smsCodeRepository;
    private final SmsCodeCooldownRepository smsCodeCooldownRepository;
    private final SmsCodeRateLimiter smsCodeRateLimiter;
    private final SmsCodeSender smsCodeSender;
    private final SmsCodeGenerator smsCodeGenerator;
    private final WechatAuthClient wechatAuthClient;
    private final AuthAccountRepository authAccountRepository;
    private final AuthSessionIssuer authSessionIssuer;
    private final AuthAuditRecorder authAuditRecorder;
    private final SmsCodeAttemptRecorder smsCodeAttemptRecorder;
    private final Clock clock;

    public AuthService() {
        this(
            new InMemorySmsCodeRepository(),
            new InMemorySmsCodeCooldownRepository(),
            new InMemorySmsCodeRateLimiter(),
            new NoopSmsCodeSender(),
            new RandomSmsCodeGenerator(),
            new PreviewWechatAuthClient(),
            new InMemoryAuthAccountRepository(new RandomAuthUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            Clock.systemUTC()
        );
    }

    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        Clock clock
    ) {
        this(
            smsCodeRepository,
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            smsCodeSender,
            smsCodeGenerator,
            wechatAuthClient,
            new InMemoryAuthAccountRepository(new RandomAuthUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            clock
        );
    }

    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeCooldownRepository smsCodeCooldownRepository,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        Clock clock
    ) {
        this(
            smsCodeRepository,
            smsCodeCooldownRepository,
            new InMemorySmsCodeRateLimiter(),
            smsCodeSender,
            smsCodeGenerator,
            wechatAuthClient,
            new InMemoryAuthAccountRepository(new RandomAuthUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            clock
        );
    }

    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeCooldownRepository smsCodeCooldownRepository,
        SmsCodeRateLimiter smsCodeRateLimiter,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        Clock clock
    ) {
        this(
            smsCodeRepository,
            smsCodeCooldownRepository,
            smsCodeRateLimiter,
            smsCodeSender,
            smsCodeGenerator,
            wechatAuthClient,
            new InMemoryAuthAccountRepository(new RandomAuthUserIdGenerator()),
            new RandomAuthSessionIssuer(),
            clock
        );
    }

    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        AuthAccountRepository authAccountRepository,
        AuthSessionIssuer authSessionIssuer,
        Clock clock
    ) {
        this(
            smsCodeRepository,
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            smsCodeSender,
            smsCodeGenerator,
            wechatAuthClient,
            authAccountRepository,
            authSessionIssuer,
            clock
        );
    }

    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        AuthAccountRepository authAccountRepository,
        Clock clock
    ) {
        this(
            smsCodeRepository,
            new InMemorySmsCodeCooldownRepository(clock),
            new InMemorySmsCodeRateLimiter(),
            smsCodeSender,
            smsCodeGenerator,
            wechatAuthClient,
            authAccountRepository,
            new RandomAuthSessionIssuer(),
            clock
        );
    }

    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeCooldownRepository smsCodeCooldownRepository,
        SmsCodeRateLimiter smsCodeRateLimiter,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        AuthAccountRepository authAccountRepository,
        AuthSessionIssuer authSessionIssuer,
        Clock clock
    ) {
        this(
            smsCodeRepository,
            smsCodeCooldownRepository,
            smsCodeRateLimiter,
            smsCodeSender,
            smsCodeGenerator,
            wechatAuthClient,
            authAccountRepository,
            authSessionIssuer,
            new NoopAuthAuditRecorder(),
            new NoopSmsCodeAttemptRecorder(),
            clock
        );
    }

    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeCooldownRepository smsCodeCooldownRepository,
        SmsCodeRateLimiter smsCodeRateLimiter,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        AuthAccountRepository authAccountRepository,
        AuthSessionIssuer authSessionIssuer,
        AuthAuditRecorder authAuditRecorder,
        Clock clock
    ) {
        this(
            smsCodeRepository,
            smsCodeCooldownRepository,
            smsCodeRateLimiter,
            smsCodeSender,
            smsCodeGenerator,
            wechatAuthClient,
            authAccountRepository,
            authSessionIssuer,
            authAuditRecorder,
            new NoopSmsCodeAttemptRecorder(),
            clock
        );
    }

    @Autowired
    public AuthService(
        SmsCodeRepository smsCodeRepository,
        SmsCodeCooldownRepository smsCodeCooldownRepository,
        SmsCodeRateLimiter smsCodeRateLimiter,
        SmsCodeSender smsCodeSender,
        SmsCodeGenerator smsCodeGenerator,
        WechatAuthClient wechatAuthClient,
        AuthAccountRepository authAccountRepository,
        AuthSessionIssuer authSessionIssuer,
        AuthAuditRecorder authAuditRecorder,
        SmsCodeAttemptRecorder smsCodeAttemptRecorder,
        Clock clock
    ) {
        this.smsCodeRepository = smsCodeRepository;
        this.smsCodeCooldownRepository = smsCodeCooldownRepository;
        this.smsCodeRateLimiter = smsCodeRateLimiter;
        this.smsCodeSender = smsCodeSender;
        this.smsCodeGenerator = smsCodeGenerator;
        this.wechatAuthClient = wechatAuthClient;
        this.authAccountRepository = authAccountRepository;
        this.authSessionIssuer = authSessionIssuer;
        this.authAuditRecorder = authAuditRecorder;
        this.smsCodeAttemptRecorder = smsCodeAttemptRecorder;
        this.clock = clock;
    }

    public PhoneCodeResponse requestPhoneCode(PhoneCodeRequest request) {
        return requestPhoneCode(request, "local");
    }

    public PhoneCodeResponse requestPhoneCode(PhoneCodeRequest request, String clientAddress) {
        requirePhone(request.phoneNumber());
        Instant now = clock.instant();
        if (!smsCodeRateLimiter.tryAcquire(request.phoneNumber(), clientAddress, now)) {
            recordAudit(new AuthAuditEvent(
                "sms_code_requested",
                AuthProvider.PHONE,
                "failure",
                null,
                "rate_limited",
                request.phoneNumber(),
                null,
                clientAddress,
                now
            ));
            throw new IllegalArgumentException("SMS code request limit exceeded.");
        }
        if (!smsCodeCooldownRepository.tryAcquire(
            request.phoneNumber(),
            now.plusSeconds(CODE_RETRY_AFTER_SECONDS)
        )) {
            recordAudit(new AuthAuditEvent(
                "sms_code_requested",
                AuthProvider.PHONE,
                "failure",
                null,
                "cooldown",
                request.phoneNumber(),
                null,
                clientAddress,
                now
            ));
            throw new IllegalArgumentException("SMS code requested too recently.");
        }
        String code = smsCodeGenerator.nextCode();
        Instant expiresAt = now.plusSeconds(CODE_EXPIRES_SECONDS);
        smsCodeRepository.save(
            request.phoneNumber(),
            code,
            expiresAt
        );
        try {
            smsCodeSender.sendLoginCode(request.phoneNumber(), code);
        } catch (RuntimeException exception) {
            recordSmsCodeAttempt(new SmsCodeAttempt(
                request.phoneNumber(),
                "login_or_register",
                "noop",
                "failed",
                "sender_exception",
                exception.getMessage(),
                clientAddress,
                now,
                expiresAt
            ));
            throw exception;
        }
        recordSmsCodeAttempt(new SmsCodeAttempt(
            request.phoneNumber(),
            "login_or_register",
            "noop",
            "sent",
            null,
            null,
            clientAddress,
            now,
            expiresAt
        ));
        recordAudit(new AuthAuditEvent(
            "sms_code_requested",
            AuthProvider.PHONE,
            "success",
            null,
            null,
            request.phoneNumber(),
            null,
            clientAddress,
            now
        ));
        return new PhoneCodeResponse(request.phoneNumber(), CODE_EXPIRES_SECONDS, CODE_RETRY_AFTER_SECONDS);
    }

    public AuthSessionResponse loginWithPhone(PhoneLoginRequest request) {
        requirePhone(request.phoneNumber());
        if (!SMS_CODE.matcher(request.smsCode()).matches()) {
            recordAudit(new AuthAuditEvent(
                "phone_login_failed",
                AuthProvider.PHONE,
                "failure",
                null,
                "invalid_sms_code_format",
                request.phoneNumber(),
                null,
                null,
                clock.instant()
            ));
            throw new IllegalArgumentException("Invalid SMS code.");
        }
        if (!smsCodeRepository.verifyAndConsume(request.phoneNumber(), request.smsCode(), clock.instant())) {
            recordAudit(new AuthAuditEvent(
                "phone_login_failed",
                AuthProvider.PHONE,
                "failure",
                null,
                "invalid_sms_code",
                request.phoneNumber(),
                null,
                null,
                clock.instant()
            ));
            throw new IllegalArgumentException("Invalid SMS code.");
        }

        AuthAccount account = authAccountRepository.findOrCreateByPhone(request.phoneNumber());

        AuthSessionResponse session = authSessionIssuer.issueSession(account, AuthProvider.PHONE, clock.instant());
        recordAudit(new AuthAuditEvent(
            "phone_login_succeeded",
            AuthProvider.PHONE,
            "success",
            session.userId(),
            null,
            request.phoneNumber(),
            null,
            null,
            clock.instant()
        ));
        return session;
    }

    public AuthSessionResponse loginWithWechat(WechatLoginRequest request) {
        if (request.authCode().isBlank() || request.state().isBlank()) {
            recordAudit(new AuthAuditEvent(
                "wechat_login_failed",
                AuthProvider.WECHAT,
                "failure",
                null,
                "invalid_wechat_payload",
                null,
                null,
                null,
                clock.instant()
            ));
            throw new IllegalArgumentException("Invalid WeChat auth payload.");
        }
        WechatIdentity identity;
        try {
            identity = wechatAuthClient.exchangeAuthCode(request.authCode(), request.state());
        } catch (RuntimeException exception) {
            recordAudit(new AuthAuditEvent(
                "wechat_login_failed",
                AuthProvider.WECHAT,
                "failure",
                null,
                "wechat_exchange_failed",
                null,
                null,
                null,
                clock.instant()
            ));
            throw exception;
        }
        AuthAccount account = authAccountRepository.findOrCreateByWechat(
            identity.openId(),
            identity.unionId(),
            identity.displayName()
        );

        AuthSessionResponse session = authSessionIssuer.issueSession(account, AuthProvider.WECHAT, clock.instant());
        recordAudit(new AuthAuditEvent(
            "wechat_login_succeeded",
            AuthProvider.WECHAT,
            "success",
            session.userId(),
            null,
            null,
            identity.openId(),
            null,
            clock.instant()
        ));
        return session;
    }

    public AuthSessionResponse refreshSession(RefreshSessionRequest request) {
        if (request.refreshToken().isBlank()) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }
        return authSessionIssuer.refreshSession(request.refreshToken(), clock.instant());
    }

    public void logout(LogoutRequest request) {
        if (request.refreshToken().isBlank()) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }
        authSessionIssuer.revokeRefreshToken(request.refreshToken(), clock.instant());
    }

    private void requirePhone(String phoneNumber) {
        if (!MAINLAND_PHONE.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number.");
        }
    }

    private void recordAudit(AuthAuditEvent event) {
        authAuditRecorder.record(event);
    }

    private void recordSmsCodeAttempt(SmsCodeAttempt attempt) {
        smsCodeAttemptRecorder.record(attempt);
    }

}
