package com.aidb.assistant.dto;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private String generatedSql;
    private String chartType;
    private String rawData;
    private LocalDateTime timestamp;

    public ChatMessageDTO() {}

    public ChatMessageDTO(Long id, Long conversationId, String role, String content, String generatedSql, String chartType, String rawData, LocalDateTime timestamp) {
        this.id = id;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.generatedSql = generatedSql;
        this.chartType = chartType;
        this.rawData = rawData;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getGeneratedSql() { return generatedSql; }
    public void setGeneratedSql(String generatedSql) { this.generatedSql = generatedSql; }
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
