package com.aidb.assistant.repository;

import com.aidb.assistant.entity.ConnectionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectionConfigRepository extends JpaRepository<ConnectionConfig, Long> {
    Optional<ConnectionConfig> findByIsActiveTrue();
}
