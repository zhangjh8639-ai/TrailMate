package com.trailmate.server.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trailmate.auth")
public class AuthProviderProperties {
    private final Wechat wechat = new Wechat();
    private final Persistence persistence = new Persistence();
    private final SmsCode smsCode = new SmsCode();
    private final SmsCodeStore smsCodeStore = new SmsCodeStore();

    public Wechat wechat() {
        return wechat;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public Persistence persistence() {
        return persistence;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public SmsCode smsCode() {
        return smsCode;
    }

    public SmsCode getSmsCode() {
        return smsCode;
    }

    public SmsCodeStore smsCodeStore() {
        return smsCodeStore;
    }

    public SmsCodeStore getSmsCodeStore() {
        return smsCodeStore;
    }

    public static class Persistence {
        private String mode = "memory";

        public String mode() {
            return mode;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class SmsCodeStore {
        private String mode = "memory";

        public String mode() {
            return mode;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class SmsCode {
        private String fixedCode = "";

        public String fixedCode() {
            return fixedCode;
        }

        public String getFixedCode() {
            return fixedCode;
        }

        public void setFixedCode(String fixedCode) {
            this.fixedCode = fixedCode;
        }
    }

    public static class Wechat {
        private String mode = "preview";
        private String appId = "";
        private String appSecret = "";
        private String apiBaseUrl = "https://api.weixin.qq.com";

        public String mode() {
            return mode;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String appId() {
            return appId;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String appSecret() {
            return appSecret;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String apiBaseUrl() {
            return apiBaseUrl;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }
    }
}
