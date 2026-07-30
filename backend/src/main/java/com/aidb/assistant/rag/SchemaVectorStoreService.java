package com.aidb.assistant.rag;

import com.aidb.assistant.dto.ColumnDTO;
import com.aidb.assistant.dto.ForeignKeyDTO;
import com.aidb.assistant.dto.SchemaDTO;
import com.aidb.assistant.dto.TableDTO;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Indexes the target database schema into an in-memory vector store using real
 * Gemini text embeddings. Semantic similarity search replaces the previous
 * keyword-matching fallback, enabling accurate RAG retrieval.
 */
@Service
public class SchemaVectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(SchemaVectorStoreService.class);
    private static final int DEFAULT_MAX_RESULTS = 5;
    private static final double SIMILARITY_THRESHOLD = 0.60;

    private final EmbeddingModel embeddingModel;

    // Thread-safe store — replaced on each indexSchema() call
    private volatile InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    private volatile SchemaDTO currentSchema;
    private final AtomicInteger indexedChunkCount = new AtomicInteger(0);

    public SchemaVectorStoreService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * Indexes all tables from the given schema into the embedding store.
     * Replaces the previous store on each call so stale data is never returned.
     */
    public synchronized void indexSchema(SchemaDTO schema) {
        if (schema == null || schema.getTables() == null || schema.getTables().isEmpty()) {
            log.warn("Attempted to index an empty or null schema — skipping.");
            return;
        }

        log.info("Indexing {} tables into Vector Store via Gemini embeddings...", schema.getTables().size());

        InMemoryEmbeddingStore<TextSegment> newStore = new InMemoryEmbeddingStore<>();
        int count = 0;

        for (TableDTO table : schema.getTables()) {
            String chunkText = buildSchemaChunk(table);

            Metadata metadata = new Metadata();
            metadata.put("tableName", table.getTableName());
            metadata.put("tableType", table.getTableType() != null ? table.getTableType() : "TABLE");

            TextSegment segment = TextSegment.from(chunkText, metadata);

            try {
                Embedding embedding = embeddingModel.embed(chunkText).content();
                newStore.add(embedding, segment);
                count++;
            } catch (Exception e) {
                log.error("Failed to embed schema for table [{}]: {}", table.getTableName(), e.getMessage());
            }
        }

        // Atomically swap to new store
        this.embeddingStore = newStore;
        this.currentSchema = schema;
        this.indexedChunkCount.set(count);

        log.info("Successfully indexed {} schema chunks into Vector Store.", count);
    }

    /**
     * Retrieves the most semantically relevant schema chunks for the given question
     * using real vector similarity search.
     *
     * @param userQuestion    natural language question
     * @param maxResults      maximum number of chunks to return
     * @param outTableNames   output list populated with matched table names for UI display
     * @return list of schema text chunks ordered by relevance
     */
    public List<String> retrieveRelevantSchema(String userQuestion, int maxResults, List<String> outTableNames) {
        if (indexedChunkCount.get() == 0) {
            log.warn("Vector Store is empty — no schema has been indexed yet.");
            return List.of();
        }

        int limit = maxResults > 0 ? maxResults : DEFAULT_MAX_RESULTS;

        try {
            Embedding queryEmbedding = embeddingModel.embed(userQuestion).content();

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(limit)
                    .minScore(SIMILARITY_THRESHOLD)
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();

            if (matches.isEmpty()) {
                log.info("No high-confidence schema matches found (threshold={}) — lowering threshold for fallback.", SIMILARITY_THRESHOLD);
                // Fallback: return top-N without threshold
                EmbeddingSearchRequest fallbackRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(limit)
                        .build();
                matches = embeddingStore.search(fallbackRequest).matches();
            }

            Set<String> seenTables = new HashSet<>();
            List<String> results = new ArrayList<>();

            for (EmbeddingMatch<TextSegment> match : matches) {
                String tableName = match.embedded().metadata().getString("tableName");
                if (tableName != null && seenTables.add(tableName)) {
                    results.add(match.embedded().text());
                    if (outTableNames != null) {
                        outTableNames.add(tableName);
                    }
                    log.debug("RAG match: table={}, score={:.4f}", tableName, match.score());
                }
            }

            return results;
        } catch (Exception e) {
            log.error("Vector similarity search failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Builds a rich text representation of a table for embedding.
     * Includes table name, type, columns with types/constraints, and FK relationships.
     */
    private String buildSchemaChunk(TableDTO table) {
        StringBuilder sb = new StringBuilder();
        sb.append("Table: ").append(table.getTableName()).append("\n");
        sb.append("Type: ").append(table.getTableType()).append("\n");

        if (table.getRemarks() != null && !table.getRemarks().isBlank()) {
            sb.append("Description: ").append(table.getRemarks()).append("\n");
        }

        sb.append("Columns:\n");
        for (ColumnDTO col : table.getColumns()) {
            sb.append("  - ").append(col.getColumnName())
              .append(" (").append(col.getDataType()).append(")");
            if (Boolean.TRUE.equals(col.getIsPrimaryKey())) {
                sb.append(" [PRIMARY KEY]");
            }
            if (Boolean.FALSE.equals(col.getIsNullable())) {
                sb.append(" [NOT NULL]");
            }
            if (Boolean.TRUE.equals(col.getIsForeignKey())) {
                sb.append(" [FK -> ").append(col.getFkReferencedTable())
                  .append(".").append(col.getFkReferencedColumn()).append("]");
            }
            if (col.getRemarks() != null && !col.getRemarks().isBlank()) {
                sb.append(" -- ").append(col.getRemarks());
            }
            sb.append("\n");
        }

        if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
            sb.append("Relationships:\n");
            for (ForeignKeyDTO fk : table.getForeignKeys()) {
                sb.append("  - ").append(table.getTableName()).append(".").append(fk.getFkColumnName())
                  .append(" references ").append(fk.getPkTableName()).append(".").append(fk.getPkColumnName()).append("\n");
            }
        }

        return sb.toString();
    }

    public SchemaDTO getCurrentSchema() {
        return currentSchema;
    }

    public int getIndexedChunkCount() {
        return indexedChunkCount.get();
    }
}
