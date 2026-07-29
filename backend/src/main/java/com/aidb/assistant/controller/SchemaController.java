package com.aidb.assistant.controller;

import com.aidb.assistant.dto.SchemaDTO;
import com.aidb.assistant.service.SchemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping
    public ResponseEntity<SchemaDTO> getSchema() {
        SchemaDTO schema = schemaService.getSchema();
        if (schema == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(schema);
    }
}
