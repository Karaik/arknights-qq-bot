package com.karaik.gamebot.roguelike.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaik.gamebot.roguelike.config.RoguelikeApiProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Component
public class RoguelikeSignatureHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RoguelikeApiProperties properties;

    public RoguelikeSignatureHelper(RoguelikeApiProperties properties) {
        this.properties = properties;
    }

    public SignatureHeaders generateHeaders(String pathWithQuery, String body, long timestamp, String token) {
        Map<String, String> baseHeaders = Map.of(
                "platform", "1",
                "timestamp", String.valueOf(timestamp),
                "dId", "",
                "vName", properties.getApp().getVersionName()
        );

        String headersJson = toJson(baseHeaders);
        String payload = (body == null ? "" : body);
        String strToSign = pathWithQuery + payload + timestamp + headersJson;

        String hexDigest = hmacSha256Hex(token, strToSign);
        String sign = md5Hex(hexDigest);

        return new SignatureHeaders(baseHeaders, sign);
    }

    private String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize headers", e);
        }
    }

    private String hmacSha256Hex(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256", e);
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
            throw new IllegalStateException("MD5 algorithm not found", e);
        }
    }

    public record SignatureHeaders(Map<String, String> baseHeaders, String sign) {
    }
}
