package com.aidb.assistant.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SqlValidatorService {

    private static final Logger log = LoggerFactory.getLogger(SqlValidatorService.class);

    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
            "DROP", "DELETE", "UPDATE", "TRUNCATE", "ALTER", "CREATE", "INSERT",
            "REPLACE", "GRANT", "REVOKE", "SHUTDOWN", "EXEC", "EXECUTE"
    );

    private static final Pattern MULTI_STATEMENT_PATTERN = Pattern.compile(";\\s*\\S+");

    public void validateSql(String sql, boolean isReadOnly) throws IllegalArgumentException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("Generated SQL string is empty or null.");
        }

        String cleanedSql = sanitizeSql(sql).trim();
        String upperSql = cleanedSql.toUpperCase();

        if (isReadOnly) {
            for (String keyword : DANGEROUS_KEYWORDS) {
                Pattern wordPattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
                if (wordPattern.matcher(cleanedSql).find()) {
                    log.warn("Blocked dangerous SQL keyword [{}] in READ-ONLY mode: {}", keyword, cleanedSql);
                    throw new IllegalArgumentException("Read-Only Mode Active! Operation [" + keyword + "] is strictly prohibited.");
                }
            }

            if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("WITH") && !upperSql.startsWith("SHOW") && !upperSql.startsWith("EXPLAIN")) {
                throw new IllegalArgumentException("Read-Only Mode only permits SELECT, WITH, SHOW, or EXPLAIN statements.");
            }
        }

        if (MULTI_STATEMENT_PATTERN.matcher(cleanedSql).find()) {
            throw new IllegalArgumentException("Multiple SQL statements in a single execution are disallowed for security.");
        }
    }

    public String sanitizeSql(String rawSql) {
        if (rawSql == null) return "";
        String cleaned = rawSql.replaceAll("```sql", "").replaceAll("```", "").trim();
        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        return cleaned;
    }
}
