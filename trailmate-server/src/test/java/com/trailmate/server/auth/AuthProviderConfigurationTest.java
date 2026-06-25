package com.trailmate.server.auth;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthProviderConfigurationTest {
    @Test
    void defaultWechatClientUsesPreviewModeWithoutCredentials() {
        AuthProviderConfiguration configuration = new AuthProviderConfiguration();
        AuthProviderProperties properties = new AuthProviderProperties();

        WechatAuthClient client = configuration.wechatAuthClient(new RestTemplate(), properties);

        assertInstanceOf(PreviewWechatAuthClient.class, client);
    }

    @Test
    void httpWechatClientRequiresAppCredentials() {
        AuthProviderConfiguration configuration = new AuthProviderConfiguration();
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.wechat().setMode("http");

        assertThrows(IllegalArgumentException.class, () ->
            configuration.wechatAuthClient(new RestTemplate(), properties)
        );
    }

    @Test
    void httpWechatClientCanBeCreatedWithCredentials() {
        AuthProviderConfiguration configuration = new AuthProviderConfiguration();
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.wechat().setMode("http");
        properties.wechat().setAppId("wx-app-id");
        properties.wechat().setAppSecret("wx-secret");

        WechatAuthClient client = configuration.wechatAuthClient(new RestTemplate(), properties);

        assertInstanceOf(WechatHttpAuthClient.class, client);
    }
}
