package com.aidb.assistant.controller;

import com.aidb.assistant.dto.ChatRequest;
import com.aidb.assistant.dto.ChatResponse;
import com.aidb.assistant.dto.ExplainRequest;
import com.aidb.assistant.dto.OptimizationDTO;
import com.aidb.assistant.entity.ChatMessage;
import com.aidb.assistant.rag.AiInsightService;
import com.aidb.assistant.repository.ChatMessageRepository;
import com.aidb.assistant.service.ChatService;
import com.aidb.assistant.service.DatabaseConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;
    private final AiInsightService aiInsightService;
    private final DatabaseConnectionService connectionService;

    public ChatController(ChatService chatService, ChatMessageRepository chatMessageRepository, AiInsightService aiInsightService, DatabaseConnectionService connectionService) {
        this.chatService = chatService;
        this.chatMessageRepository = chatMessageRepository;
        this.aiInsightService = aiInsightService;
        this.connectionService = connectionService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(chatService.processUserQuestion(request, username));
    }

    @PostMapping("/explain")
    public ResponseEntity<List<Map<String, Object>>> explainQuery(@RequestBody ExplainRequest request) {
        var activeConfig = connectionService.getActiveConnection();
        if (activeConfig.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(aiInsightService.explainExecutionPlan(activeConfig.get(), request.getSqlQuery()));
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizationDTO> optimizeQuery(@RequestBody ExplainRequest request, @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(aiInsightService.generateOptimization(request.getSqlQuery(), apiKey));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory() {
        return ResponseEntity.ok(chatMessageRepository.findTop20ByGeneratedSqlIsNotNullOrderByTimestampDesc());
    }
}
