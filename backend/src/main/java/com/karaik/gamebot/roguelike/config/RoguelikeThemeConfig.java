package com.karaik.gamebot.roguelike.config;

import java.util.List;
import java.util.Map;

public class RoguelikeThemeConfig {
    private String themeId;
    private String name;
    private Keys keys;
    private AnalysisRules analysisRules;
    private EndingRules endingRules;
    private StatsDefinitions statsDefinitions;

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Keys getKeys() {
        return keys;
    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public AnalysisRules getAnalysisRules() {
        return analysisRules;
    }

    public void setAnalysisRules(AnalysisRules analysisRules) {
        this.analysisRules = analysisRules;
    }

    public EndingRules getEndingRules() {
        return endingRules;
    }

    public void setEndingRules(EndingRules endingRules) {
        this.endingRules = endingRules;
    }

    public StatsDefinitions getStatsDefinitions() {
        return statsDefinitions;
    }

    public void setStatsDefinitions(StatsDefinitions statsDefinitions) {
        this.statsDefinitions = statsDefinitions;
    }

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

        public String getSuccessStatus() {
            return successStatus;
        }

        public void setSuccessStatus(String successStatus) {
            this.successStatus = successStatus;
        }

        public String getRelicList() {
            return relicList;
        }

        public void setRelicList(String relicList) {
            this.relicList = relicList;
        }

        public String getLastStage() {
            return lastStage;
        }

        public void setLastStage(String lastStage) {
            this.lastStage = lastStage;
        }

        public List<String> getSquad() {
            return squad;
        }

        public void setSquad(List<String> squad) {
            this.squad = squad;
        }

        public String getStartTimestamp() {
            return startTimestamp;
        }

        public void setStartTimestamp(String startTimestamp) {
            this.startTimestamp = startTimestamp;
        }

        public String getEndTimestamp() {
            return endTimestamp;
        }

        public void setEndTimestamp(String endTimestamp) {
            this.endTimestamp = endTimestamp;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public String getTotemList() {
            return totemList;
        }

        public void setTotemList(String totemList) {
            this.totemList = totemList;
        }
    }

    public static class AnalysisRules {
        private int minScoreForValid;
        private String primaryTotemId;

        public int getMinScoreForValid() {
            return minScoreForValid;
        }

        public void setMinScoreForValid(int minScoreForValid) {
            this.minScoreForValid = minScoreForValid;
        }

        public String getPrimaryTotemId() {
            return primaryTotemId;
        }

        public void setPrimaryTotemId(String primaryTotemId) {
            this.primaryTotemId = primaryTotemId;
        }
    }

    public static class EndingRules {
        private String isRollingRelic;
        private List<Ending> endings;
        private List<Ending> ending5Companions;
        private String defaultWinEnding;
        private Map<String, String> textTemplates;

        public String getIsRollingRelic() {
            return isRollingRelic;
        }

        public void setIsRollingRelic(String isRollingRelic) {
            this.isRollingRelic = isRollingRelic;
        }

        public List<Ending> getEndings() {
            return endings;
        }

        public void setEndings(List<Ending> endings) {
            this.endings = endings;
        }

        public List<Ending> getEnding5Companions() {
            return ending5Companions;
        }

        public void setEnding5Companions(List<Ending> ending5Companions) {
            this.ending5Companions = ending5Companions;
        }

        public String getDefaultWinEnding() {
            return defaultWinEnding;
        }

        public void setDefaultWinEnding(String defaultWinEnding) {
            this.defaultWinEnding = defaultWinEnding;
        }

        public Map<String, String> getTextTemplates() {
            return textTemplates;
        }

        public void setTextTemplates(Map<String, String> textTemplates) {
            this.textTemplates = textTemplates;
        }

        public static class Ending {
            private String name;
            private String relic;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getRelic() {
                return relic;
            }

            public void setRelic(String relic) {
                this.relic = relic;
            }
        }
    }

    public static class StatsDefinitions {
        private Map<String, Object> fifthEnding;

        public Map<String, Object> getFifthEnding() {
            return fifthEnding;
        }

        public void setFifthEnding(Map<String, Object> fifthEnding) {
            this.fifthEnding = fifthEnding;
        }
    }
}

