package com.karaik.gamebot.auth.sign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaik.gamebot.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 生成森空岛接口所需签名头。规则与示例 Python 保持一致。
 */
@Component
@RequiredArgsConstructor
public class SklandSignatureHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AuthProperties properties;

    /**
     * 生成签名头。
     *
     * @param pathWithQuery 不带域名的 path+query（query 不含 ?，可为空字符串）
     * @param bodyJson      请求体 JSON 字符串，GET 时为空字符串
     * @param timestamp     秒级时间戳
     * @param token         作为 HMAC 密钥的 token
     * @param cred          header 中要携带的 cred 值
     * @return 包含基础头与 sign 的对象
     */
    public SignatureHeaders generate(String pathWithQuery, String bodyJson, long timestamp, String token, String cred) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("platform", properties.getSkland().getPlatform());
        headers.put("timestamp", String.valueOf(timestamp));
        headers.put("dId", properties.getSkland().getDId());
        headers.put("vName", properties.getSkland().getVName());

        String headersJson = toJson(headers);
        String toSign = pathWithQuery + bodyJson + timestamp + headersJson;
        String hmacHex = hmacSha256Hex(token, toSign);
        String sign = md5Hex(hmacHex);

        return new SignatureHeaders(headers, sign, cred);
    }

    private String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化签名头失败", e);
        }
    }

    private String hmacSha256Hex(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("计算 HMAC-SHA256 失败", e);
        }
    }

    private String md5Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }

    public record SignatureHeaders(Map<String, String> baseHeaders, String sign, String cred) {
    }
}
