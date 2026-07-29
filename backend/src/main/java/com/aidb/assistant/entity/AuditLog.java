package com.aidb.assistant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;

    @Column(columnDefinition = "TEXT")
    private String details;

    private Long executionTimeMs;
    private Boolean isSuccess;
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(Long id, String username, String action, String details, Long executionTimeMs, Boolean isSuccess, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.details = details;
        this.executionTimeMs = executionTimeMs;
        this.isSuccess = isSuccess;
        this.timestamp = timestamp;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public Boolean getIsSuccess() { return isSuccess; }
    public void setIsSuccess(Boolean isSuccess) { this.isSuccess = isSuccess; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String username;
        private String action;
        private String details;
        private Long executionTimeMs;
        private Boolean isSuccess;
        private LocalDateTime timestamp;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public Builder executionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; return this; }
        public Builder isSuccess(Boolean isSuccess) { this.isSuccess = isSuccess; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AuditLog build() {
            return new AuditLog(id, username, action, details, executionTimeMs, isSuccess, timestamp);
        }
    }
}
