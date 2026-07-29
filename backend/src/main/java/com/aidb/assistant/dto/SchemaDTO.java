package com.aidb.assistant.dto;

import java.util.ArrayList;
import java.util.List;

public class SchemaDTO {
    private String databaseName;
    private Integer totalTables;
    private Integer totalViews;
    private Integer totalProcedures;
    private List<TableDTO> tables = new ArrayList<>();
    private List<String> views = new ArrayList<>();
    private List<String> procedures = new ArrayList<>();

    public SchemaDTO() {}
    public SchemaDTO(String databaseName, Integer totalTables, Integer totalViews, Integer totalProcedures, List<TableDTO> tables, List<String> views, List<String> procedures) {
        this.databaseName = databaseName;
        this.totalTables = totalTables;
        this.totalViews = totalViews;
        this.totalProcedures = totalProcedures;
        if (tables != null) this.tables = tables;
        if (views != null) this.views = views;
        if (procedures != null) this.procedures = procedures;
    }

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public Integer getTotalTables() { return totalTables; }
    public void setTotalTables(Integer totalTables) { this.totalTables = totalTables; }
    public Integer getTotalViews() { return totalViews; }
    public void setTotalViews(Integer totalViews) { this.totalViews = totalViews; }
    public Integer getTotalProcedures() { return totalProcedures; }
    public void setTotalProcedures(Integer totalProcedures) { this.totalProcedures = totalProcedures; }
    public List<TableDTO> getTables() { return tables; }
    public void setTables(List<TableDTO> tables) { this.tables = tables; }
    public List<String> getViews() { return views; }
    public void setViews(List<String> views) { this.views = views; }
    public List<String> getProcedures() { return procedures; }
    public void setProcedures(List<String> procedures) { this.procedures = procedures; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String databaseName;
        private Integer totalTables;
        private Integer totalViews;
        private Integer totalProcedures;
        private List<TableDTO> tables = new ArrayList<>();
        private List<String> views = new ArrayList<>();
        private List<String> procedures = new ArrayList<>();

        public Builder databaseName(String databaseName) { this.databaseName = databaseName; return this; }
        public Builder totalTables(Integer totalTables) { this.totalTables = totalTables; return this; }
        public Builder totalViews(Integer totalViews) { this.totalViews = totalViews; return this; }
        public Builder totalProcedures(Integer totalProcedures) { this.totalProcedures = totalProcedures; return this; }
        public Builder tables(List<TableDTO> tables) { this.tables = tables; return this; }
        public Builder views(List<String> views) { this.views = views; return this; }
        public Builder procedures(List<String> procedures) { this.procedures = procedures; return this; }

        public SchemaDTO build() {
            return new SchemaDTO(databaseName, totalTables, totalViews, totalProcedures, tables, views, procedures);
        }
    }
}
