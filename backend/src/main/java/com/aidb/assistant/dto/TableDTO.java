package com.aidb.assistant.dto;

import java.util.ArrayList;
import java.util.List;

public class TableDTO {
    private String tableName;
    private String tableType;
    private String remarks;
    private Long estimatedRowCount;
    private List<ColumnDTO> columns = new ArrayList<>();
    private List<ForeignKeyDTO> foreignKeys = new ArrayList<>();
    private List<IndexDTO> indexes = new ArrayList<>();

    public TableDTO() {}
    public TableDTO(String tableName, String tableType, String remarks, Long estimatedRowCount, List<ColumnDTO> columns, List<ForeignKeyDTO> foreignKeys, List<IndexDTO> indexes) {
        this.tableName = tableName;
        this.tableType = tableType;
        this.remarks = remarks;
        this.estimatedRowCount = estimatedRowCount;
        if (columns != null) this.columns = columns;
        if (foreignKeys != null) this.foreignKeys = foreignKeys;
        if (indexes != null) this.indexes = indexes;
    }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getTableType() { return tableType; }
    public void setTableType(String tableType) { this.tableType = tableType; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Long getEstimatedRowCount() { return estimatedRowCount; }
    public void setEstimatedRowCount(Long estimatedRowCount) { this.estimatedRowCount = estimatedRowCount; }
    public List<ColumnDTO> getColumns() { return columns; }
    public void setColumns(List<ColumnDTO> columns) { this.columns = columns; }
    public List<ForeignKeyDTO> getForeignKeys() { return foreignKeys; }
    public void setForeignKeys(List<ForeignKeyDTO> foreignKeys) { this.foreignKeys = foreignKeys; }
    public List<IndexDTO> getIndexes() { return indexes; }
    public void setIndexes(List<IndexDTO> indexes) { this.indexes = indexes; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String tableName;
        private String tableType;
        private String remarks;
        private Long estimatedRowCount;
        private List<ColumnDTO> columns = new ArrayList<>();
        private List<ForeignKeyDTO> foreignKeys = new ArrayList<>();
        private List<IndexDTO> indexes = new ArrayList<>();

        public Builder tableName(String tableName) { this.tableName = tableName; return this; }
        public Builder tableType(String tableType) { this.tableType = tableType; return this; }
        public Builder remarks(String remarks) { this.remarks = remarks; return this; }
        public Builder estimatedRowCount(Long estimatedRowCount) { this.estimatedRowCount = estimatedRowCount; return this; }
        public Builder columns(List<ColumnDTO> columns) { this.columns = columns; return this; }
        public Builder foreignKeys(List<ForeignKeyDTO> foreignKeys) { this.foreignKeys = foreignKeys; return this; }
        public Builder indexes(List<IndexDTO> indexes) { this.indexes = indexes; return this; }

        public TableDTO build() {
            return new TableDTO(tableName, tableType, remarks, estimatedRowCount, columns, foreignKeys, indexes);
        }
    }
}
