package com.karaik.gamebot.auth.dto.request;

import lombok.Data;

/**
 * 通过手机号+密码获取登录 token 的请求体。
 */
@Data
public class TokenByPhonePasswordRequest {
    /**
     * 手机号。
     */
    private String phone;
    /**
     * 登录密码（明文）。
     */
    private String password;
}
