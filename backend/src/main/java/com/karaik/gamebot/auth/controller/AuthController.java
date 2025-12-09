package com.karaik.gamebot.auth.controller;

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
import com.karaik.gamebot.auth.service.AuthService;
import com.karaik.gamebot.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.Supplier;

/**
 * 封装鹰角账号 / 森空岛鉴权链路的开放接口，供上层直接调用。
 * 每个接口均对应官方文档中的一步操作，便于排查问题和复用。
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "鉴权与凭证")
public class AuthController {

    private final AuthService authService;

    /**
     * 发送手机验证码（官方 send_phone_code）。
     */
    @Operation(summary = "发送手机验证码")
    @PostMapping("/send_phone_code")
    public ApiResponse<SendPhoneCodeResponse> sendPhoneCode(@RequestBody @Validated SendPhoneCodeRequest request) {
        log.info("event=send_phone_code phone={}", maskPhone(request.getPhone()));
        return wrap(() -> authService.sendPhoneCode(request));
    }

    /**
     * 使用验证码换取登录 token（官方 token_by_phone_code）。
     */
    @Operation(summary = "验证码换取登录token")
    @PostMapping("/token_by_phone_code")
    public ApiResponse<TokenByPhoneCodeResponse> tokenByPhoneCode(@RequestBody @Validated TokenByPhoneCodeRequest request) {
        log.info("event=token_by_phone_code phone={}", maskPhone(request.getPhone()));
        return wrap(() -> authService.tokenByPhoneCode(request));
    }

    /**
     * 手机号+密码直接获取登录 token。
     */
    @Operation(summary = "手机号+密码登录")
    @PostMapping("/token_by_phone_password")
    public ApiResponse<TokenByPhonePasswordResponse> tokenByPhonePassword(@RequestBody @Validated TokenByPhonePasswordRequest request) {
        log.info("event=token_by_phone_password phone={}", maskPhone(request.getPhone()));
        return wrap(() -> authService.tokenByPhonePassword(request));
    }

    /**
     * 使用登录 token 获取 oauth_code（官方 grant）。
     */
    @Operation(summary = "登录token换取oauth_code")
    @PostMapping("/grant")
    public ApiResponse<GrantResponse> grant(@RequestBody @Validated GrantRequest request) {
        log.info("event=grant_oauth");
        return wrap(() -> authService.grantOauth(request));
    }

    /**
     * 使用 oauth_code 生成 cred/token（官方 generate_cred_by_code）。
     */
    @Operation(summary = "oauth_code换取cred/token")
    @PostMapping("/generate_cred_by_code")
    public ApiResponse<CredByCodeResponse> generateCred(@RequestBody @Validated CredByCodeRequest request) {
        log.info("event=generate_cred_by_code");
        return wrap(() -> authService.generateCredByCode(request));
    }

    /**
     * 校验 cred 是否有效（官方 /user/check，需带签名）。
     */
    @Operation(summary = "校验cred有效性")
    @PostMapping("/user/check")
    public ApiResponse<CheckCredResponse> checkCred(@RequestBody @Validated CheckCredRequest request) {
        log.info("event=check_cred");
        return wrap(() -> authService.checkCred(request));
    }

    private <T> ApiResponse<T> wrap(Supplier<T> supplier) {
        try {
            return ApiResponse.success(supplier.get());
        } catch (IllegalArgumentException e) {
            log.warn("event=param.invalid msg={}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (WebClientResponseException e) {
            log.error("event=http.remote.fail status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ApiResponse.error(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("event=auth.unexpected", e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        int visible = Math.min(4, phone.length());
        return "****" + phone.substring(phone.length() - visible);
    }
}
