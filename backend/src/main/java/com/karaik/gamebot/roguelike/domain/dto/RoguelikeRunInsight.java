package com.karaik.gamebot.roguelike.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoguelikeRunInsight {
    private String runId;
    private boolean success;
    private String ending;
    private String squad;
    private String startDate;
}

