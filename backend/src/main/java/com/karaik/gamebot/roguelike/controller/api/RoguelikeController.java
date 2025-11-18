package com.karaik.gamebot.roguelike.controller.api;

import com.karaik.gamebot.common.api.ApiResponse;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeThemeSummary;
import com.karaik.gamebot.roguelike.service.RoguelikeService;
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
public class RoguelikeController {

    private final RoguelikeService service;
    private final String apiKey;

    public RoguelikeController(RoguelikeService service,
                               @Value("${roguelike.api-key:}") String apiKey) {
        this.service = service;
        this.apiKey = apiKey;
    }

    @GetMapping("/themes")
    public ApiResponse<List<RoguelikeThemeSummary>> listThemes(
            @RequestHeader(value = "X-API-KEY", required = false) String key) {
        verifyApiKey(key);
        return ApiResponse.success(service.listThemes());
    }

    @GetMapping("/{userKey}/{themeId}/analysis")
    public ApiResponse<RoguelikeAnalysisResult> getAnalysis(
            @RequestHeader(value = "X-API-KEY", required = false) String key,
            @PathVariable String userKey,
            @PathVariable String themeId,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
        verifyApiKey(key);
        RoguelikeAnalysisResult result = service.getAnalysis(userKey, themeId, refresh);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "暂无历史记录，请先刷新");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/{userKey}/refresh")
    public ApiResponse<RoguelikeAnalysisResult> refresh(
            @RequestHeader(value = "X-API-KEY", required = false) String key,
            @PathVariable String userKey,
            @RequestParam(value = "themeId", required = false) String themeId) {
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

