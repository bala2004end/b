package com.aidb.assistant.dto;

import java.util.List;
import java.util.Map;

public class ChatResponse {
    private Long messageId;
    private Long conversationId;
    private String question;
    private String generatedSql;
    private String explanation;
    private Long executionTimeMs;
    private Integer rowsReturned;
    private Boolean isSuccess;
    private String errorMessage;
    private List<Map<String, Object>> data;
    private List<String> columns;
    private String chartType;
    private List<String> retrievedTables;

    public ChatResponse() {}
    public ChatResponse(Long messageId, Long conversationId, String question, String generatedSql, String explanation, Long executionTimeMs, Integer rowsReturned, Boolean isSuccess, String errorMessage, List<Map<String, Object>> data, List<String> columns, String chartType, List<String> retrievedTables) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.question = question;
        this.generatedSql = generatedSql;
        this.explanation = explanation;
        this.executionTimeMs = executionTimeMs;
        this.rowsReturned = rowsReturned;
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.data = data;
        this.columns = columns;
        this.chartType = chartType;
        this.retrievedTables = retrievedTables;
    }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getGeneratedSql() { return generatedSql; }
    public void setGeneratedSql(String generatedSql) { this.generatedSql = generatedSql; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public Integer getRowsReturned() { return rowsReturned; }
    public void setRowsReturned(Integer rowsReturned) { this.rowsReturned = rowsReturned; }
    public Boolean getIsSuccess() { return isSuccess; }
    public void setIsSuccess(Boolean isSuccess) { this.isSuccess = isSuccess; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public List<Map<String, Object>> getData() { return data; }
    public void setData(List<Map<String, Object>> data) { this.data = data; }
    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    public List<String> getRetrievedTables() { return retrievedTables; }
    public void setRetrievedTables(List<String> retrievedTables) { this.retrievedTables = retrievedTables; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long messageId;
        private Long conversationId;
        private String question;
        private String generatedSql;
        private String explanation;
        private Long executionTimeMs;
        private Integer rowsReturned;
        private Boolean isSuccess;
        private String errorMessage;
        private List<Map<String, Object>> data;
        private List<String> columns;
        private String chartType;
        private List<String> retrievedTables;

        public Builder messageId(Long messageId) { this.messageId = messageId; return this; }
        public Builder conversationId(Long conversationId) { this.conversationId = conversationId; return this; }
        public Builder question(String question) { this.question = question; return this; }
        public Builder generatedSql(String generatedSql) { this.generatedSql = generatedSql; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder executionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; return this; }
        public Builder rowsReturned(Integer rowsReturned) { this.rowsReturned = rowsReturned; return this; }
        public Builder isSuccess(Boolean isSuccess) { this.isSuccess = isSuccess; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder data(List<Map<String, Object>> data) { this.data = data; return this; }
        public Builder columns(List<String> columns) { this.columns = columns; return this; }
        public Builder chartType(String chartType) { this.chartType = chartType; return this; }
        public Builder retrievedTables(List<String> retrievedTables) { this.retrievedTables = retrievedTables; return this; }

        public ChatResponse build() {
            return new ChatResponse(messageId, conversationId, question, generatedSql, explanation, executionTimeMs, rowsReturned, isSuccess, errorMessage, data, columns, chartType, retrievedTables);
        }
    }
}
