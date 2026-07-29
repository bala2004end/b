package com.aidb.assistant.dto;

public class ColumnDTO {
    private String columnName;
    private String dataType;
    private Integer columnSize;
    private Boolean isNullable;
    private Boolean isPrimaryKey;
    private Boolean isForeignKey;
    private String fkReferencedTable;
    private String fkReferencedColumn;
    private String remarks;

    public ColumnDTO() {}
    public ColumnDTO(String columnName, String dataType, Integer columnSize, Boolean isNullable, Boolean isPrimaryKey, Boolean isForeignKey, String fkReferencedTable, String fkReferencedColumn, String remarks) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.columnSize = columnSize;
        this.isNullable = isNullable;
        this.isPrimaryKey = isPrimaryKey;
        this.isForeignKey = isForeignKey;
        this.fkReferencedTable = fkReferencedTable;
        this.fkReferencedColumn = fkReferencedColumn;
        this.remarks = remarks;
    }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public Integer getColumnSize() { return columnSize; }
    public void setColumnSize(Integer columnSize) { this.columnSize = columnSize; }
    public Boolean getIsNullable() { return isNullable; }
    public void setIsNullable(Boolean isNullable) { this.isNullable = isNullable; }
    public Boolean getIsPrimaryKey() { return isPrimaryKey; }
    public void setIsPrimaryKey(Boolean isPrimaryKey) { this.isPrimaryKey = isPrimaryKey; }
    public Boolean getIsForeignKey() { return isForeignKey; }
    public void setIsForeignKey(Boolean isForeignKey) { this.isForeignKey = isForeignKey; }
    public String getFkReferencedTable() { return fkReferencedTable; }
    public void setFkReferencedTable(String fkReferencedTable) { this.fkReferencedTable = fkReferencedTable; }
    public String getFkReferencedColumn() { return fkReferencedColumn; }
    public void setFkReferencedColumn(String fkReferencedColumn) { this.fkReferencedColumn = fkReferencedColumn; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String columnName;
        private String dataType;
        private Integer columnSize;
        private Boolean isNullable;
        private Boolean isPrimaryKey;
        private Boolean isForeignKey;
        private String fkReferencedTable;
        private String fkReferencedColumn;
        private String remarks;

        public Builder columnName(String columnName) { this.columnName = columnName; return this; }
        public Builder dataType(String dataType) { this.dataType = dataType; return this; }
        public Builder columnSize(Integer columnSize) { this.columnSize = columnSize; return this; }
        public Builder isNullable(Boolean isNullable) { this.isNullable = isNullable; return this; }
        public Builder isPrimaryKey(Boolean isPrimaryKey) { this.isPrimaryKey = isPrimaryKey; return this; }
        public Builder isForeignKey(Boolean isForeignKey) { this.isForeignKey = isForeignKey; return this; }
        public Builder fkReferencedTable(String fkReferencedTable) { this.fkReferencedTable = fkReferencedTable; return this; }
        public Builder fkReferencedColumn(String fkReferencedColumn) { this.fkReferencedColumn = fkReferencedColumn; return this; }
        public Builder remarks(String remarks) { this.remarks = remarks; return this; }

        public ColumnDTO build() {
            return new ColumnDTO(columnName, dataType, columnSize, isNullable, isPrimaryKey, isForeignKey, fkReferencedTable, fkReferencedColumn, remarks);
        }
    }
}
