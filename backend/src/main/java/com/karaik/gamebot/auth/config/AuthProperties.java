package com.karaik.gamebot.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 鉴权/签名相关的配置，支持通过 application.yml 全量覆盖。
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * 鹰角账号相关接口配置。
     */
    private Hypergryph hypergryph = new Hypergryph();

    /**
     * 森空岛相关接口与签名配置。
     */
    private Skland skland = new Skland();

    @Data
    public static class Hypergryph {
        /**
         * 基础域名，默认 https://as.hypergryph.com
         */
        private String baseUrl = "https://as.hypergryph.com";
        /**
         * 发送手机验证码接口路径。
         */
        private String sendPhoneCode = "/general/v1/send_phone_code";
        /**
         * 通过验证码换取 token 的接口路径。
         */
        private String tokenByPhoneCode = "/user/auth/v2/token_by_phone_code";
        /**
         * grant oauth_code 接口路径。
         */
        private String grant = "/user/oauth2/v2/grant";
        /**
         * 应用 appCode，官方固定值。
         */
        private String appCode = "4ca99fa6b56cc2ba";
        /**
         * 默认 User-Agent，可按需覆盖。
         */
        private String userAgent = "Skland/1.35.0 (com.hypergryph.skland; build:103500035; Android 32; ) Okhttp/4.11.0";
        /**
         * HTTP 请求超时时间（秒）。
         */
        private int timeoutSeconds = 10;
    }

    @Data
    public static class Skland {
        /**
         * 基础域名，默认 https://zonai.skland.com
         */
        private String baseUrl = "https://zonai.skland.com";
        /**
         * 根据 oauth_code 生成 cred/token 的接口路径。
         */
        private String credByCode = "/api/v1/user/auth/generate_cred_by_code";
        /**
         * 校验 cred 是否有效的接口路径。
         */
        private String checkCred = "/api/v1/user/check";
        /**
         * 签名用版本号 vName。
         */
        private String vName = "1.35.0";
        /**
         * 签名用平台标识。
         */
        private String platform = "1";
        /**
         * 设备 ID，可留空。
         */
        private String dId = "";
    }
}
