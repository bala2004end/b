package com.aidb.assistant.dto;

import java.time.LocalDateTime;

public class SavedQueryDTO {
    private Long id;
    private String title;
    private String sqlQuery;
    private String category;
    private String description;
    private LocalDateTime createdAt;

    public SavedQueryDTO() {}
    public SavedQueryDTO(Long id, String title, String sqlQuery, String category, String description, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.sqlQuery = sqlQuery;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSqlQuery() { return sqlQuery; }
    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String sqlQuery;
        private String category;
        private String description;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder sqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SavedQueryDTO build() {
            return new SavedQueryDTO(id, title, sqlQuery, category, description, createdAt);
        }
    }
}
