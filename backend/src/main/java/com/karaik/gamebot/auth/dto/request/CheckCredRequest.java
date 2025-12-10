package com.karaik.gamebot.auth.dto.request;

import lombok.Data;

/**
 * 校验 cred 的请求体，仅需提供 cred。
 */
@Data
public class CheckCredRequest {
    /**
     * 森空岛 cred。
     */
    private String cred;
}
