package com.karaik.gamebot.skland.controller.api;

import com.karaik.gamebot.common.api.ApiResponse;
import com.karaik.gamebot.roguelike.account.RoguelikeAccountService;
import com.karaik.gamebot.roguelike.controller.api.dto.BindCredentialRequest;
import com.karaik.gamebot.skland.credential.SklandTokenStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "绑定/更新森空岛 Token 与 UID", security = @SecurityRequirement(name = "roguelikeApiKey"))
    public ApiResponse<Void> bind(
            @RequestHeader(value = "X-API-KEY", required = false)
            @Parameter(description = "API Key，用于鉴权") String key,
            @PathVariable @Parameter(description = "业务用户标识") String userKey,
            @RequestBody BindCredentialRequest body) {
        verifyApiKey(key);
        if (!StringUtils.hasText(body.getHyperToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hyperToken 不能为空");
        }
        tokenStore.saveToken(userKey, body.getHyperToken());
        if (StringUtils.hasText(body.getUid())) {
            accountService.bind(userKey, body.getUid());
        }
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
}

