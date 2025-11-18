package com.karaik.gamebot.roguelike.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 描述单个肉鸽主题的字段映射、分析规则与结局配置。
 */
@Data
public class RoguelikeThemeConfig {
    private String themeId;
    private String name;
    private Keys keys;
    private AnalysisRules analysisRules;
    private EndingRules endingRules;
    private StatsDefinitions statsDefinitions;

    @Data
    public static class Keys {
        private String successStatus;
        private String relicList;
        private String lastStage;
        private List<String> squad;
        private String startTimestamp;
        private String endTimestamp;
        private String score;
        private String difficulty;
        private String totemList;
    }

    @Data
    public static class AnalysisRules {
        private int minScoreForValid;
        private String primaryTotemId;
    }

    @Data
    public static class EndingRules {
        private String isRollingRelic;
        private List<Ending> endings;
        private List<Ending> ending5Companions;
        private String defaultWinEnding;
        private Map<String, String> textTemplates;

        @Data
        public static class Ending {
            private String name;
            private String relic;
        }
    }

    @Data
    public static class StatsDefinitions {
        private Map<String, Object> fifthEnding;
    }
}

