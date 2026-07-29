package com.aidb.assistant.dto;

public class SettingsDTO {
    private String geminiApiKey;
    private String modelName;
    private Boolean isReadOnly;
    private Integer maxRowsLimit;
    private Integer topKSchemaChunks;

    public SettingsDTO() {}
    public SettingsDTO(String geminiApiKey, String modelName, Boolean isReadOnly, Integer maxRowsLimit, Integer topKSchemaChunks) {
        this.geminiApiKey = geminiApiKey;
        this.modelName = modelName;
        this.isReadOnly = isReadOnly;
        this.maxRowsLimit = maxRowsLimit;
        this.topKSchemaChunks = topKSchemaChunks;
    }

    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Boolean getIsReadOnly() { return isReadOnly; }
    public void setIsReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; }
    public Integer getMaxRowsLimit() { return maxRowsLimit; }
    public void setMaxRowsLimit(Integer maxRowsLimit) { this.maxRowsLimit = maxRowsLimit; }
    public Integer getTopKSchemaChunks() { return topKSchemaChunks; }
    public void setTopKSchemaChunks(Integer topKSchemaChunks) { this.topKSchemaChunks = topKSchemaChunks; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String geminiApiKey;
        private String modelName;
        private Boolean isReadOnly;
        private Integer maxRowsLimit;
        private Integer topKSchemaChunks;

        public Builder geminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; return this; }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder isReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; return this; }
        public Builder maxRowsLimit(Integer maxRowsLimit) { this.maxRowsLimit = maxRowsLimit; return this; }
        public Builder topKSchemaChunks(Integer topKSchemaChunks) { this.topKSchemaChunks = topKSchemaChunks; return this; }

        public SettingsDTO build() {
            return new SettingsDTO(geminiApiKey, modelName, isReadOnly, maxRowsLimit, topKSchemaChunks);
        }
    }
}
