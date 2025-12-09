package com.karaik.gamebot.auth.dto.request;

import lombok.Data;

/**
 * 校验 cred 的请求体，需要同时携带 token 生成签名。
 */
@Data
public class CheckCredRequest {
    /**
     * 森空岛 cred。
     */
    private String cred;
    /**
     * 生成签名所需的 token（来自 generate_cred_by_code）。
     */
    private String token;
}
