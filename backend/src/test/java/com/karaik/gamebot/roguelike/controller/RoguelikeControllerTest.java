package com.karaik.gamebot.roguelike.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaik.gamebot.common.api.ApiResponse;
import com.karaik.gamebot.roguelike.controller.api.RoguelikeController;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeRunInsight;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeThemeSummary;
import com.karaik.gamebot.roguelike.service.RoguelikeApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoguelikeController.class)
@TestPropertySource(properties = "roguelike.api-key=test-key")
class RoguelikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoguelikeApplicationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnThemeListWhenApiKeyValid() throws Exception {
        Mockito.when(service.listThemes())
                .thenReturn(List.of(new RoguelikeThemeSummary("rogue_4", "萨卡兹的无终奇语")));

        mockMvc.perform(get("/api/roguelike/themes").header("X-API-KEY", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].themeId").value("rogue_4"));
    }

    @Test
    void shouldRejectWhenApiKeyMissing() throws Exception {
        mockMvc.perform(get("/api/roguelike/themes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAnalysis() throws Exception {
        RoguelikeAnalysisResult result = new RoguelikeAnalysisResult(
                "rogue_4",
                "萨卡兹的无终奇语",
                5,
                0.6,
                List.of(new RoguelikeRunInsight("run-1", true, "完成结局", "矛头", "10-01"))
        );
        Mockito.when(service.getAnalysis(anyString(), anyString(), anyBoolean())).thenReturn(result);

        mockMvc.perform(get("/api/roguelike/userA/rogue_4/analysis")
                        .header("X-API-KEY", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.themeId").value("rogue_4"));
    }

    @Test
    void shouldRefresh() throws Exception {
        RoguelikeAnalysisResult refreshed = new RoguelikeAnalysisResult(
                "rogue_4",
                "萨卡兹的无终奇语",
                6,
                0.66,
                List.of(new RoguelikeRunInsight("run-2", true, "完成结局", "矛头", "10-02"))
        );
        Mockito.when(service.refreshAndAnalyze("userA", "rogue_4")).thenReturn(refreshed);

        mockMvc.perform(post("/api/roguelike/userA/refresh")
                        .param("themeId", "rogue_4")
                        .header("X-API-KEY", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRuns").value(6));
    }
}
