package com.karaik.gamebot.roguelike.service;

import com.karaik.gamebot.roguelike.client.RoguelikeApiException;
import com.karaik.gamebot.roguelike.client.RoguelikeHttpClient;
import com.karaik.gamebot.roguelike.domain.auth.AuthFlow;
import com.karaik.gamebot.roguelike.domain.auth.CredTokenResponse;
import com.karaik.gamebot.roguelike.domain.auth.OAuthCodeResponse;
import com.karaik.gamebot.roguelike.domain.binding.BindingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class RoguelikeAuthService {

    private final RoguelikeHttpClient httpClient;

    public RoguelikeAuthService(RoguelikeHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public AuthFlow authenticate(String userKey, String hyperToken) {
        if (!StringUtils.hasText(hyperToken)) {
            throw new RoguelikeApiException("缺少森空岛 Token，请先通过 /api/skland/credentials 进行绑定");
        }
        log.debug("开始认证 userKey={}，准备请求 oauth_code", userKey);
        OAuthCodeResponse codeResponse = httpClient.requestOAuthCode(hyperToken);
        if (codeResponse == null || codeResponse.data() == null || codeResponse.data().code() == null) {
            throw new RoguelikeApiException("Token 换取 oauth_code 失败");
        }
        log.debug("oauth_code 获取成功 userKey={} code={}", userKey, codeResponse.data().code());
        CredTokenResponse credResponse = httpClient.requestCredAndToken(codeResponse.data().code());
        if (credResponse == null || credResponse.data() == null) {
            throw new RoguelikeApiException("获取 cred/token 失败");
        }
        String cred = credResponse.data().cred();
        String token = credResponse.data().token();
        if (!StringUtils.hasText(cred) || !StringUtils.hasText(token)) {
            throw new RoguelikeApiException("返回的 cred/token 为空");
        }
        log.debug("准备请求 binding 列表 userKey={}", userKey);
        BindingResponse bindingResponse = httpClient.requestBindings(cred, token);
        String uid = bindingResponse.data().list().stream()
                .filter(game -> "arknights".equalsIgnoreCase(game.appCode()))
                .findFirst()
                .flatMap(game -> game.bindingList().stream().findFirst())
                .map(BindingResponse.Binding::uid)
                .orElseThrow(() -> new RoguelikeApiException("未找到明日方舟绑定账号"));
        log.info("认证成功 userKey={} resolvedUid={}", userKey, uid);
        return new AuthFlow(cred, token, uid);
    }
}
