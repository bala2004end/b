package com.aidb.assistant.dto;

public class ExplainRequest {
    private String sqlQuery;

    public ExplainRequest() {}
    public ExplainRequest(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getSqlQuery() { return sqlQuery; }
    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }
}
