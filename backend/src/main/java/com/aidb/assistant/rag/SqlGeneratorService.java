package com.aidb.assistant.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SqlGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(SqlGeneratorService.class);

    private final SchemaVectorStoreService vectorStoreService;
    private final GeminiLlmService geminiLlmService;
    private final SqlValidatorService sqlValidatorService;

    public SqlGeneratorService(SchemaVectorStoreService vectorStoreService, GeminiLlmService geminiLlmService, SqlValidatorService sqlValidatorService) {
        this.vectorStoreService = vectorStoreService;
        this.geminiLlmService = geminiLlmService;
        this.sqlValidatorService = sqlValidatorService;
    }

    public String generateSqlForQuestion(String userQuestion, String overrideApiKey, List<String> outRetrievedTables) {
        log.info("Searching Vector DB RAG for relevant schema for question: [{}]", userQuestion);

        List<String> schemaChunks = vectorStoreService.retrieveRelevantSchema(userQuestion, 5);

        String rawSql = geminiLlmService.generateSql(userQuestion, schemaChunks, overrideApiKey);

        String sanitizedSql = sqlValidatorService.sanitizeSql(rawSql);

        log.info("Successfully generated SQL query via RAG Gemini pipeline: [{}]", sanitizedSql);
        return sanitizedSql;
    }
}
