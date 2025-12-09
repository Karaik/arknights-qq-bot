package com.karaik.gamebot.auth.dto.request;

import lombok.Data;

/**
 * 通过手机验证码换取登录 token 的请求体。
 */
@Data
public class TokenByPhoneCodeRequest {
    /**
     * 手机号。
     */
    private String phone;
    /**
     * 短信验证码。
     */
    private String code;
}
