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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core AI chat pipeline service.
 * Orchestrates: RAG schema retrieval → SQL generation → validation → execution → explanation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final DatabaseConnectionService connectionService;
    private final SqlGeneratorService sqlGeneratorService;
    private final SqlValidatorService sqlValidatorService;
    private final SqlExecutionService sqlExecutionService;
    private final AiInsightService aiInsightService;
    private final ConversationService conversationService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final ObjectMapper objectMapper; // Spring-managed bean from AppConfig

    @Transactional
    public ChatResponse processUserQuestion(ChatRequest request, String username) {
        ConnectionConfig activeConfig = connectionService.getActiveConnection(username)
                .orElseThrow(() -> new IllegalStateException(
                        "No active database connection found. Please connect to a database first."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        Conversation conversation = conversationService.getOrCreateConversation(
                request.getConversationId(), request.getQuestion(), user);

        // Save user message
        ChatMessage userMsg = ChatMessage.builder()
                .conversation(conversation)
                .sender("USER")
                .content(request.getQuestion())
                .build();
        chatMessageRepository.save(userMsg);

        // --- RAG Pipeline ---
        List<String> retrievedTables = new ArrayList<>();
        String generatedSql = null;
        String explanation = null;
        SqlExecutionService.QueryResult queryResult = null;
        String errorMessage = null;
        boolean isSuccess = false;

        try {
            generatedSql = sqlGeneratorService.generateSqlForQuestion(
                    request.getQuestion(), request.getGeminiApiKey(), retrievedTables);

            sqlValidatorService.validateSql(generatedSql, Boolean.TRUE.equals(activeConfig.getIsReadOnly()));

            queryResult = sqlExecutionService.executeQuery(activeConfig, generatedSql);
            isSuccess = true;

            explanation = aiInsightService.generateSummary(
                    request.getQuestion(), generatedSql,
                    queryResult.rowCount, queryResult.data, request.getGeminiApiKey());

        } catch (Exception e) {
            log.error("AI Chat Pipeline error for user [{}]: {}", username, e.getMessage(), e);
            errorMessage = e.getMessage();
            explanation = "An error occurred: " + e.getMessage();
        }

        // Serialize result to JSON
        String dataJson = null;
        if (queryResult != null && queryResult.data != null) {
            try {
                dataJson = objectMapper.writeValueAsString(queryResult.data);
            } catch (Exception ex) {
                log.warn("Failed to serialize query result to JSON: {}", ex.getMessage());
            }
        }

        // Save AI response message
        ChatMessage aiMsg = ChatMessage.builder()
                .conversation(conversation)
                .sender("AI")
                .content(explanation)
                .generatedSql(generatedSql)
                .explanation(explanation)
                .executionTimeMs(queryResult != null ? queryResult.executionTimeMs : 0L)
                .rowsReturned(queryResult != null ? queryResult.rowCount : 0)
                .isSuccess(isSuccess)
                .errorMessage(errorMessage)
                .resultJson(dataJson)
                .chartType(queryResult != null ? queryResult.chartType : "NONE")
                .build();

        chatMessageRepository.save(aiMsg);

        return ChatResponse.builder()
                .messageId(aiMsg.getId())
                .conversationId(conversation.getId())
                .question(request.getQuestion())
                .generatedSql(generatedSql)
                .explanation(explanation)
                .executionTimeMs(queryResult != null ? queryResult.executionTimeMs : 0L)
                .rowsReturned(queryResult != null ? queryResult.rowCount : 0)
                .isSuccess(isSuccess)
                .errorMessage(errorMessage)
                .data(queryResult != null ? queryResult.data : Collections.emptyList())
                .columns(queryResult != null ? queryResult.columns : Collections.emptyList())
                .chartType(queryResult != null ? queryResult.chartType : "NONE")
                .retrievedTables(retrievedTables)
                .build();
    }

}
