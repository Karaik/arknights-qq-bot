package com.karaik.gamebot.roguelike.controller.api;

import com.karaik.gamebot.common.api.ApiResponse;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeThemeSummary;
import com.karaik.gamebot.roguelike.service.RoguelikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/roguelike")
@Tag(name = "Roguelike API", description = "肉鸽主题刷新、查询接口")
public class RoguelikeController {

    private final RoguelikeService service;
    private final String apiKey;

    public RoguelikeController(RoguelikeService service,
                               @Value("${roguelike.api-key:}") String apiKey) {
        this.service = service;
        this.apiKey = apiKey;
    }

    @GetMapping("/themes")
    @Operation(
            summary = "获取所有可用的肉鸽主题",
            security = @SecurityRequirement(name = "roguelikeApiKey")
    )
    public ApiResponse<List<RoguelikeThemeSummary>> listThemes(
            @RequestHeader(value = "X-API-KEY", required = false)
            @Parameter(description = "API Key，用于鉴权") String key) {
        verifyApiKey(key);
        return ApiResponse.success(service.listThemes());
    }

    @GetMapping("/{userKey}/{themeId}/analysis")
    @Operation(
        summary = "获取指定主题的分析数据",
        security = @SecurityRequirement(name = "roguelikeApiKey")
    )
    public ApiResponse<RoguelikeAnalysisResult> getAnalysis(
            @RequestHeader(value = "X-API-KEY", required = false)
            @Parameter(description = "API Key，用于鉴权") String key,
            @PathVariable @Parameter(description = "业务用户标识，可映射到多个 UID") String userKey,
            @PathVariable @Parameter(description = "主题 ID 或中文名称") String themeId,
            @RequestParam(value = "refresh", defaultValue = "false")
            @Parameter(description = "是否强制刷新后再返回数据") boolean refresh) {
        verifyApiKey(key);
        RoguelikeAnalysisResult result = service.getAnalysis(userKey, themeId, refresh);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "暂无历史记录，请先刷新");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/{userKey}/refresh")
    @Operation(
        summary = "主动拉取最新的官方数据并生成分析结果",
        security = @SecurityRequirement(name = "roguelikeApiKey")
    )
    public ApiResponse<RoguelikeAnalysisResult> refresh(
            @RequestHeader(value = "X-API-KEY", required = false)
            @Parameter(description = "API Key，用于鉴权") String key,
            @PathVariable @Parameter(description = "业务用户标识，可映射到多个 UID") String userKey,
            @RequestParam(value = "themeId", required = false)
            @Parameter(description = "指定主题 ID，不传则使用官方返回的当前主题") String themeId) {
        verifyApiKey(key);
        return ApiResponse.success(service.refreshAndAnalyze(userKey, themeId));
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
