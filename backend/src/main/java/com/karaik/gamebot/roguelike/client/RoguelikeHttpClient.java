package com.karaik.gamebot.roguelike.client;

import com.karaik.gamebot.roguelike.config.RoguelikeApiProperties;
import com.karaik.gamebot.roguelike.client.RoguelikeApiException;
import com.karaik.gamebot.roguelike.domain.auth.CredTokenResponse;
import com.karaik.gamebot.roguelike.domain.auth.OAuthCodeResponse;
import com.karaik.gamebot.roguelike.domain.binding.BindingResponse;
import com.karaik.gamebot.roguelike.domain.rogue.RogueInfoResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RoguelikeHttpClient {

    private final WebClient webClient;
    private final RoguelikeApiProperties properties;
    private final RoguelikeSignatureHelper signatureHelper;

    public RoguelikeHttpClient(RoguelikeApiProperties properties,
                               RoguelikeSignatureHelper signatureHelper) {
        this.properties = properties;
        this.signatureHelper = signatureHelper;
        this.webClient = WebClient.builder()
                .defaultHeader("User-Agent", properties.getApp().getUserAgent())
                .build();
    }

    public OAuthCodeResponse requestOAuthCode(String hyperToken) {
        log.debug("POST grant-url 请求 oauth_code");
        return postJson(properties.getEndpoint().getGrantUrl(),
                Map.of("token", hyperToken, "appCode", properties.getApp().getAppCode(), "type", 0),
                OAuthCodeResponse.class);
    }

    public CredTokenResponse requestCredAndToken(String code) {
        log.debug("POST cred-auth-url 请求 cred/token");
        return postJson(properties.getEndpoint().getCredAuthUrl(),
                Map.of("kind", 1, "code", code),
                CredTokenResponse.class);
    }

    public BindingResponse requestBindings(String cred, String token) {
        log.debug("GET binding-url 请求绑定信息");
        return signedGet(properties.getEndpoint().getBindingUrl(),
                cred,
                token,
                BindingResponse.class);
    }

    public RogueInfoResponse requestRogueInfo(String cred, String token, String uid) {
        String url = properties.getEndpoint().getRogueInfoUrl() + "?uid=" + uid;
        log.debug("GET rogue-info-url 请求 uid={} 数据", uid);
        return signedGet(url, cred, token, RogueInfoResponse.class);
    }

    private <T> T postJson(String url, Object body, Class<T> type) {
        try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(type)
                    .block(optionalTimeout());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("POST 调用失败 url={} status={} body={}", url, e.getStatusCode(), responseBody);
            throw new RoguelikeApiException("POST request failed: " + url + ", status=" + e.getStatusCode()
                    + ", body=" + responseBody, e);
        } catch (Exception e) {
            log.error("POST 调用异常 url={}", url, e);
            throw new RoguelikeApiException("POST request failed: " + url, e);
        }
    }

    private <T> T signedGet(String url, String cred, String token, Class<T> type) {
        URI uri = URI.create(url);
        String pathWithQuery = uri.getPath() + (uri.getQuery() != null ? uri.getQuery() : "");
        long timestamp = Instant.now().getEpochSecond();
        RoguelikeSignatureHelper.SignatureHeaders headers = signatureHelper.generateHeaders(pathWithQuery, "", timestamp, token);

        try {
            return webClient.get()
                    .uri(url)
                    .headers(httpHeaders -> {
                        httpHeaders.setAll(headers.baseHeaders());
                        httpHeaders.set("sign", headers.sign());
                        httpHeaders.set("cred", cred);
                    })
                    .retrieve()
                    .bodyToMono(type)
                    .block(optionalTimeout());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("GET 调用失败 url={} status={} body={}", url, e.getStatusCode(), responseBody);
            throw new RoguelikeApiException("GET request failed: " + url + ", status=" + e.getStatusCode()
                    + ", body=" + responseBody, e);
        } catch (Exception e) {
            log.error("GET 调用异常 url={}", url, e);
            throw new RoguelikeApiException("GET request failed: " + url, e);
        }
    }

    private Duration optionalTimeout() {
        return Duration.ofSeconds(10);
    }
}
