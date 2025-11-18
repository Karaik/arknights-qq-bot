package com.karaik.gamebot.roguelike.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaik.gamebot.roguelike.account.RoguelikeAccountService;
import com.karaik.gamebot.roguelike.client.RoguelikeApiException;
import com.karaik.gamebot.roguelike.client.RoguelikeHttpClient;
import com.karaik.gamebot.roguelike.config.RoguelikeConfigException;
import com.karaik.gamebot.roguelike.config.RoguelikeThemeConfig;
import com.karaik.gamebot.roguelike.config.RoguelikeThemeRegistry;
import com.karaik.gamebot.skland.credential.SklandTokenStore;
import com.karaik.gamebot.roguelike.domain.auth.AuthFlow;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeThemeSummary;
import com.karaik.gamebot.roguelike.repository.RoguelikeRunRepository;
import com.karaik.gamebot.roguelike.theme.api.RoguelikeThemeAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RoguelikeService {

    private final RoguelikeAuthService authService;
    private final RoguelikeHttpClient httpClient;
    private final RoguelikeRunRepository repository;
    private final RoguelikeThemeRegistry registry;
    private final RoguelikeAccountService accountService;
    private final SklandTokenStore tokenStore;
    private final List<RoguelikeThemeAnalyzer> analyzers;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration cacheTtl = Duration.ofMinutes(5);

    public RoguelikeService(RoguelikeAuthService authService,
                            RoguelikeHttpClient httpClient,
                            RoguelikeRunRepository repository,
                            RoguelikeThemeRegistry registry,
                            RoguelikeAccountService accountService,
                            SklandTokenStore tokenStore,
                            List<RoguelikeThemeAnalyzer> analyzers) {
        this.authService = authService;
        this.httpClient = httpClient;
        this.repository = repository;
        this.registry = registry;
        this.accountService = accountService;
        this.tokenStore = tokenStore;
        this.analyzers = analyzers;
    }

    public RoguelikeAnalysisResult refreshAndAnalyze(String userKey, String themeIdOrName) {
        String uid = accountService.resolveUid(userKey);
        String hyperToken = tokenStore.getToken(userKey);
        if (!StringUtils.hasText(hyperToken)) {
            log.warn("userKey={} 未绑定森空岛 Token，拒绝刷新", userKey);
            throw new RoguelikeApiException("userKey 未绑定森空岛 Token，请先调用 /api/skland/credentials/" + userKey);
        }
        log.info("刷新肉鸽数据 userKey={} uid={} themeId={}", userKey, uid, themeIdOrName);
        AuthFlow flow = authService.authenticate(userKey, hyperToken);
        var response = httpClient.requestRogueInfo(flow.cred(), flow.token(), flow.uid());
        Map<String, Object> data = response.data();
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            log.info("credentials: {} ", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }

        RoguelikeThemeConfig config = registry.getRequired(
                Optional.ofNullable(themeIdOrName).orElseGet(() -> detectThemeId(data))
        );
        Map<String, Object> topic = selectTopic(data, config);
        List<Map<String, Object>> records = extractRecords(data);

        repository.saveRuns(uid, config.getThemeId(), records);
        RoguelikeAnalysisResult result = analyzeFromRepository(uid, config, topic);
        cache.put(cacheKey(uid, config.getThemeId()), new CacheEntry(result, Instant.now().plus(cacheTtl)));
        return result;
    }

    public RoguelikeAnalysisResult getAnalysis(String userKey, String themeIdOrName, boolean refresh) {
        if (refresh) {
            return refreshAndAnalyze(userKey, themeIdOrName);
        }
        String uid = accountService.resolveUid(userKey);
        RoguelikeThemeConfig config = registry.getRequired(themeIdOrName);
        return getCachedOrBuild(uid, config);
    }

    public List<RoguelikeAnalysisResult> listAnalyses(String userKey) {
        String uid = accountService.resolveUid(userKey);
        List<RoguelikeAnalysisResult> results = new ArrayList<>();
        for (RoguelikeThemeConfig config : registry.listThemes()) {
            RoguelikeAnalysisResult result = getCachedOrBuild(uid, config);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    public List<RoguelikeThemeSummary> listThemes() {
        return registry.listThemes().stream()
                .map(cfg -> new RoguelikeThemeSummary(cfg.getThemeId(), cfg.getName()))
                .toList();
    }

    private RoguelikeAnalysisResult getCachedOrBuild(String uid, RoguelikeThemeConfig config) {
        CacheEntry entry = cache.get(cacheKey(uid, config.getThemeId()));
        if (entry != null && entry.expireAt().isAfter(Instant.now())) {
            return entry.result();
        }
        RoguelikeAnalysisResult result = analyzeFromRepository(uid, config, fallbackTopic(config));
        if (result != null) {
            cache.put(cacheKey(uid, config.getThemeId()), new CacheEntry(result, Instant.now().plus(cacheTtl)));
        }
        return result;
    }

    private RoguelikeAnalysisResult analyzeFromRepository(String uid, RoguelikeThemeConfig config, Map<String, Object> topic) {
        List<Map<String, Object>> history = repository.listRuns(uid, config.getThemeId());
        if (history.isEmpty()) {
            return null;
        }
        RoguelikeThemeAnalyzer analyzer = analyzers.stream()
                .filter(it -> it.supports(config.getThemeId()) || it.supports(config.getName()))
                .findFirst()
                .orElseThrow(() -> new RoguelikeConfigException("Analyzer not found for theme: " + config.getThemeId()));
        return analyzer.analyze(topic, history);
    }

    private Map<String, Object> selectTopic(Map<String, Object> data, RoguelikeThemeConfig config) {
        List<Map<String, Object>> topics = extractTopics(data);
        return topics.stream()
                .filter(topic -> Objects.equals(topic.get("id"), config.getThemeId())
                        || Objects.equals(topic.get("name"), config.getName()))
                .findFirst()
                .orElseGet(() -> fallbackTopic(config));
    }

    private Map<String, Object> fallbackTopic(RoguelikeThemeConfig config) {
        return Map.of("id", config.getThemeId(), "name", config.getName());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRecords(Map<String, Object> data) {
        Map<String, Object> history = (Map<String, Object>) data.getOrDefault("history", Collections.emptyMap());
        return (List<Map<String, Object>>) history.getOrDefault("records", Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractTopics(Map<String, Object> data) {
        return (List<Map<String, Object>>) data.getOrDefault("topics", Collections.emptyList());
    }

    private String detectThemeId(Map<String, Object> data) {
        return extractTopics(data).stream()
                .filter(topic -> Boolean.TRUE.equals(topic.get("isSelected")))
                .map(topic -> Objects.toString(topic.get("id"), null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RoguelikeConfigException("Unable to detect selected theme from API response"));
    }

    private String cacheKey(String uid, String themeId) {
        return uid + ":" + themeId;
    }

    private record CacheEntry(RoguelikeAnalysisResult result, Instant expireAt) {}
}
