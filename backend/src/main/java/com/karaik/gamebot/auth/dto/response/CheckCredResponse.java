package com.karaik.gamebot.auth.dto.response;

import lombok.Data;

/**
 * 校验 cred 返回体。
 */
@Data
public class CheckCredResponse {
    private Integer code;
    private String message;
    private Object data;
}
