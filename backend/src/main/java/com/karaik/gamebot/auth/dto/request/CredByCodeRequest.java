package com.karaik.gamebot.auth.dto.request;

import lombok.Data;

/**
 * 使用 oauth_code 生成 cred/token 的请求体。
 */
@Data
public class CredByCodeRequest {
    /**
     * 授权类型，官方默认 1。
     */
    private int kind = 1;
    /**
     * oauth_code。
     */
    private String code;
}
