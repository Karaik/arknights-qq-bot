package com.karaik.gamebot.roguelike.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RoguelikeAnalysisResult {
    private String themeId;
    private String themeName;
    private int totalRuns;
    private double winRate;
    private List<RoguelikeRunInsight> recentRuns;
}

