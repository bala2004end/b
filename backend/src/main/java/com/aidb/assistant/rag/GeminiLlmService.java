package com.aidb.assistant.rag;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Wraps the Gemini LLM for SQL generation, explanation, and optimization.
 *
 * Design decisions:
 * - A default ChatLanguageModel is cached at startup using the configured API key.
 * - Per-request API key override creates a temporary model instance (uncommon path).
 * - System and user prompts are separated to reduce prompt injection surface.
 */
@Service
public class GeminiLlmService {

    private static final Logger log = LoggerFactory.getLogger(GeminiLlmService.class);

    private static final double SQL_TEMPERATURE = 0.0;       // Deterministic SQL generation
    private static final double EXPLAIN_TEMPERATURE = 0.3;   // Slightly creative for explanations
    private static final int MAX_OUTPUT_TOKENS = 2048;

    @Value("${aidb.gemini.model-name:gemini-1.5-flash}")
    private String configuredModelName;

    // Cached default model — created lazily on first use to allow @Value injection
    private volatile ChatLanguageModel defaultModel;
    private final String configuredApiKey;

    public GeminiLlmService(@Value("${aidb.gemini.api-key}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public String generateSql(String userQuestion, List<String> retrievedSchemaChunks, String overrideApiKey) {
        String schemaContext = retrievedSchemaChunks.isEmpty()
                ? "(No schema context available — generate a general MySQL SELECT query)"
                : String.join("\n\n---\n\n", retrievedSchemaChunks);

        String systemInstruction = """
                You are an expert Senior MySQL Database Administrator and SQL Generator.
                Convert the user's natural language question into a valid, optimized MySQL 8.0 SELECT query
                based ONLY on the schema context provided below.

                SCHEMA CONTEXT:
                %s

                STRICT RULES:
                1. Return ONLY plain SQL — no markdown fences, no explanations, no comments.
                2. Use explicit column names (no SELECT *) unless the question requests all columns.
                3. Use table aliases and explicit JOINs based on foreign key relationships.
                4. For date/time queries use CURDATE(), NOW(), YEAR(), MONTH() functions appropriately.
                5. Always add a LIMIT clause (default LIMIT 100) unless the question explicitly asks for all rows.
                6. Generate only SELECT/WITH/SHOW/EXPLAIN statements — never DDL or DML.
                """.formatted(schemaContext);

        return callGemini(systemInstruction, sanitizeInput(userQuestion), overrideApiKey, SQL_TEMPERATURE);
    }

    public String generateExplanation(String userQuestion, String sqlQuery, String rowCount, String sampleData, String overrideApiKey) {
        String systemInstruction = """
                You are a concise, executive-level AI Data Analyst.
                Explain the database query results in clear, business-friendly language.
                Focus on the key insight or answer to the user's question.
                Keep the response to 2-4 sentences.
                """;

        String userPrompt = """
                User Question: %s
                Executed SQL: %s
                Rows Returned: %s
                Sample Data (first 3 rows): %s

                Provide a brief executive summary of what these results mean.
                """.formatted(sanitizeInput(userQuestion), sqlQuery, rowCount, sampleData);

        return callGemini(systemInstruction, userPrompt, overrideApiKey, EXPLAIN_TEMPERATURE);
    }

    public String generateOptimization(String sqlQuery, String overrideApiKey) {
        String systemInstruction = """
                You are a MySQL performance tuning expert. Analyze the given SQL query.
                Return a JSON object with exactly these keys:
                {
                  "optimizedSql": "string — improved version of the query",
                  "explanation": "string — what was improved and why",
                  "indexRecommendations": ["array of CREATE INDEX DDL statements"],
                  "potentialBottlenecks": ["array of potential performance issues"]
                }
                Return ONLY valid JSON, no markdown.
                """;

        return callGemini(systemInstruction, "Analyze and optimize this SQL:\n" + sqlQuery, overrideApiKey, 0.1);
    }

    // -------------------------------------------------------------------------
    // Private implementation
    // -------------------------------------------------------------------------

    private String callGemini(String systemInstruction, String userPrompt, String overrideApiKey, double temperature) {
        String cleanModel = cleanModelName(configuredModelName);
        String apiKey = resolveApiKey(overrideApiKey);

        if (apiKey == null || apiKey.isBlank()) {
            log.error("No Gemini API key configured. Set aidb.gemini.api-key in application.yml or provide it in the request.");
            throw new IllegalStateException("Gemini API key is not configured. Please provide a valid API key in Settings.");
        }

        try {
            ChatLanguageModel model = resolveModel(apiKey, cleanModel, temperature);

            // Combine system instruction and user prompt (LangChain4j handles the format)
            String fullPrompt = systemInstruction + "\n\n" + userPrompt;
            String response = model.generate(fullPrompt);

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Gemini returned an empty response.");
            }

            return response.trim();
        } catch (IllegalStateException e) {
            throw e; // Re-throw configured/validation errors
        } catch (Exception e) {
            log.error("Gemini API call failed for model [{}]: {}", cleanModel, e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the default cached model if the API key matches; otherwise creates
     * a fresh instance for the override key. This avoids creating a new HTTP client
     * on every request for the common path.
     */
    private ChatLanguageModel resolveModel(String apiKey, String modelName, double temperature) {
        if (apiKey.equals(configuredApiKey)) {
            if (defaultModel == null) {
                synchronized (this) {
                    if (defaultModel == null) {
                        defaultModel = buildModel(apiKey, modelName, temperature);
                        log.info("Initialized default Gemini chat model: {}", modelName);
                    }
                }
            }
            return defaultModel;
        }
        // Per-request override (rare) — create a transient model
        return buildModel(apiKey, modelName, temperature);
    }

    private ChatLanguageModel buildModel(String apiKey, String modelName, double temperature) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxOutputTokens(MAX_OUTPUT_TOKENS)
                .build();
    }

    private String cleanModelName(String model) {
        if (model == null || model.isBlank()) return "gemini-1.5-flash";
        return model.startsWith("models/") ? model.substring(7) : model;
    }

    private String resolveApiKey(String overrideApiKey) {
        if (overrideApiKey != null && !overrideApiKey.isBlank()) {
            return overrideApiKey;
        }
        return configuredApiKey;
    }

    /**
     * Basic prompt injection protection — strips role-reversal patterns
     * and limits input length.
     */
    private String sanitizeInput(String input) {
        if (input == null) return "";
        String sanitized = input
                .replace("Ignore all previous instructions", "")
                .replace("ignore previous", "")
                .replace("SYSTEM:", "")
                .replace("Assistant:", "");
        return sanitized.length() > 2000 ? sanitized.substring(0, 2000) : sanitized;
    }
}
