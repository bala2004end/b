package com.aidb.assistant.dto;

public class ConnectionRequest {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String databaseName;
    private Boolean isReadOnly;

    public ConnectionRequest() {}
    public ConnectionRequest(String host, Integer port, String username, String password, String databaseName, Boolean isReadOnly) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.isReadOnly = isReadOnly;
    }

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
}
