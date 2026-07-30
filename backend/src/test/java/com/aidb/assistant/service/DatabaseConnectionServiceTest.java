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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionServiceTest {

    @Mock private ConnectionConfigRepository connectionRepository;
    @Mock private DatabaseMetadataExtractor metadataExtractor;
    @Mock private SchemaVectorStoreService vectorStoreService;
    @Mock private UserRepository userRepository;
    @Mock private TargetDatabasePoolManager poolManager;

    @InjectMocks
    private DatabaseConnectionService connectionService;

    private User testUser;
    private ConnectionConfig existingConfig;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        existingConfig = ConnectionConfig.builder()
                .id(100L)
                .isActive(true)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("connectAndIndex invalidates old pools and creates new connection")
    void connectAndIndex_success() throws Exception {
        ConnectionRequest req = new ConnectionRequest();
        req.setHost("localhost");
        req.setPort(3306);
        req.setUsername("root");
        req.setPassword("pass");
        req.setDatabaseName("testdb");
        req.setIsReadOnly(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(connectionRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(existingConfig));
        when(connectionRepository.save(any(ConnectionConfig.class))).thenAnswer(i -> {
            ConnectionConfig c = i.getArgument(0);
            c.setId(101L);
            return c;
        });

        SchemaDTO dummySchema = SchemaDTO.builder().databaseName("testdb").totalTables(5).tables(Collections.emptyList()).build();
        when(metadataExtractor.extractMetadata(any(ConnectionConfig.class))).thenReturn(dummySchema);
        when(vectorStoreService.getIndexedChunkCount()).thenReturn(10);

        ConnectionResponse response = connectionService.connectAndIndex(req, "testuser");

        assertThat(response.getConnected()).isTrue();
        assertThat(response.getTotalTables()).isEqualTo(5);
        assertThat(response.getTotalEmbeddings()).isEqualTo(10);

        // Verify old pool was invalidated
        verify(poolManager).invalidatePool(100L);
        assertThat(existingConfig.getIsActive()).isFalse();

        // Verify schema indexing was called
        verify(vectorStoreService).indexSchema(dummySchema);
    }

    @Test
    @DisplayName("testConnection fails gracefully for invalid host")
    void testConnection_invalidHost_fails() {
        ConnectionRequest req = new ConnectionRequest();
        req.setHost("invalid-host-that-does-not-exist");
        req.setPort(3306);
        req.setUsername("root");
        req.setPassword("pass");
        req.setDatabaseName("testdb");

        ConnectionResponse response = connectionService.testConnection(req);

        assertThat(response.getConnected()).isFalse();
        assertThat(response.getMessage()).contains("Connection failed");
    }

    @Test
    @DisplayName("toggleReadOnly updates config successfully")
    void toggleReadOnly_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(connectionRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.of(existingConfig));

        connectionService.toggleReadOnly(false, "testuser");

        assertThat(existingConfig.getIsReadOnly()).isFalse();
        verify(connectionRepository).save(existingConfig);
    }
}
