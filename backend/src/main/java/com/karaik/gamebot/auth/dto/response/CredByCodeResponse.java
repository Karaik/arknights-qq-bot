package com.karaik.gamebot.auth.dto.response;

import lombok.Data;

/**
 * 根据 oauth_code 生成 cred/token 的返回体。
 */
@Data
public class CredByCodeResponse {
    private Integer code;
    private String message;
    private CredData data;

    @Data
    public static class CredData {
        private String cred;
        private String token;
        private String userId;
    }
}
