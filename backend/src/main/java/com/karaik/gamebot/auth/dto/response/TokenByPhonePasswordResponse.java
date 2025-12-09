package com.karaik.gamebot.auth.dto.response;

import lombok.Data;

/**
 * 手机号+密码登录返回体。
 */
@Data
public class TokenByPhonePasswordResponse {
    private Integer status;
    private String type;
    private String msg;
    private TokenData data;

    @Data
    public static class TokenData {
        private String token;
    }
}
