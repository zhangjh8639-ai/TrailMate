package com.trailmate.server.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class WechatHttpAuthClient implements WechatAuthClient {
    private final RestTemplate restTemplate;
    private final WechatAuthProperties properties;

    public WechatHttpAuthClient(
        RestTemplate restTemplate,
        WechatAuthProperties properties
    ) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public WechatIdentity exchangeAuthCode(String authCode, String state) {
        URI uri = UriComponentsBuilder
            .fromUriString(properties.apiBaseUrl())
            .path("/sns/oauth2/access_token")
            .queryParam("appid", properties.appId())
            .queryParam("secret", properties.appSecret())
            .queryParam("code", authCode)
            .queryParam("grant_type", "authorization_code")
            .build()
            .toUri();
        WechatAccessTokenResponse response = restTemplate.getForObject(uri, WechatAccessTokenResponse.class);
        if (response == null || response.openId() == null || response.openId().isBlank() || response.errCode() != null) {
            throw new IllegalArgumentException("WeChat auth code exchange failed.");
        }

        String displayName = fetchDisplayName(response.accessToken(), response.openId());

        return new WechatIdentity(response.openId(), response.unionId(), displayName);
    }

    private String fetchDisplayName(String accessToken, String openId) {
        if (accessToken == null || accessToken.isBlank()) {
            return "微信用户";
        }
        URI uri = UriComponentsBuilder
            .fromUriString(properties.apiBaseUrl())
            .path("/sns/userinfo")
            .queryParam("access_token", accessToken)
            .queryParam("openid", openId)
            .queryParam("lang", "zh_CN")
            .build()
            .toUri();
        WechatUserInfoResponse response = restTemplate.getForObject(uri, WechatUserInfoResponse.class);
        if (response == null || response.errCode() != null || response.nickName() == null || response.nickName().isBlank()) {
            return "微信用户";
        }
        return response.nickName();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WechatAccessTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("openid") String openId,
        @JsonProperty("unionid") String unionId,
        @JsonProperty("errcode") Integer errCode,
        @JsonProperty("errmsg") String errMsg
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WechatUserInfoResponse(
        @JsonProperty("nickname") String nickName,
        @JsonProperty("errcode") Integer errCode,
        @JsonProperty("errmsg") String errMsg
    ) {
    }
}
