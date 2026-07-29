package com.aidb.assistant.service;

import com.aidb.assistant.dto.SchemaDTO;
import com.aidb.assistant.rag.SchemaVectorStoreService;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {

    private final SchemaVectorStoreService vectorStoreService;

    public SchemaService(SchemaVectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    public SchemaDTO getSchema() {
        return vectorStoreService.getCurrentSchema();
    }
}
