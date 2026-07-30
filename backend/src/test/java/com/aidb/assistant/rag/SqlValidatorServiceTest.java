package com.aidb.assistant.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SqlValidatorService.
 * Verifies that dangerous keywords are blocked in read-only mode,
 * multi-statement SQL is rejected, and safe SELECT queries pass.
 */
class SqlValidatorServiceTest {

    private SqlValidatorService validator;

    @BeforeEach
    void setUp() {
        validator = new SqlValidatorService();
    }

    // ── Read-Only Mode Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("SELECT query passes read-only validation")
    void selectQuery_passesReadOnly() {
        assertDoesNotThrow(() ->
            validator.validateSql("SELECT * FROM employees LIMIT 10", true));
    }

    @Test
    @DisplayName("WITH CTE query passes read-only validation")
    void withQuery_passesReadOnly() {
        assertDoesNotThrow(() ->
            validator.validateSql("WITH cte AS (SELECT * FROM orders) SELECT * FROM cte", true));
    }

    @Test
    @DisplayName("SHOW query passes read-only validation")
    void showQuery_passesReadOnly() {
        assertDoesNotThrow(() ->
            validator.validateSql("SHOW TABLES", true));
    }

    @Test
    @DisplayName("DROP blocked in read-only mode")
    void dropTable_blockedInReadOnly() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("DROP TABLE employees", true));
        assertTrue(ex.getMessage().contains("DROP"));
    }

    @Test
    @DisplayName("DELETE blocked in read-only mode")
    void deleteStatement_blockedInReadOnly() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("DELETE FROM users WHERE id = 1", true));
    }

    @Test
    @DisplayName("UPDATE blocked in read-only mode")
    void updateStatement_blockedInReadOnly() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("UPDATE employees SET salary = 0", true));
    }

    @Test
    @DisplayName("TRUNCATE blocked in read-only mode")
    void truncateStatement_blockedInReadOnly() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("TRUNCATE TABLE orders", true));
    }

    @Test
    @DisplayName("INSERT blocked in read-only mode")
    void insertStatement_blockedInReadOnly() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("INSERT INTO users VALUES (1, 'hacker')", true));
    }

    @Test
    @DisplayName("Non-SELECT statement blocked in read-only mode")
    void nonSelect_blockedInReadOnly() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("ALTER TABLE users ADD COLUMN age INT", true));
    }

    // ── Multi-statement Tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("Multi-statement SQL blocked")
    void multiStatement_blocked() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("SELECT * FROM users; DROP TABLE users", true));
    }

    // ── Null/Empty Tests ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Null SQL throws IllegalArgumentException")
    void nullSql_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql(null, true));
    }

    @Test
    @DisplayName("Blank SQL throws IllegalArgumentException")
    void blankSql_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            validator.validateSql("   ", true));
    }

    // ── Sanitize Tests ────────────────────────────────────────────────────────

    @Test
    @DisplayName("sanitizeSql strips markdown SQL fences")
    void sanitize_stripsMarkdownFences() {
        String raw = "```sql\nSELECT * FROM users\n```";
        String sanitized = validator.sanitizeSql(raw);
        assertFalse(sanitized.contains("```"));
        assertTrue(sanitized.contains("SELECT"));
    }

    @Test
    @DisplayName("sanitizeSql removes trailing semicolon")
    void sanitize_removesTrailingSemicolon() {
        String sanitized = validator.sanitizeSql("SELECT 1;");
        assertFalse(sanitized.endsWith(";"));
    }

    @Test
    @DisplayName("sanitizeSql handles null input")
    void sanitize_handlesNull() {
        assertEquals("", validator.sanitizeSql(null));
    }
}
