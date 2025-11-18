package com.karaik.gamebot.roguelike.theme.api;

import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;

import java.util.List;
import java.util.Map;

public interface RoguelikeThemeAnalyzer {
    boolean supports(String themeIdOrName);

    RoguelikeAnalysisResult analyze(Map<String, Object> rawTopic, List<Map<String, Object>> historyRecords);
}

