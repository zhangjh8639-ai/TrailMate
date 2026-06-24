package com.trailmate.server.auth;

import java.time.Clock;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(AuthProviderProperties.class)
public class AuthProviderConfiguration {
    private static final Pattern SMS_CODE = Pattern.compile("^\\d{4,8}$");

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SmsCodeGenerator smsCodeGenerator(AuthProviderProperties properties) {
        String fixedCode = properties.smsCode().fixedCode();
        if (fixedCode == null || fixedCode.isBlank()) {
            return new RandomSmsCodeGenerator();
        }
        if (!SMS_CODE.matcher(fixedCode).matches()) {
            throw new IllegalArgumentException("Fixed SMS code must contain 4 to 8 digits.");
        }
        return () -> fixedCode;
    }

    @Bean
    public WechatAuthClient wechatAuthClient(
        RestTemplate restTemplate,
        AuthProviderProperties properties
    ) {
        if ("preview".equalsIgnoreCase(properties.wechat().mode())) {
            return new PreviewWechatAuthClient();
        }
        if (!"http".equalsIgnoreCase(properties.wechat().mode())) {
            throw new IllegalArgumentException("Unsupported WeChat auth mode.");
        }
        return new WechatHttpAuthClient(
            restTemplate,
            new WechatAuthProperties(
                properties.wechat().appId(),
                properties.wechat().appSecret(),
                properties.wechat().apiBaseUrl()
            )
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "trailmate.auth.persistence", name = "mode", havingValue = "jdbc")
    public AuthAccountRepository jdbcAuthAccountRepository(
        JdbcTemplate jdbcTemplate,
        AuthUserIdGenerator userIdGenerator,
        Clock clock,
        AuthProviderProperties properties
    ) {
        return new JdbcAuthAccountRepository(
            jdbcTemplate,
            userIdGenerator,
            clock,
            properties.wechat().appId()
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "trailmate.auth.persistence", name = "mode", havingValue = "jdbc")
    public AuthSessionIssuer jdbcAuthSessionIssuer(JdbcTemplate jdbcTemplate) {
        return new JdbcAuthSessionIssuer(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = "trailmate.auth.persistence", name = "mode", havingValue = "jdbc")
    public AuthAuditRecorder jdbcAuthAuditRecorder(JdbcTemplate jdbcTemplate) {
        return new JdbcAuthAuditRecorder(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = "trailmate.auth.persistence", name = "mode", havingValue = "jdbc")
    public SmsCodeAttemptRecorder jdbcSmsCodeAttemptRecorder(JdbcTemplate jdbcTemplate) {
        return new JdbcSmsCodeAttemptRecorder(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AuthAuditRecorder.class)
    public AuthAuditRecorder noopAuthAuditRecorder() {
        return new NoopAuthAuditRecorder();
    }

    @Bean
    @ConditionalOnMissingBean(SmsCodeAttemptRecorder.class)
    public SmsCodeAttemptRecorder noopSmsCodeAttemptRecorder() {
        return new NoopSmsCodeAttemptRecorder();
    }
}
