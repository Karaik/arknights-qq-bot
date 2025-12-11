package com.karaik.gamebot.auth;

import com.karaik.gamebot.auth.dto.request.CredByCodeRequest;
import com.karaik.gamebot.auth.dto.request.GrantRequest;
import com.karaik.gamebot.auth.dto.request.SendPhoneCodeRequest;
import com.karaik.gamebot.auth.dto.request.TokenByPhoneCodeRequest;
import com.karaik.gamebot.auth.dto.request.TokenByPhonePasswordRequest;
import com.karaik.gamebot.auth.dto.response.*;
import com.karaik.gamebot.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 登录链路的集成测试。将获取 token 的两种方式（短信、密码）拆分，
 * token 获取后作为长期凭证单独输入，后续共用 grant -> generate_cred_by_code -> /user/check 的 pipeline。
 *
 * 注意：需要填入真实账号信息，未填则跳过，避免误调用官方接口。
 */
@Slf4j
@SpringBootTest
class AuthFlowTest {

    /**
     * 测试用账号信息（请在运行前填入实际值）
     */
    private static final String TEST_PHONE = "";      // 例如 13800000000
    private static final String TEST_SMS_CODE = "";   // 短信验证码
    private static final String TEST_PASSWORD = "";   // 密码登录用
    private static final String TEST_TOKEN = "";      // 已获取的登录 token（长期凭证）

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("短信验证码发送")
    void sendSmsCodeOnly() {
        Assumptions.assumeTrue(notBlank(TEST_PHONE), "未提供手机号，跳过发送验证码");
        SendPhoneCodeRequest sendReq = new SendPhoneCodeRequest();
        sendReq.setPhone(TEST_PHONE);
        sendReq.setType(2);
        var sendPhoneCodeResponse = authService.sendPhoneCode(sendReq);
        log.info("sendPhoneCodeResponse = {}", sendPhoneCodeResponse);
    }

    @Test
    @DisplayName("短信验证码换取 token（不继续后续链路）")
    void smsLoginGetTokenOnly() {
        Assumptions.assumeTrue(notBlank(TEST_PHONE), "未提供手机号，跳过短信登录测试");
        Assumptions.assumeTrue(notBlank(TEST_SMS_CODE), "未提供验证码，跳过短信登录测试");

        String token = loginBySmsWithoutSend(TEST_PHONE, TEST_SMS_CODE);
        log.info("token = {}", token);
        Assertions.assertTrue(notBlank(token), "token 为空");
    }

    @Test
    @DisplayName("密码登录获取 token（不继续后续链路）")
    void passwordLoginGetTokenOnly() {
        Assumptions.assumeTrue(notBlank(TEST_PHONE), "未提供手机号，跳过密码登录测试");
        Assumptions.assumeTrue(notBlank(TEST_PASSWORD), "未提供密码，跳过密码登录测试");

        String token = loginByPassword(TEST_PHONE, TEST_PASSWORD);
        log.info("token = {}", token);
        Assertions.assertTrue(notBlank(token), "token 为空");
    }

    @Test
    @DisplayName("已有 token -> grant -> cred -> check")
    void tokenPipelineFlow() {
        Assumptions.assumeTrue(notBlank(TEST_TOKEN), "未提供 token，跳过后续链路");
        runGrantCredCheck(TEST_TOKEN);
    }

    /**
     * 短信登录：使用外部提供的验证码 -> 返回 token（不包含发送环节）。
     */
    private String loginBySmsWithoutSend(String phone, String smsCode) {
        TokenByPhoneCodeRequest codeReq = new TokenByPhoneCodeRequest();
        codeReq.setPhone(phone);
        codeReq.setCode(smsCode);
        TokenByPhoneCodeResponse tokenResp = authService.tokenByPhoneCode(codeReq);
        return tokenResp.getData().getToken();
    }

    /**
     * 密码登录：直接返回 token。
     */
    private String loginByPassword(String phone, String password) {
        TokenByPhonePasswordRequest pwdReq = new TokenByPhonePasswordRequest();
        pwdReq.setPhone(phone);
        pwdReq.setPassword(password);
        TokenByPhonePasswordResponse pwdResp = authService.tokenByPhonePassword(pwdReq);
        log.info("pwdResp = {}", pwdResp);
        return pwdResp.getData().getToken();
    }

    /**
     * 后续共用链路：grant -> generate_cred_by_code -> /user/check 断言成功。
     */
    private void runGrantCredCheck(String token) {
        GrantRequest grantReq = new GrantRequest();
        grantReq.setToken(token);
        GrantResponse grantResp = authService.grantOauth(grantReq);
        String oauthCode = grantResp.getData().getCode();

        CredByCodeRequest credReq = new CredByCodeRequest();
        credReq.setCode(oauthCode);
        CredByCodeResponse credResp = authService.generateCredByCode(credReq);
        String cred = credResp.getData().getCred();

        var checkResp = authService.checkCred(cred);
        log.info("checkResp = {}", checkResp);
        Assertions.assertEquals(0, checkResp.getCode());
        Assertions.assertEquals("OK", checkResp.getMessage());
    }

    private boolean notBlank(String val) {
        return val != null && !val.isBlank();
    }
}
