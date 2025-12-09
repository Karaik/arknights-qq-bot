package com.karaik.gamebot.auth.dto.response;

import lombok.Data;

/**
 * grant 接口返回体。
 */
@Data
public class GrantResponse {
    private Integer status;
    private String type;
    private String msg;
    private GrantData data;

    @Data
    public static class GrantData {
        private String code;
        private String uid;
    }
}
