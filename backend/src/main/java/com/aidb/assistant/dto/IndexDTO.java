package com.aidb.assistant.dto;

public class IndexDTO {
    private String indexName;
    private String columnName;
    private Boolean isNonUnique;
    private String type;

    public IndexDTO() {}
    public IndexDTO(String indexName, String columnName, Boolean isNonUnique, String type) {
        this.indexName = indexName;
        this.columnName = columnName;
        this.isNonUnique = isNonUnique;
        this.type = type;
    }

    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public Boolean getIsNonUnique() { return isNonUnique; }
    public void setIsNonUnique(Boolean isNonUnique) { this.isNonUnique = isNonUnique; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String indexName;
        private String columnName;
        private Boolean isNonUnique;
        private String type;

        public Builder indexName(String indexName) { this.indexName = indexName; return this; }
        public Builder columnName(String columnName) { this.columnName = columnName; return this; }
        public Builder isNonUnique(Boolean isNonUnique) { this.isNonUnique = isNonUnique; return this; }
        public Builder type(String type) { this.type = type; return this; }

        public IndexDTO build() {
            return new IndexDTO(indexName, columnName, isNonUnique, type);
        }
    }
}
