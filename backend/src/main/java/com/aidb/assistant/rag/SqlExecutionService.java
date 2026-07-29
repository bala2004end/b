package com.aidb.assistant.rag;

import com.aidb.assistant.entity.ConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class SqlExecutionService {

    private static final Logger log = LoggerFactory.getLogger(SqlExecutionService.class);

    public QueryResult executeQuery(ConnectionConfig config, String sqlQuery) throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                config.getHost(), config.getPort(), config.getDatabaseName());

        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();

        log.info("Executing target SQL query on connection [{}]: {}", config.getDatabaseName(), sqlQuery);

        try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword());
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(30);

            try (ResultSet rs = stmt.executeQuery(sqlQuery)) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    columnNames.add(rsmd.getColumnLabel(i));
                }

                while (rs.next()) {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String colName = rsmd.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        rowMap.put(colName, val);
                    }
                    dataList.add(rowMap);
                }
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        String chartType = detectChartType(columnNames, dataList);

        return new QueryResult(dataList, columnNames, executionTimeMs, dataList.size(), chartType);
    }

    private String detectChartType(List<String> columns, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty() || columns.size() < 2) {
            return "NONE";
        }

        boolean hasNumberCol = false;
        for (String col : columns) {
            Object val = rows.get(0).get(col);
            if (val instanceof Number) {
                hasNumberCol = true;
                break;
            }
        }

        if (!hasNumberCol) {
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

        public QueryResult(List<Map<String, Object>> data, List<String> columns, long executionTimeMs, int rowCount, String chartType) {
            this.data = data;
            this.columns = columns;
            this.executionTimeMs = executionTimeMs;
            this.rowCount = rowCount;
            this.chartType = chartType;
        }
    }
}
