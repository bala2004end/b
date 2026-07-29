package com.aidb.assistant.service;

import com.aidb.assistant.dto.ChatRequest;
import com.aidb.assistant.dto.ChatResponse;
import com.aidb.assistant.entity.*;
import com.aidb.assistant.rag.*;
import com.aidb.assistant.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final DatabaseConnectionService connectionService;
    private final SqlGeneratorService sqlGeneratorService;
    private final SqlValidatorService sqlValidatorService;
    private final SqlExecutionService sqlExecutionService;
    private final AiInsightService aiInsightService;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(DatabaseConnectionService connectionService, SqlGeneratorService sqlGeneratorService, SqlValidatorService sqlValidatorService, SqlExecutionService sqlExecutionService, AiInsightService aiInsightService, ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository, AuditLogRepository auditLogRepository) {
        this.connectionService = connectionService;
        this.sqlGeneratorService = sqlGeneratorService;
        this.sqlValidatorService = sqlValidatorService;
        this.sqlExecutionService = sqlExecutionService;
        this.aiInsightService = aiInsightService;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public ChatResponse processUserQuestion(ChatRequest request, String username) {
        ConnectionConfig activeConfig = connectionService.getActiveConnection()
                .orElseThrow(() -> new IllegalStateException("No active MySQL database connection found. Please connect to a database first."));

        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseGet(() -> createNewConversation(request.getQuestion()));
        } else {
            conversation = createNewConversation(request.getQuestion());
        }

        ChatMessage userMsg = ChatMessage.builder()
                .conversation(conversation)
                .sender("USER")
                .content(request.getQuestion())
                .build();
        chatMessageRepository.save(userMsg);

        List<String> retrievedTables = new ArrayList<>();
        String generatedSql = null;
        String explanation = null;
        SqlExecutionService.QueryResult queryResult = null;
        String errorMessage = null;
        boolean isSuccess = false;

        try {
            generatedSql = sqlGeneratorService.generateSqlForQuestion(request.getQuestion(), request.getGeminiApiKey(), retrievedTables);

            sqlValidatorService.validateSql(generatedSql, activeConfig.getIsReadOnly());

            queryResult = sqlExecutionService.executeQuery(activeConfig, generatedSql);
            isSuccess = true;

            explanation = aiInsightService.generateSummary(
                    request.getQuestion(),
                    generatedSql,
                    queryResult.rowCount,
                    queryResult.data,
                    request.getGeminiApiKey()
            );

        } catch (Exception e) {
            log.error("Error in AI Chat Pipeline: {}", e.getMessage(), e);
            errorMessage = e.getMessage();
            explanation = "Failed to complete query processing: " + e.getMessage();
        }

        String dataJson = null;
        if (queryResult != null && queryResult.data != null) {
            try {
                dataJson = objectMapper.writeValueAsString(queryResult.data);
            } catch (Exception ignored) {}
        }

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

        AuditLog audit = AuditLog.builder()
                .username(username != null ? username : "anonymous")
                .action("EXECUTE_SQL")
                .details(generatedSql != null ? generatedSql : request.getQuestion())
                .executionTimeMs(queryResult != null ? queryResult.executionTimeMs : 0L)
                .isSuccess(isSuccess)
                .build();
        auditLogRepository.save(audit);

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

    private Conversation createNewConversation(String initialQuestion) {
        String title = initialQuestion.length() > 30 ? initialQuestion.substring(0, 30) + "..." : initialQuestion;
        Conversation conversation = Conversation.builder()
                .title(title)
                .build();
        return conversationRepository.save(conversation);
    }
}
