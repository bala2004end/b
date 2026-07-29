package com.aidb.assistant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    private String sender; // USER or AI

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String generatedSql;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private Long executionTimeMs;
    private Integer rowsReturned;
    private Boolean isSuccess;
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String resultJson;

    @Column(columnDefinition = "TEXT")
    private String chartType;

    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(Long id, Conversation conversation, String sender, String content, String generatedSql, String explanation, Long executionTimeMs, Integer rowsReturned, Boolean isSuccess, String errorMessage, String resultJson, String chartType, LocalDateTime timestamp) {
        this.id = id;
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
        this.generatedSql = generatedSql;
        this.explanation = explanation;
        this.executionTimeMs = executionTimeMs;
        this.rowsReturned = rowsReturned;
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.resultJson = resultJson;
        this.chartType = chartType;
        this.timestamp = timestamp;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
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
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Conversation conversation;
        private String sender;
        private String content;
        private String generatedSql;
        private String explanation;
        private Long executionTimeMs;
        private Integer rowsReturned;
        private Boolean isSuccess;
        private String errorMessage;
        private String resultJson;
        private String chartType;
        private LocalDateTime timestamp;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder conversation(Conversation conversation) { this.conversation = conversation; return this; }
        public Builder sender(String sender) { this.sender = sender; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder generatedSql(String generatedSql) { this.generatedSql = generatedSql; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder executionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; return this; }
        public Builder rowsReturned(Integer rowsReturned) { this.rowsReturned = rowsReturned; return this; }
        public Builder isSuccess(Boolean isSuccess) { this.isSuccess = isSuccess; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder resultJson(String resultJson) { this.resultJson = resultJson; return this; }
        public Builder chartType(String chartType) { this.chartType = chartType; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public ChatMessage build() {
            return new ChatMessage(id, conversation, sender, content, generatedSql, explanation, executionTimeMs, rowsReturned, isSuccess, errorMessage, resultJson, chartType, timestamp);
        }
    }
}
