package com.aidb.assistant.service;

import com.aidb.assistant.dto.ConnectionRequest;
import com.aidb.assistant.dto.ConnectionResponse;
import com.aidb.assistant.dto.SchemaDTO;
import com.aidb.assistant.entity.ConnectionConfig;
import com.aidb.assistant.rag.DatabaseMetadataExtractor;
import com.aidb.assistant.rag.SchemaVectorStoreService;
import com.aidb.assistant.repository.ConnectionConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DatabaseConnectionService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionService.class);

    private final ConnectionConfigRepository connectionRepository;
    private final DatabaseMetadataExtractor metadataExtractor;
    private final SchemaVectorStoreService vectorStoreService;

    public DatabaseConnectionService(ConnectionConfigRepository connectionRepository, DatabaseMetadataExtractor metadataExtractor, SchemaVectorStoreService vectorStoreService) {
        this.connectionRepository = connectionRepository;
        this.metadataExtractor = metadataExtractor;
        this.vectorStoreService = vectorStoreService;
    }

    public ConnectionResponse testConnection(ConnectionRequest req) {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                req.getHost(), req.getPort(), req.getDatabaseName());

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.getConnection(url, req.getUsername(), req.getPassword()).close();
            return ConnectionResponse.builder()
                    .connected(true)
                    .message("MySQL connection successful!")
                    .host(req.getHost())
                    .port(req.getPort())
                    .databaseName(req.getDatabaseName())
                    .build();
        } catch (Exception e) {
            return ConnectionResponse.builder()
                    .connected(false)
                    .message("Connection failed: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ConnectionResponse connectAndIndex(ConnectionRequest req) {
        connectionRepository.findAll().forEach(c -> {
            c.setIsActive(false);
            connectionRepository.save(c);
        });

        ConnectionConfig config = ConnectionConfig.builder()
                .name(req.getDatabaseName())
                .host(req.getHost())
                .port(req.getPort())
                .username(req.getUsername())
                .password(req.getPassword())
                .databaseName(req.getDatabaseName())
                .isReadOnly(req.getIsReadOnly() != null ? req.getIsReadOnly() : true)
                .isActive(true)
                .lastConnectedAt(LocalDateTime.now())
                .build();

        ConnectionConfig savedConfig = connectionRepository.save(config);

        try {
            SchemaDTO schema = metadataExtractor.extractMetadata(savedConfig);

            vectorStoreService.indexSchema(schema);

            return ConnectionResponse.builder()
                    .connected(true)
                    .message("Successfully connected and indexed schema in Vector Store!")
                    .databaseName(savedConfig.getDatabaseName())
                    .host(savedConfig.getHost())
                    .port(savedConfig.getPort())
                    .isReadOnly(savedConfig.getIsReadOnly())
                    .totalTables(schema.getTotalTables())
                    .totalEmbeddings(vectorStoreService.getIndexedChunkCount())
                    .build();

        } catch (Exception e) {
            log.error("Error extracting schema or vector indexing: {}", e.getMessage(), e);
            return ConnectionResponse.builder()
                    .connected(false)
                    .message("Connected to MySQL, but schema indexing failed: " + e.getMessage())
                    .build();
        }
    }

    public Optional<ConnectionConfig> getActiveConnection() {
        return connectionRepository.findByIsActiveTrue();
    }

    @Transactional
    public void toggleReadOnly(boolean readOnly) {
        connectionRepository.findByIsActiveTrue().ifPresent(c -> {
            c.setIsReadOnly(readOnly);
            connectionRepository.save(c);
        });
    }
}
