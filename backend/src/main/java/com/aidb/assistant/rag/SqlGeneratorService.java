package com.aidb.assistant.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the RAG pipeline: retrieve relevant schema chunks → generate SQL.
 * Populates the outRetrievedTables list so the UI can display which tables were used.
 */
@Service
public class SqlGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(SqlGeneratorService.class);

    private static final int SCHEMA_CHUNKS_TO_RETRIEVE = 5;

    private final SchemaVectorStoreService vectorStoreService;
    private final GeminiLlmService geminiLlmService;
    private final SqlValidatorService sqlValidatorService;

    public SqlGeneratorService(
            SchemaVectorStoreService vectorStoreService,
            GeminiLlmService geminiLlmService,
            SqlValidatorService sqlValidatorService) {
        this.vectorStoreService = vectorStoreService;
        this.geminiLlmService = geminiLlmService;
        this.sqlValidatorService = sqlValidatorService;
    }

    /**
     * Generates a safe, sanitized SQL query for the given natural language question.
     *
     * @param userQuestion       the user's natural language question
     * @param overrideApiKey     optional per-request Gemini API key override
     * @param outRetrievedTables output list populated with RAG-retrieved table names
     * @return sanitized SQL query string
     */
    public String generateSqlForQuestion(String userQuestion, String overrideApiKey, List<String> outRetrievedTables) {
        log.info("RAG pipeline: embedding question and searching vector store for [{}]", userQuestion);

        List<String> tableNames = new ArrayList<>();
        List<String> schemaChunks = vectorStoreService.retrieveRelevantSchema(
                userQuestion, SCHEMA_CHUNKS_TO_RETRIEVE, tableNames
        );

        // Populate the caller's output list with retrieved table names
        if (outRetrievedTables != null) {
            outRetrievedTables.addAll(tableNames);
        }

        if (schemaChunks.isEmpty()) {
            log.warn("No relevant schema chunks retrieved — SQL generation may be inaccurate.");
        } else {
            log.info("RAG retrieved {} schema chunks from tables: {}", schemaChunks.size(), tableNames);
        }

        String rawSql = geminiLlmService.generateSql(userQuestion, schemaChunks, overrideApiKey);
        String sanitizedSql = sqlValidatorService.sanitizeSql(rawSql);

        log.info("Generated SQL via RAG pipeline: [{}]", sanitizedSql);
        return sanitizedSql;
    }
}
