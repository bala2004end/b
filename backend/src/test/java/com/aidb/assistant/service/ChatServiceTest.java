package com.aidb.assistant.service;

import com.aidb.assistant.dto.ChatMessageDTO;
import com.aidb.assistant.dto.ChatRequest;
import com.aidb.assistant.dto.ChatResponse;
import com.aidb.assistant.entity.ChatMessage;
import com.aidb.assistant.entity.ConnectionConfig;
import com.aidb.assistant.entity.Conversation;
import com.aidb.assistant.entity.User;
import com.aidb.assistant.mapper.ChatMessageMapper;
import com.aidb.assistant.rag.AiInsightService;
import com.aidb.assistant.rag.SqlExecutionService;
import com.aidb.assistant.rag.SqlGeneratorService;
import com.aidb.assistant.rag.SqlValidatorService;
import com.aidb.assistant.repository.ChatMessageRepository;
import com.aidb.assistant.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private DatabaseConnectionService connectionService;
    @Mock private SqlGeneratorService sqlGeneratorService;
    @Mock private SqlValidatorService sqlValidatorService;
    @Mock private SqlExecutionService sqlExecutionService;
    @Mock private AiInsightService aiInsightService;
    @Mock private ConversationService conversationService;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChatMessageMapper chatMessageMapper;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private ConnectionConfig testConfig;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testConfig = ConnectionConfig.builder()
                .id(1L)
                .isReadOnly(true)
                .build();

        testConversation = Conversation.builder()
                .id(100L)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("Successfully processes user question")
    void processUserQuestion_success() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setQuestion("Show all users");
        request.setGeminiApiKey("api-key");

        when(connectionService.getActiveConnection("testuser")).thenReturn(Optional.of(testConfig));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(conversationService.getOrCreateConversation(any(), anyString(), any())).thenReturn(testConversation);
        
        String generatedSql = "SELECT * FROM users";
        when(sqlGeneratorService.generateSqlForQuestion(anyString(), anyString(), anyList())).thenReturn(generatedSql);
        
        SqlExecutionService.QueryResult queryResult = new SqlExecutionService.QueryResult(
                List.of(Collections.singletonMap("id", 1)),
                List.of("id"),
                10L,
                1,
                "NONE"
        );
        when(sqlExecutionService.executeQuery(testConfig, generatedSql)).thenReturn(queryResult);
        
        String explanation = "Here are the users.";
        when(aiInsightService.generateSummary(anyString(), anyString(), anyInt(), anyList(), anyString())).thenReturn(explanation);
        
        when(objectMapper.writeValueAsString(any())).thenReturn("[{\"id\":1}]");

        ChatResponse response = chatService.processUserQuestion(request, "testuser");

        assertThat(response.getIsSuccess()).isTrue();
        assertThat(response.getGeneratedSql()).isEqualTo(generatedSql);
        assertThat(response.getExplanation()).isEqualTo(explanation);
        assertThat(response.getRowsReturned()).isEqualTo(1);
        
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("Throws exception if no active connection")
    void processUserQuestion_noActiveConnection_throws() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("Show all users");

        when(connectionService.getActiveConnection("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.processUserQuestion(request, "testuser"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No active database connection found");
    }

    @Test
    @DisplayName("Throws exception if user not found")
    void processUserQuestion_userNotFound_throws() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("Show all users");

        when(connectionService.getActiveConnection("testuser")).thenReturn(Optional.of(testConfig));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.processUserQuestion(request, "testuser"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User not found");
    }

}
