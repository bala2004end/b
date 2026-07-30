package com.aidb.assistant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central Spring application configuration beans.
 * Provides the EmbeddingModel and ObjectMapper as singletons.
 */
@Configuration
public class AppConfig {

    @Value("${aidb.gemini.api-key}")
    private String geminiApiKey;

    @Value("${aidb.gemini.embedding-model-name:text-embedding-004}")
    private String embeddingModelName;

    /**
     * Gemini text embedding model — used by SchemaVectorStoreService for
     * real semantic embeddings during schema indexing and retrieval.
     * text-embedding-004 produces 768-dimensional vectors.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName(embeddingModelName)
                .build();
    }

    /**
     * Spring-managed ObjectMapper with JavaTimeModule for LocalDateTime serialization.
     * Injected into services to avoid creating new instances per request.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
