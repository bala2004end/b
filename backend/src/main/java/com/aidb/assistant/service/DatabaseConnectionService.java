package com.aidb.assistant.service;

import com.aidb.assistant.dto.ConnectionRequest;
import com.aidb.assistant.dto.ConnectionResponse;
import com.aidb.assistant.dto.SchemaDTO;
import com.aidb.assistant.entity.ConnectionConfig;
import com.aidb.assistant.entity.User;
import com.aidb.assistant.rag.DatabaseMetadataExtractor;
import com.aidb.assistant.rag.SchemaVectorStoreService;
import com.aidb.assistant.rag.TargetDatabasePoolManager;
import com.aidb.assistant.repository.ConnectionConfigRepository;
import com.aidb.assistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DatabaseConnectionService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionService.class);

    private final ConnectionConfigRepository connectionRepository;
    private final DatabaseMetadataExtractor metadataExtractor;
    private final SchemaVectorStoreService vectorStoreService;
    private final UserRepository userRepository;
    private final TargetDatabasePoolManager poolManager;

    /**
     * Tests a connection without saving it. Uses DriverManager directly since
     * we don't want to create a pool for an untested connection.
     */
    public ConnectionResponse testConnection(ConnectionRequest req) {
        String url = buildJdbcUrl(req.getHost(), req.getPort(), req.getDatabaseName());
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (var conn = DriverManager.getConnection(url, req.getUsername(), req.getPassword())) {
                // Connection successful — close immediately
            }
            return ConnectionResponse.builder()
                    .connected(true)
                    .message("MySQL connection successful!")
                    .host(req.getHost())
                    .port(req.getPort())
                    .databaseName(req.getDatabaseName())
                    .build();
        } catch (Exception e) {
            log.warn("Connection test failed for {}:{}/{}: {}", req.getHost(), req.getPort(), req.getDatabaseName(), e.getMessage());
            return ConnectionResponse.builder()
                    .connected(false)
                    .message("Connection failed: " + sanitizeErrorMessage(e.getMessage()))
                    .build();
        }
    }

    @Transactional
    public ConnectionResponse connectAndIndex(ConnectionRequest req, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        // Deactivate all previous connections and invalidate their pools
        connectionRepository.findAllByUserId(user.getId()).forEach(c -> {
            if (Boolean.TRUE.equals(c.getIsActive())) {
                poolManager.invalidatePool(c.getId());
            }
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
                .isReadOnly(req.getIsReadOnly() != null ? req.getIsReadOnly() : Boolean.TRUE)
                .isActive(Boolean.TRUE)
                .lastConnectedAt(LocalDateTime.now())
                .user(user)
                .build();

        ConnectionConfig savedConfig = connectionRepository.save(config);

        try {
            SchemaDTO schema = metadataExtractor.extractMetadata(savedConfig);
            vectorStoreService.indexSchema(schema);

            return ConnectionResponse.builder()
                    .connected(true)
                    .message("Successfully connected and indexed schema with real embeddings!")
                    .databaseName(savedConfig.getDatabaseName())
                    .host(savedConfig.getHost())
                    .port(savedConfig.getPort())
                    .isReadOnly(savedConfig.getIsReadOnly())
                    .totalTables(schema.getTotalTables())
                    .totalEmbeddings(vectorStoreService.getIndexedChunkCount())
                    .build();

        } catch (Exception e) {
            log.error("Schema extraction/indexing failed for [{}]: {}", savedConfig.getDatabaseName(), e.getMessage(), e);
            return ConnectionResponse.builder()
                    .connected(false)
                    .message("Connected to MySQL but schema indexing failed: " + sanitizeErrorMessage(e.getMessage()))
                    .build();
        }
    }

    public Optional<ConnectionConfig> getActiveConnection(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> connectionRepository.findByUserIdAndIsActiveTrue(user.getId()));
    }

    @Transactional
    public void toggleReadOnly(boolean readOnly, String username) {
        getActiveConnection(username).ifPresent(c -> {
            c.setIsReadOnly(readOnly);
            connectionRepository.save(c);
        });
    }

    private String buildJdbcUrl(String host, int port, String database) {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000",
            host, port, database);
    }

    /**
     * Strips potentially sensitive information (passwords, IPs) from exception messages
     * before returning them to the client.
     */
    private String sanitizeErrorMessage(String message) {
        if (message == null) return "Unknown error";
        // Remove anything that looks like a password in the connection string
        return message.replaceAll("password=[^&\\s]+", "password=***")
                      .replaceAll("(?i)access denied for user '[^']+' using password: .*", "Access denied — check credentials");
    }
}
