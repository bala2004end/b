package com.aidb.assistant.service;

import com.aidb.assistant.dto.AnalyticsDTO;
import com.aidb.assistant.entity.AuditLog;
import com.aidb.assistant.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalyticsService {

    private final AuditLogRepository auditLogRepository;

    public AnalyticsService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AnalyticsDTO getAnalytics() {
        long totalExecuted = auditLogRepository.count();
        Double avgMs = auditLogRepository.getAverageExecutionTime();

        List<AuditLog> slowLogs = auditLogRepository.findTop10ByOrderByExecutionTimeMsDesc();
        List<Map<String, Object>> slowQueryMap = new ArrayList<>();
        List<String> indexRecs = new ArrayList<>();

        for (AuditLog log : slowLogs) {
            if (log.getExecutionTimeMs() != null && log.getExecutionTimeMs() > 50) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", log.getId());
                m.put("query", log.getDetails());
                m.put("executionTimeMs", log.getExecutionTimeMs());
                m.put("timestamp", log.getTimestamp());
                slowQueryMap.add(m);

                if (log.getDetails() != null && log.getDetails().toLowerCase().contains("where")) {
                    indexRecs.add("Consider adding composite index on WHERE clause columns for: " + log.getDetails());
                }
            }
        }

        if (indexRecs.isEmpty()) {
            indexRecs.add("All queries executing within sub-50ms parameters.");
        }

        return AnalyticsDTO.builder()
                .totalQueriesExecuted(totalExecuted)
                .avgExecutionTimeMs(avgMs != null ? Math.round(avgMs * 100.0) / 100.0 : 0.0)
                .slowQueriesCount((long) slowQueryMap.size())
                .slowQueryLogs(slowQueryMap)
                .indexRecommendations(indexRecs)
                .build();
    }
}
