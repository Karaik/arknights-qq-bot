package com.karaik.gamebot.roguelike.theme.api;

import com.karaik.gamebot.roguelike.config.RoguelikeThemeConfig;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeRunInsight;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractThemeAnalyzer implements RoguelikeThemeAnalyzer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd")
            .withZone(ZoneId.systemDefault());

    protected final RoguelikeThemeConfig config;

    protected AbstractThemeAnalyzer(RoguelikeThemeConfig config) {
        this.config = config;
    }

    @Override
    public boolean supports(String themeIdOrName) {
        return config.getThemeId().equalsIgnoreCase(themeIdOrName)
                || config.getName().equalsIgnoreCase(themeIdOrName);
    }

    @Override
    public RoguelikeAnalysisResult analyze(Map<String, Object> rawTopic, List<Map<String, Object>> historyRecords) {
        int totalRuns = 0;
        int winCount = 0;
        List<RoguelikeRunInsight> recent = new ArrayList<>();

        for (Map<String, Object> run : historyRecords) {
            if (!isValidRun(run)) {
                continue;
            }
            totalRuns++;
            boolean success = success(run);
            if (success) {
                winCount++;
            }
            if (recent.size() < 8) {
                recent.add(createInsight(run, success));
            }
        }

        double winRate = totalRuns == 0 ? 0 : (double) winCount / totalRuns;
        return new RoguelikeAnalysisResult(
                config.getThemeId(),
                config.getName(),
                totalRuns,
                winRate,
                recent
        );
    }

    protected boolean isValidRun(Map<String, Object> run) {
        Object scoreObj = run.get(config.getKeys().getScore());
        if (scoreObj instanceof Number num) {
            return num.intValue() >= config.getAnalysisRules().getMinScoreForValid();
        }
        return true;
    }

    protected boolean success(Map<String, Object> run) {
        Object status = run.get(config.getKeys().getSuccessStatus());
        if (status instanceof Number number) {
            return number.intValue() == 1;
        }
        return false;
    }

    private RoguelikeRunInsight createInsight(Map<String, Object> run, boolean success) {
        String runId = (String) run.getOrDefault("id", "unknown");
        String ending = determineEnding(run, success);
        String squad = extractSquad(run);
        String startDate = formatDate(run.get(config.getKeys().getStartTimestamp()));
        return new RoguelikeRunInsight(runId, success, ending, squad, startDate);
    }

    private String formatDate(Object startTs) {
        if (startTs instanceof Number number) {
            return DATE_FORMATTER.format(Instant.ofEpochSecond(number.longValue()));
        }
        return "N/A";
    }

    private String extractSquad(Map<String, Object> run) {
        List<String> path = config.getKeys().getSquad();
        Object cursor = run;
        for (String key : path) {
            if (!(cursor instanceof Map<?, ?> map)) {
                return "N/A";
            }
            cursor = map.get(key);
        }
        return cursor instanceof String str ? str : "N/A";
    }

    private String determineEnding(Map<String, Object> run, boolean success) {
        if (!success) {
            Object lastStage = run.getOrDefault(config.getKeys().getLastStage(), "未知关卡");
            String template = config.getEndingRules().getTextTemplates().get("failure");
            return template.replace("{last_stage}", lastStage.toString());
        }
        List<String> endings = new ArrayList<>();
        for (RoguelikeThemeConfig.EndingRules.Ending ending : config.getEndingRules().getEndings()) {
            if (hasRelic(run, ending.getRelic())) {
                endings.add(ending.getName());
            }
        }
        if (endings.isEmpty()) {
            endings.add(config.getEndingRules().getDefaultWinEnding());
        }
        String template = config.getEndingRules().getTextTemplates().get("success");
        return template.replace("{endings}", String.join("", endings));
    }

    protected boolean hasRelic(Map<String, Object> run, String relicId) {
        Object list = run.get(config.getKeys().getRelicList());
        if (list instanceof List<?> relics) {
            return relics.contains(relicId);
        }
        return false;
    }
}

