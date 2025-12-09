package com.karaik.gamebot.auth.dto.request;

import lombok.Data;

/**
 * 使用登录 token 换取 oauth_code 的请求体。
 */
@Data
public class GrantRequest {
    /**
     * 登录 token。
     */
    private String token;
}
