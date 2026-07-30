package com.aidb.assistant.rag;

import com.aidb.assistant.dto.OptimizationDTO;
import com.aidb.assistant.entity.ConnectionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiInsightServiceTest {

    @Mock private GeminiLlmService geminiLlmService;
    @Mock private TargetDatabasePoolManager poolManager;
    private ObjectMapper objectMapper = new ObjectMapper();

    private AiInsightService aiInsightService;

    @BeforeEach
    void setUp() {
        aiInsightService = new AiInsightService(geminiLlmService, poolManager, objectMapper);
    }

    @Test
    @DisplayName("generateSummary delegates to GeminiLlmService correctly")
    void generateSummary_success() {
        String expectedExplanation = "This query returns 5 active users.";
        when(geminiLlmService.generateExplanation(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedExplanation);

        String result = aiInsightService.generateSummary("How many users?", "SELECT *", 5, 
                List.of(Map.of("id", 1)), "api-key");

        assertThat(result).isEqualTo(expectedExplanation);
    }

    @Test
    @DisplayName("generateOptimization parses valid JSON from LLM")
    void generateOptimization_validJson_returnsDto() {
        String mockJsonResponse = """
                ```json
                {
                  "optimizedSql": "SELECT id FROM users LIMIT 10",
                  "explanation": "Added LIMIT for performance",
                  "indexRecommendations": ["CREATE INDEX idx_user ON users(id)"],
                  "potentialBottlenecks": ["Full table scan"]
                }
                ```
                """;

        when(geminiLlmService.generateOptimization(anyString(), anyString())).thenReturn(mockJsonResponse);

        OptimizationDTO dto = aiInsightService.generateOptimization("SELECT * FROM users", "api-key");

        assertThat(dto.getOptimizedSql()).isEqualTo("SELECT id FROM users LIMIT 10");
        assertThat(dto.getExplanation()).isEqualTo("Added LIMIT for performance");
        assertThat(dto.getIndexRecommendations()).containsExactly("CREATE INDEX idx_user ON users(id)");
        assertThat(dto.getPotentialBottlenecks()).containsExactly("Full table scan");
    }

    @Test
    @DisplayName("generateOptimization falls back gracefully on invalid JSON")
    void generateOptimization_invalidJson_returnsFallbackDto() {
        when(geminiLlmService.generateOptimization(anyString(), anyString())).thenReturn("This is not JSON");

        OptimizationDTO dto = aiInsightService.generateOptimization("SELECT * FROM users", "api-key");

        assertThat(dto.getOriginalSql()).isEqualTo("SELECT * FROM users");
        assertThat(dto.getOptimizedSql()).isEqualTo("SELECT * FROM users");
        assertThat(dto.getExplanation()).contains("Optimization analysis could not be completed");
        assertThat(dto.getIndexRecommendations()).isEmpty();
    }
}
