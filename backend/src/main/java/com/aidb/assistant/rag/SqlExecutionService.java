package com.aidb.assistant.rag;

import com.aidb.assistant.entity.ConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * Executes SQL queries against the target database using the pooled connection manager.
 * Enforces a configurable maximum row limit to prevent runaway queries from consuming
 * excessive memory or exposing large datasets.
 */
@Service
public class SqlExecutionService {

    private static final Logger log = LoggerFactory.getLogger(SqlExecutionService.class);
    private static final int QUERY_TIMEOUT_SECONDS = 30;

    private final TargetDatabasePoolManager poolManager;

    @Value("${aidb.sql.max-rows:500}")
    private int maxRows;

    public SqlExecutionService(TargetDatabasePoolManager poolManager) {
        this.poolManager = poolManager;
    }

    public QueryResult executeQuery(ConnectionConfig config, String sqlQuery) throws SQLException {
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();

        log.info("Executing SQL on [{}] (maxRows={}): {}", config.getDatabaseName(), maxRows, sqlQuery);

        try (Connection conn = poolManager.getDataSource(config).getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            stmt.setMaxRows(maxRows); // Enforce row limit at JDBC driver level

            try (ResultSet rs = stmt.executeQuery(sqlQuery)) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    columnNames.add(rsmd.getColumnLabel(i));
                }

                while (rs.next()) {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        Object val = rs.getObject(i);
                        // Convert SQL Date/Timestamp to strings for safe JSON serialization
                        if (val instanceof java.sql.Date sqlDate) {
                            val = sqlDate.toString();
                        } else if (val instanceof java.sql.Timestamp ts) {
                            val = ts.toLocalDateTime().toString();
                        }
                        rowMap.put(rsmd.getColumnLabel(i), val);
                    }
                    dataList.add(rowMap);
                }
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        String chartType = detectChartType(columnNames, dataList);

        log.info("Query executed in {}ms, returned {} rows, chartType={}", executionTimeMs, dataList.size(), chartType);

        return new QueryResult(dataList, columnNames, executionTimeMs, dataList.size(), chartType);
    }

    private String detectChartType(List<String> columns, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty() || columns.size() < 2) {
            return "NONE";
        }

        // Check if at least one column has numeric data
        boolean hasNumericColumn = false;
        for (String col : columns) {
            Object val = rows.get(0).get(col);
            if (val instanceof Number) {
                hasNumericColumn = true;
                break;
            }
        }

        if (!hasNumericColumn) {
            return "NONE";
        }

        if (rows.size() <= 6 && columns.size() == 2) {
            return "PIE";
        } else if (rows.size() <= 20) {
            return "BAR";
        } else {
            return "LINE";
        }
    }

    public static class QueryResult {
        public final List<Map<String, Object>> data;
        public final List<String> columns;
        public final long executionTimeMs;
        public final int rowCount;
        public final String chartType;

        public QueryResult(List<Map<String, Object>> data, List<String> columns,
                           long executionTimeMs, int rowCount, String chartType) {
            this.data = Collections.unmodifiableList(data);
            this.columns = Collections.unmodifiableList(columns);
            this.executionTimeMs = executionTimeMs;
            this.rowCount = rowCount;
            this.chartType = chartType;
        }
    }
}
