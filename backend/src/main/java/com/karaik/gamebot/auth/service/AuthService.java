package com.karaik.gamebot.auth.service;

import com.karaik.gamebot.auth.client.AuthHttpClient;
import com.karaik.gamebot.auth.config.AuthProperties;
import com.karaik.gamebot.auth.dto.request.CheckCredRequest;
import com.karaik.gamebot.auth.dto.request.CredByCodeRequest;
import com.karaik.gamebot.auth.dto.request.GrantRequest;
import com.karaik.gamebot.auth.dto.request.SendPhoneCodeRequest;
import com.karaik.gamebot.auth.dto.request.TokenByPhonePasswordRequest;
import com.karaik.gamebot.auth.dto.request.TokenByPhoneCodeRequest;
import com.karaik.gamebot.auth.dto.response.CheckCredResponse;
import com.karaik.gamebot.auth.dto.response.CredByCodeResponse;
import com.karaik.gamebot.auth.dto.response.GrantResponse;
import com.karaik.gamebot.auth.dto.response.SendPhoneCodeResponse;
import com.karaik.gamebot.auth.dto.response.TokenByPhoneCodeResponse;
import com.karaik.gamebot.auth.dto.response.TokenByPhonePasswordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 封装鹰角/森空岛认证链路，提供分步接口供外部调用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthProperties properties;
    private final AuthHttpClient httpClient;
    /**
     * 手机验证码发送冷却记录，key 为手机号，value 为最近发送时间（秒级）。
     */
    private final Map<String, Long> phoneCodeCooldown = new ConcurrentHashMap<>();

    private Duration timeout() {
        return httpClient.timeout();
    }

    /**
     * 通过手机号+密码换取登录 token。
     * <p>业务说明：官方 token_by_phone_password，适合已有密码的账号直接登录。</p>
     */
    public TokenByPhonePasswordResponse tokenByPhonePassword(TokenByPhonePasswordRequest request) {
        validatePhone(request.getPhone());
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        String path = properties.getHypergryph().getToken_by_phone_password();
        var body = Map.of("phone", request.getPhone(), "password", request.getPassword());
        return httpClient.postJson(properties.getHypergryph().getBase_url(), path, body, TokenByPhonePasswordResponse.class, timeout());
    }

    /**
     * 发送手机验证码。
     * <p>业务说明：调用官方 send_phone_code，常用于验证码登录第一步。</p>
     */
    public SendPhoneCodeResponse sendPhoneCode(SendPhoneCodeRequest request) {
        validatePhone(request.getPhone());
        enforceCooldown(request.getPhone());
        if (request.getType() == null || request.getType() != 2) {
            throw new IllegalArgumentException("验证码类型仅支持 2（短信登录）");
        }
        String path = properties.getHypergryph().getSend_phone_code();
        var body = Map.of("phone", request.getPhone(), "type", request.getType());
        SendPhoneCodeResponse resp = httpClient.postJson(properties.getHypergryph().getBase_url(), path, body, SendPhoneCodeResponse.class, timeout());
        phoneCodeCooldown.put(request.getPhone(), Instant.now().getEpochSecond());
        return resp;
    }

    /**
     * 通过验证码换取登录 token。
     * <p>业务说明：用户输入验证码后调用官方 token_by_phone_code，返回的 token 可用于 grant。</p>
     */
    public TokenByPhoneCodeResponse tokenByPhoneCode(TokenByPhoneCodeRequest request) {
        validatePhone(request.getPhone());
        if (!StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        String path = properties.getHypergryph().getToken_by_phone_code();
        var body = Map.of("phone", request.getPhone(), "code", request.getCode());
        return httpClient.postJson(properties.getHypergryph().getBase_url(), path, body, TokenByPhoneCodeResponse.class, timeout());
    }

    /**
     * 使用 token 获取 oauth_code。
     * <p>业务说明：调用官方 grant，传入 appCode 固定值，返回 code 与 uid。</p>
     */
    public GrantResponse grantOauth(GrantRequest request) {
        if (!StringUtils.hasText(request.getToken())) {
            throw new IllegalArgumentException("token 不能为空");
        }
        String path = properties.getHypergryph().getGrant();
        var body = Map.of(
                "token", request.getToken(),
                "appCode", properties.getHypergryph().getApp_code(),
                "type", 0
        );
        return httpClient.postJson(properties.getHypergryph().getBase_url(), path, body, GrantResponse.class, timeout());
    }

    /**
     * 通过 oauth_code 获取 cred/token。
     * <p>业务说明：调用森空岛 generate_cred_by_code，返回 cred 与后续签名用的 token。</p>
     */
    public CredByCodeResponse generateCredByCode(CredByCodeRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("code 不能为空");
        }
        String path = properties.getSkland().getCred_by_code();
        var body = Map.of("kind", request.getKind(), "code", request.getCode());
        return httpClient.postJson(properties.getSkland().getBase_url(), path, body, CredByCodeResponse.class, timeout());
    }

    /**
     * 带签名校验 cred 有效性，需要 cred 与 token。
     * <p>业务说明：官方 /user/check 需携带 Cred、sign 等头，使用 generate_cred_by_code 返回的 token 计算签名。</p>
     */
    public CheckCredResponse checkCred(CheckCredRequest request) {
        if (!StringUtils.hasText(request.getCred()) || !StringUtils.hasText(request.getToken())) {
            throw new IllegalArgumentException("cred/token 不能为空");
        }
        String path = properties.getSkland().getCheck_cred();
        return httpClient.signedGet(properties.getSkland().getBase_url(), path, request.getToken(), request.getCred(), CheckCredResponse.class, timeout());
    }

    private void validatePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("手机号不能为空");
        }
    }

    /**
     * 短信冷却校验，若配置 <=0 则不生效。
     */
    private void enforceCooldown(String phone) {
        int cooldown = properties.getHypergryph().getPhone_code_cooldown_seconds();
        if (cooldown <= 0) {
            return;
        }
        long now = Instant.now().getEpochSecond();
        Long last = phoneCodeCooldown.get(phone);
        if (last != null) {
            long diff = now - last;
            if (diff < cooldown) {
                long wait = cooldown - diff;
                log.warn("event=phone_code.cooldown phone=****{} wait={}s", phone.substring(Math.max(0, phone.length() - 4)), wait);
                throw new IllegalArgumentException("验证码发送过于频繁，请等待 " + wait + " 秒后重试");
            }
        }
    }
}
