package com.aidb.assistant.rag;

import com.aidb.assistant.dto.ColumnDTO;
import com.aidb.assistant.dto.ForeignKeyDTO;
import com.aidb.assistant.dto.SchemaDTO;
import com.aidb.assistant.dto.TableDTO;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchemaVectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(SchemaVectorStoreService.class);

    private final InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    private final Map<String, String> schemaChunkMap = new HashMap<>();
    private SchemaDTO currentSchema;
    private int indexedChunkCount = 0;

    public synchronized void indexSchema(SchemaDTO schema) {
        this.currentSchema = schema;
        this.schemaChunkMap.clear();
        this.indexedChunkCount = 0;

        if (schema == null || schema.getTables() == null) {
            return;
        }

        log.info("Indexing {} tables into Vector Store RAG pipeline...", schema.getTables().size());

        for (TableDTO table : schema.getTables()) {
            StringBuilder chunk = new StringBuilder();
            chunk.append("Table Name: ").append(table.getTableName()).append("\n");
            chunk.append("Table Type: ").append(table.getTableType()).append("\n");
            if (table.getRemarks() != null && !table.getRemarks().isBlank()) {
                chunk.append("Description: ").append(table.getRemarks()).append("\n");
            }

            chunk.append("Columns:\n");
            for (ColumnDTO col : table.getColumns()) {
                chunk.append("  - ").append(col.getColumnName())
                     .append(" (").append(col.getDataType()).append(")");
                if (Boolean.TRUE.equals(col.getIsPrimaryKey())) {
                    chunk.append(" [PRIMARY KEY]");
                }
                if (Boolean.TRUE.equals(col.getIsForeignKey())) {
                    chunk.append(" [FOREIGN KEY -> ")
                         .append(col.getFkReferencedTable()).append("(").append(col.getFkReferencedColumn()).append(")]");
                }
                chunk.append("\n");
            }

            if (!table.getForeignKeys().isEmpty()) {
                chunk.append("Relationships & Foreign Keys:\n");
                for (ForeignKeyDTO fk : table.getForeignKeys()) {
                    chunk.append("  - ").append(table.getTableName()).append(".").append(fk.getFkColumnName())
                         .append(" references ").append(fk.getPkTableName()).append("(").append(fk.getPkColumnName()).append(")\n");
                }
            }

            String chunkText = chunk.toString();
            schemaChunkMap.put(table.getTableName().toLowerCase(), chunkText);

            Metadata metadata = new Metadata();
            metadata.put("tableName", table.getTableName());

            TextSegment segment = TextSegment.from(chunkText, metadata);
            embeddingStore.add(segment);
            indexedChunkCount++;
        }

        log.info("Successfully indexed {} schema vector chunks into Vector Store.", indexedChunkCount);
    }

    public List<String> retrieveRelevantSchema(String userQuestion, int maxResults) {
        if (schemaChunkMap.isEmpty()) {
            log.warn("Vector Store is empty! Returning fallback full schema.");
            return new ArrayList<>(schemaChunkMap.values());
        }

        String queryLower = userQuestion.toLowerCase();
        List<String> matchedChunks = new ArrayList<>();
        Set<String> matchedTableNames = new HashSet<>();

        for (Map.Entry<String, String> entry : schemaChunkMap.entrySet()) {
            String tableName = entry.getKey();
            String chunkContent = entry.getValue().toLowerCase();

            if (queryLower.contains(tableName) || 
                queryLower.contains(tableName.replaceAll("s$", "")) || 
                containsAnyKeyword(queryLower, chunkContent)) {
                matchedTableNames.add(tableName);
                matchedChunks.add(entry.getValue());
            }
        }

        if (!matchedChunks.isEmpty()) {
            return matchedChunks.stream().limit(maxResults).collect(Collectors.toList());
        }

        return schemaChunkMap.values().stream().limit(maxResults).collect(Collectors.toList());
    }

    private boolean containsAnyKeyword(String query, String chunkContent) {
        String[] keywords = {"employee", "salary", "department", "product", "stock", "customer", "order", "purchase", "amount", "joined", "hire", "category", "spent", "item", "view"};
        for (String kw : keywords) {
            if (query.contains(kw) && chunkContent.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public SchemaDTO getCurrentSchema() {
        return currentSchema;
    }

    public int getIndexedChunkCount() {
        return indexedChunkCount;
    }
}
