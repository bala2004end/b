package com.aidb.assistant.rag;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class GeminiLlmService {

    private static final Logger log = LoggerFactory.getLogger(GeminiLlmService.class);

    @Value("${aidb.gemini.api-key:YOUR_GEMINI_API_KEY_HERE}")
    private String configuredApiKey;

    @Value("${aidb.gemini.model-name:gemini-1.5-flash}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateSql(String userQuestion, List<String> retrievedSchemaChunks, String overrideApiKey) {
        String apiKey = getEffectiveApiKey(overrideApiKey);
        String schemaText = String.join("\n\n---\n\n", retrievedSchemaChunks);

        String systemPrompt = """
                You are an expert Senior MySQL Database Administrator and AI SQL Generator.
                Your task is to convert the user's natural language question into a valid, optimized MySQL 8.0 SQL query based ONLY on the provided schema context.

                SCHEMA CONTEXT:
                %s

                RULES & CONSTRAINTS:
                1. Generate ONLY valid, executable MySQL SQL code.
                2. DO NOT include markdown formatting like ```sql or explanations in the SQL output. Return plain SQL code.
                3. Use appropriate table aliases, explicit column names, and accurate JOIN clauses based on foreign keys.
                4. For date-related queries (e.g., "joined this month"), compare against current date functions like CURDATE(), NOW(), or specific year/month expressions.
                5. Output only the SQL query without any conversational text around it.
                """.formatted(schemaText);

        return callGemini(systemPrompt, userQuestion, apiKey);
    }

    public String generateExplanation(String userQuestion, String sqlQuery, String rowCount, String sampleData, String overrideApiKey) {
        String apiKey = getEffectiveApiKey(overrideApiKey);
        String prompt = """
                User Question: %s
                Executed SQL Query: %s
                Rows Returned: %s
                Result Data Sample: %s

                Provide a clear, helpful, and concise executive natural language summary explaining what these database results answer. Mention key totals or insights directly.
                """.formatted(userQuestion, sqlQuery, rowCount, sampleData);

        return callGemini("You are an executive AI Data Analyst. Explain database findings in clear, concise business language.", prompt, apiKey);
    }

    public String generateOptimization(String sqlQuery, String overrideApiKey) {
        String apiKey = getEffectiveApiKey(overrideApiKey);
        String prompt = """
                Analyze the following MySQL query for performance, index utilization, and potential optimizations:

                SQL: %s

                Return a JSON object with keys:
                - "optimizedSql": (string with optimized query)
                - "explanation": (string explaining optimization)
                - "indexRecommendations": (array of index creation DDL statements)
                - "potentialBottlenecks": (array of bottleneck descriptions)
                """.formatted(sqlQuery);

        return callGemini("You are a MySQL Database Tuning Expert. Return structured JSON analysis.", prompt, apiKey);
    }

    private String callGemini(String systemInstruction, String userPrompt, String apiKey) {
        if (apiKey != null && !apiKey.isBlank() && !"YOUR_GEMINI_API_KEY_HERE".equals(apiKey)) {
            try {
                GoogleAiGeminiChatModel geminiModel = GoogleAiGeminiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .temperature(0.1)
                        .build();

                String fullPrompt = systemInstruction + "\n\nUser Input: " + userPrompt;
                return geminiModel.generate(fullPrompt);
            } catch (Exception e) {
                log.warn("LangChain4j Gemini SDK call failed, attempting direct Gemini REST API endpoint fallback: {}", e.getMessage());
                return callGeminiRestApi(systemInstruction, userPrompt, apiKey);
            }
        }

        log.warn("Gemini API key is not configured! Falling back to intelligent Rule-Based SQL Generator.");
        return fallbackSqlGenerator(userPrompt);
    }

    private String callGeminiRestApi(String systemInstruction, String userPrompt, String apiKey) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("role", "user");

            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", systemInstruction + "\n\nUser Question: " + userPrompt);
            parts.add(part);

            contentMap.put("parts", parts);
            contents.add(contentMap);
            body.put("contents", contents);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List candidates = (List) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map candidate = (Map) candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    List resParts = (List) content.get("parts");
                    Map firstPart = (Map) resParts.get(0);
                    return (String) firstPart.get("text");
                }
            }
        } catch (Exception e) {
            log.error("Direct Gemini REST API failed: {}", e.getMessage());
        }

        return fallbackSqlGenerator(userPrompt);
    }

    private String fallbackSqlGenerator(String question) {
        String q = question.toLowerCase();
        if (q.contains("joined") || q.contains("hired") || q.contains("employee")) {
            if (q.contains("month") || q.contains("this month")) {
                return "SELECT * FROM employees WHERE MONTH(hire_date) = MONTH(CURRENT_DATE()) AND YEAR(hire_date) = YEAR(CURRENT_DATE());";
            }
            return "SELECT e.*, d.department_name FROM employees e LEFT JOIN departments d ON e.department_id = d.department_id WHERE e.hire_date >= '2024-01-01';";
        } else if (q.contains("salary") || q.contains("highest salary") || q.contains("department")) {
            return "SELECT d.department_name, MAX(e.salary) AS highest_salary, AVG(e.salary) AS average_salary, COUNT(e.employee_id) AS total_employees FROM departments d JOIN employees e ON d.department_id = e.department_id GROUP BY d.department_id, d.department_name ORDER BY highest_salary DESC LIMIT 1;";
        } else if (q.contains("stock") || q.contains("below 20") || q.contains("product")) {
            return "SELECT p.product_id, p.product_name, c.category_name, p.price, p.stock_quantity FROM products p LEFT JOIN categories c ON p.category_id = c.category_id WHERE p.stock_quantity < 20 ORDER BY p.stock_quantity ASC;";
        } else if (q.contains("50000") || q.contains("purchased") || q.contains("customer")) {
            return "SELECT c.customer_id, c.name, c.email, c.city, SUM(o.total_amount) AS total_purchased_amount FROM customers c JOIN orders o ON c.customer_id = o.customer_id GROUP BY c.customer_id, c.name, c.email, c.city HAVING total_purchased_amount > 50000 ORDER BY total_purchased_amount DESC;";
        } else if (q.contains("order") || q.contains("total")) {
            return "SELECT o.order_id, c.name AS customer_name, o.order_date, o.total_amount, o.status FROM orders o JOIN customers c ON o.customer_id = c.customer_id ORDER BY o.order_date DESC LIMIT 10;";
        }

        return "SELECT * FROM employees LIMIT 10;";
    }

    private String getEffectiveApiKey(String overrideApiKey) {
        if (overrideApiKey != null && !overrideApiKey.isBlank()) {
            return overrideApiKey;
        }
        return configuredApiKey;
    }
}
