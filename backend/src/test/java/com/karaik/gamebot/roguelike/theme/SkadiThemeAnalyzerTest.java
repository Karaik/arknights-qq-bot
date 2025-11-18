package com.karaik.gamebot.roguelike.theme;

import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;
import com.karaik.gamebot.roguelike.theme.skadi.SkadiThemeAnalyzer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SkadiThemeAnalyzerTest {

    @Autowired
    private SkadiThemeAnalyzer analyzer;

    @Test
    void shouldProduceSummaryFromHistoryRecords() {
        Map<String, Object> rawTopic = Map.of("name", "萨卡兹的无终奇语");
        List<Map<String, Object>> history = List.of(
                Map.of("id", "run-1", "score", 150, "success", 1, "gainRelicList", List.of("rogue_4_relic_final_11"), "startTs", 1700000000L),
                Map.of("id", "run-2", "score", 80, "success", 0, "startTs", 1700000100L)
        );

        RoguelikeAnalysisResult result = analyzer.analyze(rawTopic, history);

        assertThat(result.getThemeId()).isEqualTo("rogue_4");
        assertThat(result.getTotalRuns()).isEqualTo(1); // second run filtered by score
        assertThat(result.getWinRate()).isEqualTo(1.0);
        assertThat(result.getRecentRuns()).hasSize(1);
    }
}

