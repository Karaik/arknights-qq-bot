package com.karaik.gamebot.roguelike.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@org.springframework.stereotype.Component
@ConfigurationProperties(prefix = "hypergryph")
public class RoguelikeApiProperties {

    @NotBlank
    private String token;

    private final Endpoint endpoint = new Endpoint();

    private final App app = new App();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public App getApp() {
        return app;
    }

    public static class Endpoint {
        private String grantUrl = "https://as.hypergryph.com/user/oauth2/v2/grant";
        private String credAuthUrl = "https://zonai.skland.com/api/v1/user/auth/generate_cred_by_code";
        private String bindingUrl = "https://zonai.skland.com/api/v1/game/player/binding";
        private String rogueInfoUrl = "https://zonai.skland.com/api/v1/game/arknights/rogue";

        public String getGrantUrl() {
            return grantUrl;
        }

        public void setGrantUrl(String grantUrl) {
            this.grantUrl = grantUrl;
        }

        public String getCredAuthUrl() {
            return credAuthUrl;
        }

        public void setCredAuthUrl(String credAuthUrl) {
            this.credAuthUrl = credAuthUrl;
        }

        public String getBindingUrl() {
            return bindingUrl;
        }

        public void setBindingUrl(String bindingUrl) {
            this.bindingUrl = bindingUrl;
        }

        public String getRogueInfoUrl() {
            return rogueInfoUrl;
        }

        public void setRogueInfoUrl(String rogueInfoUrl) {
            this.rogueInfoUrl = rogueInfoUrl;
        }
    }

    public static class App {
        private String appCode = "4ca99fa6b56cc2ba";
        private String userAgent = "Skland/1.35.0 (com.hypergryph.skland; build:103500035; Android 32; ) Okhttp/4.11.0";
        private String versionName = "1.35.0";

        public String getAppCode() {
            return appCode;
        }

        public void setAppCode(String appCode) {
            this.appCode = appCode;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }
    }
}
