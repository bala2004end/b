package com.aidb.assistant.controller;

import com.aidb.assistant.dto.SettingsDTO;
import com.aidb.assistant.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final DatabaseConnectionService connectionService;

    @Value("${aidb.gemini.api-key:YOUR_GEMINI_API_KEY_HERE}")
    private String defaultApiKey;

    @Value("${aidb.gemini.model-name:gemini-1.5-flash}")
    private String modelName;

    public SettingsController(DatabaseConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping
    public ResponseEntity<SettingsDTO> getSettings() {
        boolean isReadOnly = connectionService.getActiveConnection()
                .map(c -> Boolean.TRUE.equals(c.getIsReadOnly()))
                .orElse(true);

        return ResponseEntity.ok(SettingsDTO.builder()
                .geminiApiKey(defaultApiKey.startsWith("YOUR_") ? "" : defaultApiKey)
                .modelName(modelName)
                .isReadOnly(isReadOnly)
                .maxRowsLimit(500)
                .topKSchemaChunks(5)
                .build());
    }

    @PostMapping
    public ResponseEntity<SettingsDTO> updateSettings(@RequestBody SettingsDTO dto) {
        if (dto.getIsReadOnly() != null) {
            connectionService.toggleReadOnly(dto.getIsReadOnly());
        }
        return ResponseEntity.ok(dto);
    }
}
