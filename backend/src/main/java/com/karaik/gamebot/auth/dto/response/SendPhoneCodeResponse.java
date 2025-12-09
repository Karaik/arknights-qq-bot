package com.karaik.gamebot.auth.dto.response;

import lombok.Data;

/**
 * 发送手机验证码返回体。
 */
@Data
public class SendPhoneCodeResponse {
    private Integer status;
    private String type;
    private String msg;
    private Object data;
}
