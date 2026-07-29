package com.aidb.assistant.dto;

import java.util.List;

public class OptimizationDTO {
    private String originalSql;
    private String optimizedSql;
    private String explanation;
    private List<String> indexRecommendations;
    private List<String> potentialBottlenecks;

    public OptimizationDTO() {}
    public OptimizationDTO(String originalSql, String optimizedSql, String explanation, List<String> indexRecommendations, List<String> potentialBottlenecks) {
        this.originalSql = originalSql;
        this.optimizedSql = optimizedSql;
        this.explanation = explanation;
        this.indexRecommendations = indexRecommendations;
        this.potentialBottlenecks = potentialBottlenecks;
    }

    public String getOriginalSql() { return originalSql; }
    public void setOriginalSql(String originalSql) { this.originalSql = originalSql; }
    public String getOptimizedSql() { return optimizedSql; }
    public void setOptimizedSql(String optimizedSql) { this.optimizedSql = optimizedSql; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public List<String> getIndexRecommendations() { return indexRecommendations; }
    public void setIndexRecommendations(List<String> indexRecommendations) { this.indexRecommendations = indexRecommendations; }
    public List<String> getPotentialBottlenecks() { return potentialBottlenecks; }
    public void setPotentialBottlenecks(List<String> potentialBottlenecks) { this.potentialBottlenecks = potentialBottlenecks; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String originalSql;
        private String optimizedSql;
        private String explanation;
        private List<String> indexRecommendations;
        private List<String> potentialBottlenecks;

        public Builder originalSql(String originalSql) { this.originalSql = originalSql; return this; }
        public Builder optimizedSql(String optimizedSql) { this.optimizedSql = optimizedSql; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder indexRecommendations(List<String> indexRecommendations) { this.indexRecommendations = indexRecommendations; return this; }
        public Builder potentialBottlenecks(List<String> potentialBottlenecks) { this.potentialBottlenecks = potentialBottlenecks; return this; }

        public OptimizationDTO build() {
            return new OptimizationDTO(originalSql, optimizedSql, explanation, indexRecommendations, potentialBottlenecks);
        }
    }
}
