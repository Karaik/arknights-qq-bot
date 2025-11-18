package com.karaik.gamebot.roguelike.domain.dto;

import java.util.List;

public class RoguelikeAnalysisResult {
    private String themeId;
    private String themeName;
    private int totalRuns;
    private double winRate;
    private List<RoguelikeRunInsight> recentRuns;

    public RoguelikeAnalysisResult(String themeId, String themeName, int totalRuns, double winRate, List<RoguelikeRunInsight> recentRuns) {
        this.themeId = themeId;
        this.themeName = themeName;
        this.totalRuns = totalRuns;
        this.winRate = winRate;
        this.recentRuns = recentRuns;
    }

    public String getThemeId() {
        return themeId;
    }

    public String getThemeName() {
        return themeName;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public double getWinRate() {
        return winRate;
    }

    public List<RoguelikeRunInsight> getRecentRuns() {
        return recentRuns;
    }
}

