package com.aidb.assistant.controller;

import com.aidb.assistant.dto.ConnectionRequest;
import com.aidb.assistant.dto.ConnectionResponse;
import com.aidb.assistant.entity.ConnectionConfig;
import com.aidb.assistant.service.DatabaseConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/connection")
public class DatabaseConnectionController {

    private final DatabaseConnectionService connectionService;

    public DatabaseConnectionController(DatabaseConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/test")
    public ResponseEntity<ConnectionResponse> testConnection(@RequestBody ConnectionRequest request) {
        return ResponseEntity.ok(connectionService.testConnection(request));
    }

    @PostMapping("/connect")
    public ResponseEntity<ConnectionResponse> connectDatabase(@RequestBody ConnectionRequest request) {
        return ResponseEntity.ok(connectionService.connectAndIndex(request));
    }

    @GetMapping("/active")
    public ResponseEntity<ConnectionConfig> getActiveConnection() {
        return connectionService.getActiveConnection()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/read-only")
    public ResponseEntity<Map<String, Boolean>> toggleReadOnly(@RequestBody Map<String, Boolean> body) {
        boolean readOnly = body.getOrDefault("isReadOnly", true);
        connectionService.toggleReadOnly(readOnly);
        return ResponseEntity.ok(Map.of("isReadOnly", readOnly));
    }
}
