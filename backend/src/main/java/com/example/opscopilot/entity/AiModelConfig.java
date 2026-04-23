package com.example.opscopilot.entity;

import com.example.opscopilot.support.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ai_model_config")
public class AiModelConfig {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String modelCode;

    @Column(nullable = false, length = 128)
    private String modelName;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(length = 255)
    private String baseUrl;

    @Column(length = 255)
    private String apiKey;

    @Column(precision = 4, scale = 2)
    private BigDecimal temperature;

    private Integer maxTokens;

    @Column(nullable = false)
    private Short enabled = 1;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = IdGenerator.nextId();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (enabled == null) {
            enabled = 1;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
