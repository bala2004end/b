package com.aidb.assistant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "connection_configs")
public class ConnectionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String databaseName;
    private Boolean isReadOnly;
    private Boolean isActive;
    private LocalDateTime lastConnectedAt;

    public ConnectionConfig() {}

    public ConnectionConfig(Long id, String name, String host, Integer port, String username, String password, String databaseName, Boolean isReadOnly, Boolean isActive, LocalDateTime lastConnectedAt) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.isReadOnly = isReadOnly;
        this.isActive = isActive;
        this.lastConnectedAt = lastConnectedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.isReadOnly == null) {
            this.isReadOnly = true;
        }
        if (this.isActive == null) {
            this.isActive = false;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public Boolean getIsReadOnly() { return isReadOnly; }
    public void setIsReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getLastConnectedAt() { return lastConnectedAt; }
    public void setLastConnectedAt(LocalDateTime lastConnectedAt) { this.lastConnectedAt = lastConnectedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String host;
        private Integer port;
        private String username;
        private String password;
        private String databaseName;
        private Boolean isReadOnly;
        private Boolean isActive;
        private LocalDateTime lastConnectedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder host(String host) { this.host = host; return this; }
        public Builder port(Integer port) { this.port = port; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder databaseName(String databaseName) { this.databaseName = databaseName; return this; }
        public Builder isReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder lastConnectedAt(LocalDateTime lastConnectedAt) { this.lastConnectedAt = lastConnectedAt; return this; }

        public ConnectionConfig build() {
            return new ConnectionConfig(id, name, host, port, username, password, databaseName, isReadOnly, isActive, lastConnectedAt);
        }
    }
}
