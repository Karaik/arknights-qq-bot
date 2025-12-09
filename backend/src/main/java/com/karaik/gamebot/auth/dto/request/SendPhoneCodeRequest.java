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
     * 短信类型，只允许 2（短信登录），其他值将被拒绝。
     */
    private Integer type = 2;
}
