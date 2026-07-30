package com.aidb.assistant.controller;

import com.aidb.assistant.dto.ChatMessageDTO;
import com.aidb.assistant.dto.ChatRequest;
import com.aidb.assistant.dto.ChatResponse;
import com.aidb.assistant.dto.ExplainRequest;
import com.aidb.assistant.dto.OptimizationDTO;
import com.aidb.assistant.rag.AiInsightService;
import com.aidb.assistant.security.CurrentUser;
import com.aidb.assistant.service.ChatService;
import com.aidb.assistant.service.DatabaseConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AiInsightService aiInsightService;
    private final DatabaseConnectionService connectionService;

    @PostMapping
    public ResponseEntity<ChatResponse> askQuestion(
            @Valid @RequestBody ChatRequest request,
            @CurrentUser String username) {
        return ResponseEntity.ok(chatService.processUserQuestion(request, username));
    }

    @PostMapping("/explain")
    public ResponseEntity<List<Map<String, Object>>> explainQuery(
            @Valid @RequestBody ExplainRequest request,
            @CurrentUser String username) {
        return connectionService.getActiveConnection(username)
                .map(config -> ResponseEntity.ok(aiInsightService.explainExecutionPlan(config, request.getSqlQuery())))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizationDTO> optimizeQuery(
            @Valid @RequestBody ExplainRequest request,
            @CurrentUser String username,
            @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(aiInsightService.generateOptimization(request.getSqlQuery(), apiKey));
    }

}
