package com.karaik.gamebot.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
         * 基础域名，需在配置文件指定。
         */
        @NotBlank
        private String baseUrl;
        /**
         * 手机号+密码登录接口路径。
         */
        @NotBlank
        private String tokenByPhonePassword;
        /**
         * 发送手机验证码接口路径。
         */
        @NotBlank
        private String sendPhoneCode;
        /**
         * 通过验证码换取 token 的接口路径。
         */
        @NotBlank
        private String tokenByPhoneCode;
        /**
         * grant oauth_code 接口路径。
         */
        @NotBlank
        private String grant;
        /**
         * 应用 appCode，官方固定值。
         */
        @NotBlank
        private String appCode;
        /**
         * 默认 User-Agent，可按需覆盖。
         */
        @NotBlank
        private String userAgent;
        /**
         * 内部接口鉴权的默认 API Key（全局 Header 校验），不填则关闭。
         */
        private String apiKey = "";
        /**
         * HTTP 请求超时时间（秒）。
         */
        @Positive
        private int timeoutSeconds;
    }

    @Data
    public static class Skland {
        /**
         * 基础域名，需在配置文件指定。
         */
        @NotBlank
        private String baseUrl;
        /**
         * 根据 oauth_code 生成 cred/token 的接口路径。
         */
        @NotBlank
        private String credByCode;
        /**
         * 校验 cred 是否有效的接口路径。
         */
        @NotBlank
        private String checkCred;
        /**
         * 签名用版本号 vName。
         */
        @NotBlank
        private String vName;
        /**
         * 签名用平台标识。
         */
        @NotBlank
        private String platform;
        /**
         * 设备 ID，可留空。
         */
        private String dId = "";
    }
}
