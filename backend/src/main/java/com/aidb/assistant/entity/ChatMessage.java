package com.aidb.assistant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "chat_messages",
    indexes = {
        @Index(name = "idx_cm_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_cm_timestamp", columnList = "timestamp")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 10)
    private String sender; // USER or AI

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String generatedSql;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private Long executionTimeMs;
    private Integer rowsReturned;
    private Boolean isSuccess;

    @Column(length = 500)
    private String errorMessage;

    @Column(columnDefinition = "LONGTEXT")
    private String resultJson;

    @Column(length = 20)
    private String chartType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
