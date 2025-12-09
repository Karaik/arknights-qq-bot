package com.karaik.gamebot.auth.dto.response;

import lombok.Data;

/**
 * 通过验证码获取登录 token 的返回体。
 */
@Data
public class TokenByPhoneCodeResponse {
    private Integer status;
    private String type;
    private String msg;
    private TokenData data;

    @Data
    public static class TokenData {
        private String token;
    }
}
