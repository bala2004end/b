package com.aidb.assistant.rag;

import com.aidb.assistant.dto.OptimizationDTO;
import com.aidb.assistant.entity.ConnectionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * Provides AI-powered insight capabilities: execution plan analysis and query optimization.
 */
@Service
@RequiredArgsConstructor
public class AiInsightService {

    private static final Logger log = LoggerFactory.getLogger(AiInsightService.class);

    private final GeminiLlmService geminiLlmService;
    private final TargetDatabasePoolManager poolManager;
    private final ObjectMapper objectMapper;

    public String generateSummary(String userQuestion, String sql, int rowCount,
                                   List<Map<String, Object>> data, String apiKey) {
        String sampleStr = (data != null && !data.isEmpty())
                ? data.subList(0, Math.min(3, data.size())).toString()
                : "No rows returned";
        return geminiLlmService.generateExplanation(userQuestion, sql, String.valueOf(rowCount), sampleStr, apiKey);
    }

    /**
     * Runs EXPLAIN on the SQL query using the pooled connection — no raw DriverManager.
     */
    public List<Map<String, Object>> explainExecutionPlan(ConnectionConfig config, String sqlQuery) {
        String explainSql = "EXPLAIN " + sqlQuery;
        List<Map<String, Object>> explainRows = new ArrayList<>();

        try (Connection conn = poolManager.getDataSource(config).getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(explainSql)) {

            ResultSetMetaData md = rs.getMetaData();
            int colCount = md.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                explainRows.add(row);
            }
        } catch (Exception e) {
            log.warn("EXPLAIN query failed for SQL [{}]: {}", sqlQuery, e.getMessage());
        }

        return explainRows;
    }

    /**
     * Calls Gemini to analyze and optimize the SQL, then parses the structured JSON response.
     */
    public OptimizationDTO generateOptimization(String sqlQuery, String apiKey) {
        try {
            String jsonResponse = geminiLlmService.generateOptimization(sqlQuery, apiKey);

            // Strip any residual markdown code fences before parsing
            String cleanJson = jsonResponse
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(cleanJson, Map.class);

            String optimizedSql = getStringOrDefault(parsed, "optimizedSql", sqlQuery);
            String explanation = getStringOrDefault(parsed, "explanation", "No optimization details available.");
            List<String> indexRecs = getListOrDefault(parsed, "indexRecommendations");
            List<String> bottlenecks = getListOrDefault(parsed, "potentialBottlenecks");

            return OptimizationDTO.builder()
                    .originalSql(sqlQuery)
                    .optimizedSql(optimizedSql)
                    .explanation(explanation)
                    .indexRecommendations(indexRecs)
                    .potentialBottlenecks(bottlenecks)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse Gemini optimization response: {}", e.getMessage());
            return OptimizationDTO.builder()
                    .originalSql(sqlQuery)
                    .optimizedSql(sqlQuery)
                    .explanation("Optimization analysis could not be completed: " + e.getMessage())
                    .indexRecommendations(List.of())
                    .potentialBottlenecks(List.of())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getListOrDefault(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    private String getStringOrDefault(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return (value != null) ? value.toString() : defaultValue;
    }
}
