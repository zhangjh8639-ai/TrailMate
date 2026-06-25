package com.trailmate.server.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class WechatHttpAuthClientTest {
    @Test
    void exchangeAuthCodeCallsWechatAccessTokenEndpointAndMapsOpenId() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        WechatAuthProperties properties = new WechatAuthProperties(
            "wx-app-id",
            "wx-secret",
            "https://api.weixin.qq.com"
        );
        WechatHttpAuthClient client = new WechatHttpAuthClient(restTemplate, properties);
        server.expect(requestTo(
                "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=wx-app-id&secret=wx-secret&code=wx-auth-code&grant_type=authorization_code"
            ))
            .andExpect(method(GET))
            .andRespond(withSuccess(
                """
                    {
                      "expires_in": 7200,
                      "refresh_token": "wx-refresh-token",
                      "openid": "openid-from-wechat",
                      "scope": "snsapi_userinfo",
                      "unionid": "unionid-from-wechat"
                    }
                    """,
                MediaType.APPLICATION_JSON
            ));

        WechatIdentity identity = client.exchangeAuthCode("wx-auth-code", "nonce");

        assertEquals("openid-from-wechat", identity.openId());
        assertEquals("unionid-from-wechat", identity.unionId());
        assertEquals("微信用户", identity.displayName());
        server.verify();
    }

    @Test
    void exchangeAuthCodeFetchesWechatUserInfoWhenAccessTokenIsAvailable() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        WechatHttpAuthClient client = new WechatHttpAuthClient(
            restTemplate,
            new WechatAuthProperties("wx-app-id", "wx-secret", "https://api.weixin.qq.com")
        );
        server.expect(requestTo(
                "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=wx-app-id&secret=wx-secret&code=wx-auth-code&grant_type=authorization_code"
            ))
            .andExpect(method(GET))
            .andRespond(withSuccess(
                """
                    {
                      "access_token": "wx-access-token",
                      "openid": "openid-from-wechat",
                      "unionid": "unionid-from-token"
                    }
                    """,
                MediaType.APPLICATION_JSON
            ));
        server.expect(requestTo(
                "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=wx-access-token&openid=openid-from-wechat&lang=zh_CN"
            ))
            .andExpect(method(GET))
            .andRespond(withSuccess(
                """
                    {
                      "openid": "openid-from-wechat",
                      "nickname": "山野用户",
                      "unionid": "unionid-from-userinfo"
                    }
                    """,
                MediaType.APPLICATION_JSON
            ));

        WechatIdentity identity = client.exchangeAuthCode("wx-auth-code", "nonce");

        assertEquals("openid-from-wechat", identity.openId());
        assertEquals("unionid-from-token", identity.unionId());
        assertEquals("山野用户", identity.displayName());
        server.verify();
    }

    @Test
    void exchangeAuthCodeFailsFastWhenWechatReturnsError() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        WechatHttpAuthClient client = new WechatHttpAuthClient(
            restTemplate,
            new WechatAuthProperties("wx-app-id", "wx-secret", "https://api.weixin.qq.com")
        );
        server.expect(requestTo(
                "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=wx-app-id&secret=wx-secret&code=bad-code&grant_type=authorization_code"
            ))
            .andRespond(withSuccess(
                """
                    {
                      "errcode": 40029,
                      "errmsg": "invalid code"
                    }
                    """,
                MediaType.APPLICATION_JSON
            ));

        assertThrows(IllegalArgumentException.class, () -> client.exchangeAuthCode("bad-code", "nonce"));
        server.verify();
    }

    @Test
    void propertiesRejectBlankCredentialsBeforeNetworkUse() {
        assertThrows(IllegalArgumentException.class, () ->
            new WechatAuthProperties("", "secret", "https://api.weixin.qq.com")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new WechatAuthProperties("appid", "", "https://api.weixin.qq.com")
        );
    }
}
