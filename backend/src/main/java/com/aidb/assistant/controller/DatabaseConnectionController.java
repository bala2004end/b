package com.aidb.assistant.controller;

import com.aidb.assistant.dto.ConnectionRequest;
import com.aidb.assistant.dto.ConnectionResponse;
import com.aidb.assistant.service.DatabaseConnectionService;
import com.aidb.assistant.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/connection")
@RequiredArgsConstructor
public class DatabaseConnectionController {

    private final DatabaseConnectionService connectionService;

    @PostMapping("/test")
    public ResponseEntity<ConnectionResponse> testConnection(@Valid @RequestBody ConnectionRequest request) {
        return ResponseEntity.ok(connectionService.testConnection(request));
    }

    @PostMapping("/connect")
    public ResponseEntity<ConnectionResponse> connectDatabase(
            @Valid @RequestBody ConnectionRequest request,
            @CurrentUser String username) {
        return ResponseEntity.ok(connectionService.connectAndIndex(request, username));
    }

    /**
     * Returns a ConnectionResponse DTO — NOT the ConnectionConfig entity.
     * This prevents the encrypted DB password and internal fields from being
     * exposed to the HTTP client.
     */
    @GetMapping("/active")
    public ResponseEntity<ConnectionResponse> getActiveConnection(@CurrentUser String username) {
        return connectionService.getActiveConnection(username)
                .map(config -> ConnectionResponse.builder()
                        .connected(true)
                        .message("Active connection found.")
                        .host(config.getHost())
                        .port(config.getPort())
                        .databaseName(config.getDatabaseName())
                        .isReadOnly(config.getIsReadOnly())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/read-only")
    public ResponseEntity<Map<String, Boolean>> toggleReadOnly(
            @RequestBody Map<String, Boolean> body,
            @CurrentUser String username) {
        boolean readOnly = Boolean.TRUE.equals(body.get("isReadOnly"));
        connectionService.toggleReadOnly(readOnly, username);
        return ResponseEntity.ok(Map.of("isReadOnly", readOnly));
    }
}
