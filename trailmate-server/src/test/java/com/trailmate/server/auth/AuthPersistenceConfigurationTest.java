package com.trailmate.server.auth;

import com.trailmate.server.TrailMateServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthPersistenceConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TrailMateServerApplication.class)
        .withPropertyValues("spring.flyway.enabled=false");

    @Test
    void defaultPersistenceUsesInMemoryAuthImplementations() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthAccountRepository.class);
            assertThat(context).hasSingleBean(AuthSessionIssuer.class);
            assertThat(context).hasSingleBean(SmsCodeGenerator.class);
            assertThat(context).hasSingleBean(SmsCodeRepository.class);
            assertThat(context).hasSingleBean(SmsCodeCooldownRepository.class);
            assertThat(context).hasSingleBean(SmsCodeRateLimiter.class);
            assertThat(context).hasSingleBean(AuthAuditRecorder.class);
            assertThat(context).hasSingleBean(SmsCodeAttemptRecorder.class);
            assertThat(context.getBean(AuthAccountRepository.class))
                .isInstanceOf(InMemoryAuthAccountRepository.class);
            assertThat(context.getBean(AuthSessionIssuer.class))
                .isInstanceOf(RandomAuthSessionIssuer.class);
            assertThat(context.getBean(SmsCodeGenerator.class))
                .isInstanceOf(RandomSmsCodeGenerator.class);
            assertThat(context.getBean(SmsCodeRepository.class))
                .isInstanceOf(InMemorySmsCodeRepository.class);
            assertThat(context.getBean(SmsCodeCooldownRepository.class))
                .isInstanceOf(InMemorySmsCodeCooldownRepository.class);
            assertThat(context.getBean(SmsCodeRateLimiter.class))
                .isInstanceOf(InMemorySmsCodeRateLimiter.class);
            assertThat(context.getBean(AuthAuditRecorder.class))
                .isInstanceOf(NoopAuthAuditRecorder.class);
            assertThat(context.getBean(SmsCodeAttemptRecorder.class))
                .isInstanceOf(NoopSmsCodeAttemptRecorder.class);
        });
    }

    @Test
    void fixedSmsCodeCanBeConfiguredForLocalSmokeTests() {
        contextRunner
            .withPropertyValues("trailmate.auth.sms-code.fixed-code=123456")
            .run(context -> {
                assertThat(context).hasSingleBean(SmsCodeGenerator.class);
                assertThat(context.getBean(SmsCodeGenerator.class).nextCode()).isEqualTo("123456");
            });
    }

    @Test
    void redisSmsCodeStoreUsesRedisRepository() {
        contextRunner
            .withPropertyValues("trailmate.auth.sms-code-store.mode=redis")
            .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
            .run(context -> {
                assertThat(context).hasSingleBean(SmsCodeRepository.class);
                assertThat(context).hasSingleBean(SmsCodeCooldownRepository.class);
                assertThat(context).hasSingleBean(SmsCodeRateLimiter.class);
                assertThat(context.getBean(SmsCodeRepository.class))
                    .isInstanceOf(RedisSmsCodeRepository.class);
                assertThat(context.getBean(SmsCodeCooldownRepository.class))
                    .isInstanceOf(RedisSmsCodeCooldownRepository.class);
                assertThat(context.getBean(SmsCodeRateLimiter.class))
                    .isInstanceOf(RedisSmsCodeRateLimiter.class);
            });
    }

    @Test
    void jdbcPersistenceUsesJdbcAuthImplementations() {
        contextRunner
            .withPropertyValues(
                "trailmate.auth.persistence.mode=jdbc",
                "trailmate.auth.wechat.app-id=wx-app-id",
                "spring.datasource.url=jdbc:h2:mem:trailmate_auth_config;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AuthAccountRepository.class);
                assertThat(context).hasSingleBean(AuthSessionIssuer.class);
                assertThat(context).hasSingleBean(AuthAuditRecorder.class);
                assertThat(context).hasSingleBean(SmsCodeAttemptRecorder.class);
                assertThat(context.getBean(AuthAccountRepository.class))
                    .isInstanceOf(JdbcAuthAccountRepository.class);
                assertThat(context.getBean(AuthSessionIssuer.class))
                    .isInstanceOf(JdbcAuthSessionIssuer.class);
                assertThat(context.getBean(AuthAuditRecorder.class))
                    .isInstanceOf(JdbcAuthAuditRecorder.class);
                assertThat(context.getBean(SmsCodeAttemptRecorder.class))
                    .isInstanceOf(JdbcSmsCodeAttemptRecorder.class);
            });
    }

    @Test
    void jdbcPersistenceAppliesSchemaMigrations() {
        contextRunner
            .withPropertyValues(
                "trailmate.auth.persistence.mode=jdbc",
                "trailmate.auth.wechat.app-id=wx-app-id",
                "spring.flyway.enabled=true",
                "spring.flyway.locations=classpath:db/test-migration",
                "spring.datasource.url=jdbc:h2:mem:trailmate_auth_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver"
            )
            .run(context -> {
                JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);

                Integer userCount = jdbcTemplate.queryForObject("select count(*) from migration_probe", Integer.class);

                assertThat(userCount).isZero();
            });
    }

    @Test
    void authServiceUsesInjectedAccountRepositoryAndSessionIssuer() {
        CapturingAuthAccountRepository accountRepository = new CapturingAuthAccountRepository();
        CapturingAuthSessionIssuer sessionIssuer = new CapturingAuthSessionIssuer();

        contextRunner
            .withPropertyValues(
                "trailmate.auth.persistence.mode=test",
                "trailmate.auth.wechat.mode=preview"
            )
            .withBean(AuthAccountRepository.class, () -> accountRepository)
            .withBean(AuthSessionIssuer.class, () -> sessionIssuer)
            .run(context -> {
                AuthSessionResponse response = context.getBean(AuthService.class)
                    .loginWithWechat(new WechatLoginRequest("wx-code", "state"));

                assertThat(accountRepository.wechatOpenId).isEqualTo("wx_openid_preview");
                assertThat(accountRepository.wechatUnionId).isNull();
                assertThat(sessionIssuer.issuedProvider).isEqualTo(AuthProvider.WECHAT);
                assertThat(response.userId()).isEqualTo("usr_injected");
                assertThat(response.refreshToken()).isEqualTo("refresh_injected");
            });
    }

    private static final class CapturingAuthAccountRepository implements AuthAccountRepository {
        private String wechatOpenId;
        private String wechatUnionId;

        @Override
        public AuthAccount findOrCreateByPhone(String phoneNumber) {
            return new AuthAccount("usr_injected", phoneNumber, null, null);
        }

        @Override
        public AuthAccount findOrCreateByWechat(String openId, String unionId, String displayName) {
            this.wechatOpenId = openId;
            this.wechatUnionId = unionId;
            return new AuthAccount("usr_injected", null, openId, displayName);
        }
    }

    private static final class CapturingAuthSessionIssuer implements AuthSessionIssuer {
        private AuthProvider issuedProvider;

        @Override
        public AuthSessionResponse issueSession(AuthAccount account, AuthProvider provider, Instant now) {
            this.issuedProvider = provider;
            return new AuthSessionResponse(
                account.userId(),
                provider,
                "access_injected",
                "refresh_injected",
                now.plusSeconds(7200).toString(),
                account.phoneNumber(),
                account.wechatOpenId(),
                account.displayName()
            );
        }

        @Override
        public AuthSessionResponse refreshSession(String refreshToken, Instant now) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void revokeRefreshToken(String refreshToken, Instant now) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
