package com.aidb.assistant.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsDTO {
    private Long totalQueriesExecuted;
    private Double avgExecutionTimeMs;
    private Long slowQueriesCount;
    private List<Map<String, Object>> slowQueryLogs;
    private List<String> indexRecommendations;
    private List<Map<String, Object>> queryTrends;

    public AnalyticsDTO() {}
    public AnalyticsDTO(Long totalQueriesExecuted, Double avgExecutionTimeMs, Long slowQueriesCount, List<Map<String, Object>> slowQueryLogs, List<String> indexRecommendations, List<Map<String, Object>> queryTrends) {
        this.totalQueriesExecuted = totalQueriesExecuted;
        this.avgExecutionTimeMs = avgExecutionTimeMs;
        this.slowQueriesCount = slowQueriesCount;
        this.slowQueryLogs = slowQueryLogs;
        this.indexRecommendations = indexRecommendations;
        this.queryTrends = queryTrends;
    }

    public Long getTotalQueriesExecuted() { return totalQueriesExecuted; }
    public void setTotalQueriesExecuted(Long totalQueriesExecuted) { this.totalQueriesExecuted = totalQueriesExecuted; }
    public Double getAvgExecutionTimeMs() { return avgExecutionTimeMs; }
    public void setAvgExecutionTimeMs(Double avgExecutionTimeMs) { this.avgExecutionTimeMs = avgExecutionTimeMs; }
    public Long getSlowQueriesCount() { return slowQueriesCount; }
    public void setSlowQueriesCount(Long slowQueriesCount) { this.slowQueriesCount = slowQueriesCount; }
    public List<Map<String, Object>> getSlowQueryLogs() { return slowQueryLogs; }
    public void setSlowQueryLogs(List<Map<String, Object>> slowQueryLogs) { this.slowQueryLogs = slowQueryLogs; }
    public List<String> getIndexRecommendations() { return indexRecommendations; }
    public void setIndexRecommendations(List<String> indexRecommendations) { this.indexRecommendations = indexRecommendations; }
    public List<Map<String, Object>> getQueryTrends() { return queryTrends; }
    public void setQueryTrends(List<Map<String, Object>> queryTrends) { this.queryTrends = queryTrends; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long totalQueriesExecuted;
        private Double avgExecutionTimeMs;
        private Long slowQueriesCount;
        private List<Map<String, Object>> slowQueryLogs;
        private List<String> indexRecommendations;
        private List<Map<String, Object>> queryTrends;

        public Builder totalQueriesExecuted(Long totalQueriesExecuted) { this.totalQueriesExecuted = totalQueriesExecuted; return this; }
        public Builder avgExecutionTimeMs(Double avgExecutionTimeMs) { this.avgExecutionTimeMs = avgExecutionTimeMs; return this; }
        public Builder slowQueriesCount(Long slowQueriesCount) { this.slowQueriesCount = slowQueriesCount; return this; }
        public Builder slowQueryLogs(List<Map<String, Object>> slowQueryLogs) { this.slowQueryLogs = slowQueryLogs; return this; }
        public Builder indexRecommendations(List<String> indexRecommendations) { this.indexRecommendations = indexRecommendations; return this; }
        public Builder queryTrends(List<Map<String, Object>> queryTrends) { this.queryTrends = queryTrends; return this; }

        public AnalyticsDTO build() {
            return new AnalyticsDTO(totalQueriesExecuted, avgExecutionTimeMs, slowQueriesCount, slowQueryLogs, indexRecommendations, queryTrends);
        }
    }
}
