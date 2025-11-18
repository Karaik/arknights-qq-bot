package com.karaik.gamebot.skland.controller.api;

import com.karaik.gamebot.common.api.ApiResponse;
import com.karaik.gamebot.roguelike.account.RoguelikeAccountService;
import com.karaik.gamebot.roguelike.controller.api.dto.BindCredentialRequest;
import com.karaik.gamebot.skland.credential.SklandTokenStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/skland/credentials")
@Tag(name = "Skland Credentials", description = "森空岛 Token 与 UID 绑定接口")
public class SklandCredentialController {

    private final SklandTokenStore tokenStore;
    private final RoguelikeAccountService accountService;
    private final String apiKey;

    public SklandCredentialController(SklandTokenStore tokenStore,
                                      RoguelikeAccountService accountService,
                                      @Value("${roguelike.api-key:}") String apiKey) {
        this.tokenStore = tokenStore;
        this.accountService = accountService;
        this.apiKey = apiKey;
    }

    @PostMapping("/{userKey}")
    @Operation(
            summary = "绑定/更新森空岛 Token 与 UID",
            security = {
                    @SecurityRequirement(name = "roguelikeApiKey"),
                    @SecurityRequirement(name = "sklandToken")
            }
    )
    public ApiResponse<Void> bind(
            @RequestHeader(value = "X-API-KEY", required = false)
            @Parameter(description = "API Key，用于鉴权", example = "local-dev-key") String key,
            @RequestHeader(value = "X-SKLAND-TOKEN", required = true)
            @Parameter(description = "森空岛 hyperToken，建议直接从 Skland 抓包复制") String hyperToken,
            @PathVariable @Parameter(description = "业务用户标识") String userKey,
            @RequestBody(required = false) BindCredentialRequest body) {
        verifyApiKey(key);
        tokenStore.saveToken(userKey, hyperToken);
        if (body != null && StringUtils.hasText(body.getUid())) {
            accountService.bind(userKey, body.getUid());
        }
        log.info("成功绑定森空岛 Token userKey={} uidProvided={} tokenPreview={}",
                userKey,
                body != null && StringUtils.hasText(body.getUid()),
                maskToken(hyperToken));
        return ApiResponse.success(null);
    }

    private void verifyApiKey(String provided) {
        if (!StringUtils.hasText(apiKey)) {
            return;
        }
        if (!apiKey.equals(provided)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效的 API Key");
        }
    }

    private String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return "empty";
        }
        if (token.length() <= 8) {
            return token;
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
