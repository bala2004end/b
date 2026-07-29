package com.aidb.assistant.dto;

public class ConnectionResponse {
    private Boolean connected;
    private String message;
    private String databaseName;
    private String host;
    private Integer port;
    private Boolean isReadOnly;
    private Integer totalTables;
    private Integer totalColumns;
    private Integer totalEmbeddings;

    public ConnectionResponse() {}
    public ConnectionResponse(Boolean connected, String message, String databaseName, String host, Integer port, Boolean isReadOnly, Integer totalTables, Integer totalColumns, Integer totalEmbeddings) {
        this.connected = connected;
        this.message = message;
        this.databaseName = databaseName;
        this.host = host;
        this.port = port;
        this.isReadOnly = isReadOnly;
        this.totalTables = totalTables;
        this.totalColumns = totalColumns;
        this.totalEmbeddings = totalEmbeddings;
    }

    public Boolean getConnected() { return connected; }
    public void setConnected(Boolean connected) { this.connected = connected; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public Boolean getIsReadOnly() { return isReadOnly; }
    public void setIsReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; }
    public Integer getTotalTables() { return totalTables; }
    public void setTotalTables(Integer totalTables) { this.totalTables = totalTables; }
    public Integer getTotalColumns() { return totalColumns; }
    public void setTotalColumns(Integer totalColumns) { this.totalColumns = totalColumns; }
    public Integer getTotalEmbeddings() { return totalEmbeddings; }
    public void setTotalEmbeddings(Integer totalEmbeddings) { this.totalEmbeddings = totalEmbeddings; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Boolean connected;
        private String message;
        private String databaseName;
        private String host;
        private Integer port;
        private Boolean isReadOnly;
        private Integer totalTables;
        private Integer totalColumns;
        private Integer totalEmbeddings;

        public Builder connected(Boolean connected) { this.connected = connected; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder databaseName(String databaseName) { this.databaseName = databaseName; return this; }
        public Builder host(String host) { this.host = host; return this; }
        public Builder port(Integer port) { this.port = port; return this; }
        public Builder isReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; return this; }
        public Builder totalTables(Integer totalTables) { this.totalTables = totalTables; return this; }
        public Builder totalColumns(Integer totalColumns) { this.totalColumns = totalColumns; return this; }
        public Builder totalEmbeddings(Integer totalEmbeddings) { this.totalEmbeddings = totalEmbeddings; return this; }

        public ConnectionResponse build() {
            return new ConnectionResponse(connected, message, databaseName, host, port, isReadOnly, totalTables, totalColumns, totalEmbeddings);
        }
    }
}
