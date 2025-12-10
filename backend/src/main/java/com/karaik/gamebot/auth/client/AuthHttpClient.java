package com.karaik.gamebot.auth.client;

import com.karaik.gamebot.auth.config.AuthProperties;
import com.karaik.gamebot.auth.sign.SklandSignatureHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 封装鹰角/森空岛 HTTP 调用，包含普通调用与带签名调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHttpClient {

    private final AuthProperties properties;
    private final SklandSignatureHelper signatureHelper;

    private WebClient webClient(String userAgent) {
        return WebClient.builder()
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    /**
     * 普通 JSON POST 调用（用于 send_phone_code、token_by_phone_code、grant）。
     */
    public <T> T postJson(String base_url, String path, Object body, Class<T> type, Duration timeout) {
        String url = base_url + path;
        try {
            return webClient(properties.getHypergryph().getUser_agent())
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(type)
                    .timeout(timeout)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("event=http.post.fail url={} status={} body={}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    /**
     * 带签名的 GET/POST 调用（用于 cred 校验等需要 Cred 头的接口）。
     */
    public <T> T signedGet(String base_url, String pathWithQuery, String token, String cred, Class<T> type, Duration timeout) {
        long ts = Instant.now().getEpochSecond();
        URI uri = URI.create(base_url + pathWithQuery);
        String path = uri.getPath();
        String query = uri.getQuery() == null ? "" : uri.getQuery();
        String combined = path + query;
        var headers = signatureHelper.generate(combined, "", ts, token, cred);
        try {
            return webClient(properties.getHypergryph().getUser_agent())
                    .get()
                    .uri(uri)
                    .headers(h -> {
                        h.setAll(headers.baseHeaders());
                        h.set("cred", headers.cred());
                        h.set("sign", headers.sign());
                    })
                    .retrieve()
                    .bodyToMono(type)
                    .timeout(timeout)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("event=http.get.fail url={} status={} body={}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    /**
     * 简单 GET 调用，允许自定义 Header，不做签名。
     */
    public <T> T getWithHeaders(String base_url, String path, Map<String, String> headers, Class<T> type, Duration timeout) {
        String url = base_url + path;
        try {
            return webClient(properties.getHypergryph().getUser_agent())
                    .get()
                    .uri(url)
                    .headers(h -> headers.forEach(h::set))
                    .retrieve()
                    .bodyToMono(type)
                    .timeout(timeout)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("event=http.get.fail url={} status={} body={}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public Duration timeout() {
        return Duration.ofSeconds(properties.getHypergryph().getTimeout_seconds());
    }
}
