package com.aidb.assistant.repository;

import com.aidb.assistant.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    
    @Query("SELECT AVG(a.executionTimeMs) FROM AuditLog a WHERE a.executionTimeMs IS NOT NULL")
    Double getAverageExecutionTime();

    List<AuditLog> findTop10ByOrderByExecutionTimeMsDesc();
}
