package com.karaik.gamebot.auth.dto.request;

import lombok.Data;

/**
 * 发送手机验证码请求体。
 */
@Data
public class SendPhoneCodeRequest {
    /**
     * 手机号，支持纯数字或含区号字符串。
     */
    private String phone;
    /**
     * 验证码类型，官方短信登录为 2。
     */
    private int type = 2;
}
