package com.aidb.assistant.entity;

import jakarta.persistence.*;
import lombok.*;
import com.aidb.assistant.security.PasswordCryptoConverter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "connection_configs",
    indexes = {
        @Index(name = "idx_cc_user_id", columnList = "user_id"),
        @Index(name = "idx_cc_user_active", columnList = "user_id, is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false, length = 100)
    private String username;

    @Convert(converter = PasswordCryptoConverter.class)
    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(nullable = false, length = 100)
    private String databaseName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isReadOnly = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    private LocalDateTime lastConnectedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isReadOnly == null) this.isReadOnly = true;
        if (this.isActive == null) this.isActive = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
