package com.karaik.gamebot.roguelike.domain.dto;

public class RoguelikeRunInsight {
    private final String runId;
    private final boolean success;
    private final String ending;
    private final String squad;
    private final String startDate;

    public RoguelikeRunInsight(String runId, boolean success, String ending, String squad, String startDate) {
        this.runId = runId;
        this.success = success;
        this.ending = ending;
        this.squad = squad;
        this.startDate = startDate;
    }

    public String getRunId() {
        return runId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getEnding() {
        return ending;
    }

    public String getSquad() {
        return squad;
    }

    public String getStartDate() {
        return startDate;
    }
}

