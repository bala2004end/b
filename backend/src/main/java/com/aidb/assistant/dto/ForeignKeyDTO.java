package com.aidb.assistant.dto;

public class ForeignKeyDTO {
    private String fkColumnName;
    private String pkTableName;
    private String pkColumnName;
    private String fkName;

    public ForeignKeyDTO() {}
    public ForeignKeyDTO(String fkColumnName, String pkTableName, String pkColumnName, String fkName) {
        this.fkColumnName = fkColumnName;
        this.pkTableName = pkTableName;
        this.pkColumnName = pkColumnName;
        this.fkName = fkName;
    }

    public String getFkColumnName() { return fkColumnName; }
    public void setFkColumnName(String fkColumnName) { this.fkColumnName = fkColumnName; }
    public String getPkTableName() { return pkTableName; }
    public void setPkTableName(String pkTableName) { this.pkTableName = pkTableName; }
    public String getPkColumnName() { return pkColumnName; }
    public void setPkColumnName(String pkColumnName) { this.pkColumnName = pkColumnName; }
    public String getFkName() { return fkName; }
    public void setFkName(String fkName) { this.fkName = fkName; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String fkColumnName;
        private String pkTableName;
        private String pkColumnName;
        private String fkName;

        public Builder fkColumnName(String fkColumnName) { this.fkColumnName = fkColumnName; return this; }
        public Builder pkTableName(String pkTableName) { this.pkTableName = pkTableName; return this; }
        public Builder pkColumnName(String pkColumnName) { this.pkColumnName = pkColumnName; return this; }
        public Builder fkName(String fkName) { this.fkName = fkName; return this; }

        public ForeignKeyDTO build() {
            return new ForeignKeyDTO(fkColumnName, pkTableName, pkColumnName, fkName);
        }
    }
}
